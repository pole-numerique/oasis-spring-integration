package org.oasis_eu.spring.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 
 * @author mkalam-alami
 *
 */
public class OasisLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public OasisLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {
    	// Pass current locale to login service
        if (LocaleContextHolder.getLocale() != null) {

            return UriComponentsBuilder.fromUriString(super.getLoginFormUrl())
                    .queryParam("ui_locales", LocaleContextHolder.getLocale())
                    .build()
                    .encode()
                    .toUriString();
        } else {
            return super.getLoginFormUrl();
        }

    }
    
}
