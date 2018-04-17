package org.oasis_eu.spring.kernel.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.oasis_eu.spring.kernel.model.IdToken;
import org.oasis_eu.spring.kernel.model.TokenResponse;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

/**
 * User: schambon
 * Date: 2/12/14
 */
@Service
public class OpenIdCService {

    public static final String NONCE = "openid_connect_nonce";
    public static final String STATE = "openid_connect_state";
    private static final Logger logger = LoggerFactory.getLogger(OpenIdCService.class);

    @Autowired
    private OpenIdCConfiguration configuration;

    @Autowired
    private ObjectMapper objectMapper;

    /** used in auth process (NOT once auth'd) */
    @Autowired
    @Qualifier("kernelRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private Kernel kernel;

    public OpenIdCAuthentication processAuthentication(String code, String state, String savedState, String savedNonce, String callbackUri) {
        return processAuthentication(code, null, state, savedState, savedNonce, callbackUri);
    }

    public OpenIdCAuthentication processAuthentication(String code, String refreshToken, String state, String savedState,
            String savedNonce, String callbackUri) {

        if ((savedState == null && state == null) || (savedState != null && savedState.equals(state))) {

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            if (refreshToken != null) {
                form.add("grant_type", "refresh_token");
                form.add("refresh_token", refreshToken);
            } else { // default : code
                form.add("grant_type", "authorization_code");
                form.add("code", code);
                form.add("redirect_uri", callbackUri); // (should not be required in refresh_token case)
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + BaseEncoding.base64().encode(
                String.format(Locale.ROOT, "%s:%s", configuration.getClientId(), configuration.getClientSecret()).getBytes(StandardCharsets.UTF_8)));

            logger.debug("Token endpoint: {}", configuration.getTokenEndpoint());
            ResponseEntity<TokenResponse> response = restTemplate.exchange(configuration.getTokenEndpoint(),
            		HttpMethod.POST, new HttpEntity<>(form, headers), TokenResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
              logger.error("Oops, got error {}", response.getStatusCode());
              for (Map.Entry<String, List<String>> header : response.getHeaders().entrySet()) {
                logger.debug("Header: {}: {}", header.getKey(), header.getValue());
              }
              // TODO throw an exception
            }
            TokenResponse tokenResponse = response.getBody();

            logger.debug("Token response: {}", tokenResponse);
            if (refreshToken == null) {
                IdToken idToken = new IdToken();
                boolean appUser;
                boolean appAdmin;
                String acr = null;
                try {
                    SignedJWT signedJWT = SignedJWT.parse(tokenResponse.getIdToken());
                    JWTClaimsSet idClaims = signedJWT.getJWTClaimsSet();
                    idToken.setIat(idClaims.getIssueTime().getTime());
                    idToken.setNonce(idClaims.getStringClaim("nonce"));
                    idToken.setSub(idClaims.getSubject());
                    idToken.setExp(idClaims.getExpirationTime().getTime());
                    idToken.setIss(idClaims.getIssuer());

                    appUser = Optional.ofNullable(idClaims.getBooleanClaim("app_user")).orElse(false);
                    appAdmin = Optional.ofNullable(idClaims.getBooleanClaim("app_admin")).orElse(false);
                    acr = idClaims.getStringClaim("acr");

                    if (!configuration.isMocked()) {
                        verifySignature(signedJWT);
                    }

                    logger.debug("Signature verified");

                } catch (ParseException e) {
                    logger.error("Cannot parse ID Token as JWS", e);
                    return null;
                }

                if (!configuration.isMocked() && (idToken.getNonce() == null || !idToken.getNonce().equals(savedNonce))) {
                    logger.error("Invalid nonce, possible replay attack");
                    return null;
                }

                Instant issuedAt = Instant.ofEpochMilli(idToken.getIat());
                Instant expires = issuedAt.plusSeconds(tokenResponse.getExpiresIn());

                OpenIdCAuthentication authentication = new OpenIdCAuthentication(idToken.getSub(), tokenResponse.getAccessToken(), tokenResponse.getIdToken(),
                        issuedAt, expires, appUser, appAdmin);
                if (tokenResponse.getRefreshToken() != null) {
                    authentication.setRefreshToken(tokenResponse.getRefreshToken());
                    authentication.setRefreshNonce(savedNonce);
                }
                if (!StringUtils.isEmpty(acr)) {
                    authentication.setAcr(acr);
                }

                return authentication;
            }else{
                Instant now = Instant.now();
                return new OpenIdCAuthentication("", tokenResponse.getAccessToken(), tokenResponse.getIdToken(),now, now.minusMillis(-3600), true, true);
            }
        } else {
            logger.error("Cannot match state with saved state; possible replay attack");

            return null;
        }
    }

    public boolean verifySignature(SignedJWT signedJWT) throws ParseException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",
            "Basic " + BaseEncoding.base64().encode(
                String.format(Locale.ROOT, "%s:%s", configuration.getClientId(), configuration.getClientSecret() ).getBytes(StandardCharsets.UTF_8))
        );

        // TODO cache the keys as per HTTP caching headers
        String keysAsJson = restTemplate.exchange(configuration.getKeysEndpoint(), HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();

        JWKSet jwkSet = JWKSet.parse(keysAsJson);
        if (jwkSet.getKeys().size() == 1) {
            // TODO do this better when there are more than one key
            JWK key = jwkSet.getKeys().get(0);
            JWSVerifier verifier = null;
            try {
                if (key instanceof RSAKey) {
                    verifier = new RSASSAVerifier(((RSAKey) key).toRSAPublicKey());
                } else if (key instanceof OctetSequenceKey) {
                    verifier = new MACVerifier(((OctetSequenceKey) key).toByteArray());
                }
                if (verifier != null) {
                    return signedJWT.verify(verifier);
                }

            } catch (JOSEException e) {
                logger.error("Cannot verify key", e);
            }
        }

        return false;
    }

    public String getAuthUri(String state, String nonce, String callbackUri, String scopesToRequire, PromptType promptType, String uiLocales) {
        if (configuration.isMocked()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(configuration.getMockLoginPageUri())
                    .queryParam("response_type", "code")
                    .queryParam("client_id", configuration.getClientId())
                    .queryParam("scope", scopesToRequire)
                    .queryParam("redirect_uri", callbackUri)
                    .queryParam("state", state)
                    .queryParam("nonce", nonce);

            return builder
                        .build()
                        .encode()
                        .toUriString();
        } else {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(configuration.getAuthEndpoint())
                    .queryParam("response_type", "code")
                    .queryParam("client_id", configuration.getClientId())
                    .queryParam("scope", scopesToRequire)
                    .queryParam("redirect_uri", callbackUri)
                    .queryParam("state", state)
                    .queryParam("nonce", nonce);

            if (uiLocales != null) {
                builder = builder.queryParam("ui_locales", uiLocales);
            }

            if (promptType.equals(PromptType.FORCED)) {
                builder = builder.queryParam("prompt", "consent");
            } else if (promptType.equals(PromptType.NONE)) {
                builder = builder.queryParam("prompt", "none");
            }
            return builder
                        .build()
                        .encode()
                        .toUriString();
        }
    }

    public UserInfo getUserInfo(OpenIdCAuthentication openIdCAuthentication) {
    	// possibly nothing yet in SecurityContextHolder.getContext() (if within authenticate()),
    	// so using access token instead :
    	String accessToken = openIdCAuthentication.getAccessToken();

    	return kernel.getEntityOrNull(configuration.getUserInfoEndpoint(),
    			UserInfo.class, //Oasis kernel User Authentication Impl
    			user(accessToken));

    }




    public String getStateString(StateType type) {
        State state = new State();
        state.setType(type);

        byte[] bytes = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        String rd = BaseEncoding.base64Url().encode(bytes);

        state.setRandom(rd);

        try {
            String s = objectMapper.writeValueAsString(state);
            return BaseEncoding.base64Url().encode(s.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            logger.error("Cannot serialize state", e);
            throw new RuntimeException(e);
        }
    }

    public StateType getStateType(String stateString) {
        try {
            State s = objectMapper.readValue(BaseEncoding.base64Url().decode(stateString), State.class);
            return s.getType();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void redirectToAuth(HttpServletRequest request, HttpServletResponse response, StateType stateType) throws IOException {
        HttpSession session = request.getSession();
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String nonce = BaseEncoding.base64Url().encode(bytes);

        String state = getStateString(stateType);

        session.setAttribute(STATE, state);
        session.setAttribute(NONCE, nonce);

        String callbackUri = configuration.getCallbackUri();
        String scopesToRequire = configuration.getScopesToRequire();
        String uiLocales = request.getParameter("ui_locales");
        if (uiLocales == null) {
            uiLocales = RequestContextUtils.getLocale(request).getLanguage();
        }

        PromptType promptType = stateType.equals(StateType.SIMPLE_CHECK) ? PromptType.NONE : PromptType.DEFAULT;

        if (scopesToRequire.contains("offline_access")) {
            promptType = PromptType.FORCED;
        }

        String authUri = getAuthUri(state, nonce, callbackUri, scopesToRequire,
                promptType, uiLocales);

        logger.debug("Auth uri is: {}", authUri);
        response.sendRedirect(authUri);
    }
}
