package org.oasis_eu.spring.kernel.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Provides HTTP response as request attribute, allows to ex. set headers
 * (ex. X-Oasis-Portal-Kernel-SomethingWentWrong, see Kernel & #166 "Something went wrong" message)
 * within service layer.
 * 
 * @author mdutoo
 *
 */
public class ResponseProviderInterceptor implements WebFilter {

    public static final String REQUEST_ATTRIBUTE_RESPONSE_NAME = ResponseProviderInterceptor.class.getName() +  ".reponse";

    /**
     * 
     * @return HTTP response from request attribute set in preHandle
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes reqAttrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if (reqAttrs != null) {
            HttpServletRequest request = reqAttrs.getRequest();
            if (request != null) {
                return (HttpServletResponse) request.getAttribute(REQUEST_ATTRIBUTE_RESPONSE_NAME);
            }
        }
        return null;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        RequestContextHolder.currentRequestAttributes()
                .setAttribute(REQUEST_ATTRIBUTE_RESPONSE_NAME, exchange.getResponse(), RequestAttributes.SCOPE_REQUEST);
        return Mono.empty();
    }
}
