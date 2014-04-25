package org.oasis_eu.spring.kernel.security;

import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.oasis_eu.spring.kernel.model.IdToken;
import org.oasis_eu.spring.kernel.model.TokenResponse;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.Instant;

/**
 * User: schambon
 * Date: 2/12/14
 */
@Service
public class OpenIdCService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdCService.class);

    @Autowired
    private OpenIdCConfiguration configuration;

    @Autowired
    @Qualifier("kernelRestTemplate")
    private RestTemplate restTemplate;

//    private Gson gson, userInfoGson;


    public OpenIdCAuthentication processAuthentication(String code, String state, String savedState, String savedNonce, String callbackUri) {

        if (savedState != null && savedState.equals(state)) {

            MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
            form.add("grant_type", "authorization_code");
            form.add("client_id", configuration.getClientId());
            form.add("redirect_uri", callbackUri);
            form.add("code", code);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + BaseEncoding.base64().encode(String.format("%s:%s", configuration.clientId, configuration.clientSecret).getBytes()));

            HttpEntity<TokenResponse> response = restTemplate.exchange(configuration.getTokenEndpoint(), HttpMethod.POST, new HttpEntity<>(form, headers), TokenResponse.class);
            TokenResponse tokenResponse = response.getBody();

            LOGGER.debug("Token response: " + tokenResponse);

            IdToken idToken = new IdToken();
            try {
                SignedJWT signedJWT = SignedJWT.parse(tokenResponse.getIdToken());
                ReadOnlyJWTClaimsSet idClaims = signedJWT.getJWTClaimsSet();
                idToken.setIat(idClaims.getIssueTime().getTime());
                idToken.setNonce(idClaims.getStringClaim("nonce"));
                idToken.setSub(idClaims.getSubject());
                idToken.setExp(idClaims.getExpirationTime().getTime());
                idToken.setIss(idClaims.getIssuer());

                if (!configuration.isMocked()) {
                    verifySignature(signedJWT);
                }

            } catch (ParseException e) {
                LOGGER.error("Cannot parse ID Token as JWS", e);
                return null;
            }

            if (!configuration.isMocked() && (idToken.getNonce() == null || !idToken.getNonce().equals(savedNonce))) {
                LOGGER.error("Invalid nonce, possible replay attack");
                return null;
            }

            Instant issuedAt = Instant.ofEpochSecond(idToken.getIat());
            Instant expires = issuedAt.plusSeconds(tokenResponse.getExpiresIn());

            return new OpenIdCAuthentication(idToken.getSub(), tokenResponse.getAccessToken(), tokenResponse.getIdToken(), issuedAt, expires);

        } else {
            LOGGER.error("Cannot match state with saved state; possible replay attack");

            return null;
        }
    }

    public boolean verifySignature(SignedJWT signedJWT) throws ParseException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + BaseEncoding.base64().encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()).getBytes()));

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

            } catch (JOSEException |InvalidKeySpecException |NoSuchAlgorithmException e) {
                LOGGER.error("Cannot verify key", e);
            }
        }

        return false;
    }

    public String getAuthUri(String state, String nonce, String callbackUri, String scopesToRequire, boolean forcePrompt) {
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
    
            if (forcePrompt) {
                builder = builder.queryParam("prompt", "consent");
            }
            return builder
                        .build()
                        .encode()
                        .toUriString();
        }
    }

    public UserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept", "application/json");

        UserInfo body = restTemplate.exchange(
                configuration.getUserInfoEndpoint(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserInfo.class)
                .getBody();

        return body;
    }

}
