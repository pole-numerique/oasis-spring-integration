package org.oasis_eu.spring.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

/**
 * User: schambon
 * Date: 7/28/14
 */
public class KernelLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(KernelLoggingInterceptor.class);

    private static final Logger fullErrorLogger = LoggerFactory.getLogger("kernelLogging.logFullErrorResponses");
    private static final Logger performanceLogger = LoggerFactory.getLogger("kernelLogging.logRequestTimings");

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        if (logger.isDebugEnabled()) {
            logger.debug("Intercepted {} request to: {}", request.getMethod(), request.getURI());

            logger.debug("Request headers: {}", buildHeaders(request));
            logger.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
        }

        long before = System.currentTimeMillis();

        ClientHttpResponse response = execution.execute(request, body);

        long after = System.currentTimeMillis();

        if (performanceLogger.isDebugEnabled()) {
            long diff = after - before;
            performanceLogger.debug("{} Request to {} took {} ms", request.getMethod(), request.getURI(), diff);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Response: {} {}", response.getStatusCode().value(), response.getStatusCode().getReasonPhrase());
            StringBuilder headers = new StringBuilder("\n");
            response.getHeaders().entrySet().forEach(e -> headers.append(e.getKey() + ":\t" + e.getValue() + "\n"));
            logger.debug("Response headers: {}", headers.toString());
        }

        int bufferLimit = -1;
        if ((!response.getStatusCode().is2xxSuccessful() && fullErrorLogger.isInfoEnabled())) {
            bufferLimit = 10000; // up to 10kb response (should be enough for all Kernel error messages)
        } else if (fullErrorLogger.isDebugEnabled()) {
            bufferLimit = 1000000; // up to 1000kb response (should be enough for almost all Kernel messages)
        }

        if ( bufferLimit > 0) { // If there is any error AND log.info is enabled (PROD) OR log.debug is enabled (PREPROD / DEV)
            try {
                String text;
                BufferedInputStream bufIn = new BufferedInputStream(response.getBody());
                bufIn.mark(bufferLimit);
                response = new ClientHttpResponseWrapper(response, bufIn);
                InputStreamReader reader = new InputStreamReader(bufIn, Charsets.UTF_8);
                try {
                    text = CharStreams.toString(reader);
                } finally {
                    bufIn.reset(); // now can be read again from start
                    // NB. If the data contained in the stream (body) is larger than bufferLimit, it will throw an IOException within reset()
                    // due to the reader pointer position is -1 (means the mark has been invalidated and cannot be reset)
                }

                fullErrorLogger.info("Full response body: {}", text); // error message provided by Kernel (or response body if DEBUG mode)

            } catch (IOException ioex) { // happens in response.getInputStream(),
                // actually also happens and is caught within response.getStatusCode()
                fullErrorLogger.info("Full response body can't be retrieved (" + ioex.getMessage()
                        + "). Received status code : {}", response.getStatusCode());

                // example :

                // ERROR| 12:17:52.205 | org.oasis_eu.spring.kernel.service.Kernel - Cannot find <{If-Match=[1433240236449], Authorization=[Bearer
                // eyJpZCI6IjQ4YTljNjhjLTdhODEtNDU4NC1iYTFhLTA1OWVmNzJlMzFlNS9PY2FTWGNOdmo4VzMyTDFFTVgzZDNBIiwiaWF0IjoxNDMzMjM5Mjc2ODAzLCJleHAiOjE
                // 0MzMyNDI4NzY4MDN9]}> [] through endpoint http://kernel.ozwillo-dev.eu/d/pending-memberships/membership/d2f6b7da-2e8b-4504-86b7-
                // edb95d689b1c : error I/O error on DELETE request for "http://kernel.ozwillo-dev.eu/d/pending-memberships/membership/d2f6b7da-2e
                // 8b-4504-86b7-edb95d689b1c":http://kernel.ozwillo-dev.eu/d/pending-memberships/membership/d2f6b7da-2e8b-4504-86b7-edb95d689b1c;
                // nested exception is java.io.FileNotFoundException: http://kernel.ozwillo-dev.eu/d/pending-memberships/membership/d2f6b7da-2e8b-
                // 4504-86b7-edb95d689b1c

                // ERROR| 12:08:20.900 | org.oasis_eu.spring.kernel.service.Kernel - Cannot find <org.oasis_eu.spring.kernel.service.impl.UserDire
                // ctoryImpl$2MembershipRequest@25c3609f,{Authorization=[Bearer eyJpZCI6IjQ4YTljNjhjLTdhODEtNDU4NC1iYTFhLTA1OWVmNzJlMzFlNS9PY2FTWG
                // NOdmo4VzMyTDFFTVgzZDNBIiwiaWF0IjoxNDMzMjM5Mjc2ODAzLCJleHAiOjE0MzMyNDI4NzY4MDN9]}> [] through endpoint http://kernel.ozwillo-dev.
                // eu/d/memberships/org/f90e899b-c1f3-4bc6-8892-2dc138a22ca8 : error I/O error on POST request for "http://kernel.ozwillo-dev.eu/d/
                // memberships/org/f90e899b-c1f3-4bc6-8892-2dc138a22ca8":Server returned HTTP response code: 409 for URL: http://kernel.ozwillo-dev
                // .eu/d/memberships/org/f90e899b-c1f3-4bc6-8892-2dc138a22ca8; nested exception is java.io.IOException: Server returned HTTP respon
                // se code: 409 for URL: http://kernel.ozwillo-dev.eu/d/memberships/org/f90e899b-c1f3-4bc6-8892-2dc138a22ca8
            }

        }

        // even if we're not logging all requests, log info on errors
        if (!response.getStatusCode().is2xxSuccessful()) { // TODO LATER logger.error() if 5xx, unify with above
            if (logger.isWarnEnabled()) {
                logger.warn("{} request to {} resulted in {} {}", request.getMethod(), request.getURI(), response.getStatusCode(),
                        response.getStatusCode().getReasonPhrase());
                if (logger.isInfoEnabled()) {
                    logger.info("Request headers: {}", buildHeaders(request));
                    logger.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
                }
            }
        }

        return response;
    }

	private StringBuilder buildHeaders(HttpRequest request) {
        StringBuilder headers = new StringBuilder("\n");
        request.getHeaders().entrySet().forEach(e -> headers.append(e.getKey() + ":\t" + e.getValue() + "\n"));
		return headers;
	}
}
