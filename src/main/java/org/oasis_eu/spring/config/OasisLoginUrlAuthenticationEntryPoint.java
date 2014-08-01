package org.oasis_eu.spring.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 
 * @author mkalam-alami
 *
 */
public class OasisLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	@Autowired(required = false)
	private LocaleResolver localeResolver;
	
    public OasisLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {
    	// Pass current locale to login service
        if (localeResolver != null) {

            return UriComponentsBuilder.fromUriString(super.getLoginFormUrl())
                    .queryParam("ui_locales", localeResolver.resolveLocale(request))
                    .build()
                    .encode()
                    .toUriString();
        } else {
            return super.getLoginFormUrl();
        }

    }
    
}
