package org.oasis_eu.spring.kernel.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;

/**
 * Do not consider 4xx as errors - 404 is a legitimate result
 *
 * User: schambon
 * Date: 7/31/14
 */
public class KernelResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected boolean hasError(HttpStatus statusCode) {
        return (statusCode == HttpStatus.UNAUTHORIZED ||
                statusCode == HttpStatus.FORBIDDEN ||
                statusCode == HttpStatus.NOT_FOUND ||
				statusCode.series() == HttpStatus.Series.SERVER_ERROR);
    }
}
