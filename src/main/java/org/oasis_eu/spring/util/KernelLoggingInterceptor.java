package org.oasis_eu.spring.util;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * User: schambon
 * Date: 7/28/14
 */
public class KernelLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(KernelLoggingInterceptor.class);

    private static final Logger fullErrorLogger = LoggerFactory.getLogger("kernelLogging.logFullErrorResponses");

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        if (logger.isDebugEnabled()) {
            logger.debug("Intercepted {} request to: {}", request.getMethod(), request.getURI());
            StringBuilder headers = new StringBuilder("\n");
            request.getHeaders().entrySet().forEach(e -> headers.append(e.getKey() + ":\t" + e.getValue() + "\n"));

            logger.debug("Request headers: {}", headers.toString());
            logger.debug("Request body: {}", new String(body));
        }


        ClientHttpResponse response = execution.execute(request, body);

        if (logger.isDebugEnabled()) {
            logger.debug("Response: {} {}", response.getStatusCode().value(), response.getStatusCode().getReasonPhrase());
            StringBuilder headers = new StringBuilder("\n");
            response.getHeaders().entrySet().forEach(e -> headers.append(e.getKey() + ":\t" + e.getValue() + "\n"));
            logger.debug("Request headers: {}", headers.toString());

            if (!response.getStatusCode().is2xxSuccessful() && fullErrorLogger.isDebugEnabled()) {
                String text;
                InputStreamReader reader = new InputStreamReader(response.getBody(),
                        Charsets.UTF_8);
                boolean threw = true;
                try {
                    text = CharStreams.toString(reader);
                    threw = false;
                } finally {
                    Closeables.close(reader, threw);
                }

                fullErrorLogger.debug("Full response body: {}", text);
            }
        }

        // even if we're not logging all requests, log info on errors
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.info("{} request to {} resulted in {} {}", request.getMethod(), request.getURI(), response.getStatusCode(), response.getStatusCode().getReasonPhrase());
        }


        return response;
    }
}
