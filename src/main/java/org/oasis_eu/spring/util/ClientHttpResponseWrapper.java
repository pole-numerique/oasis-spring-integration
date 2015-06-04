package org.oasis_eu.spring.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Membership of an organization
 *
 * User: ilucatero
 * Date: 06/01/15
 */
public class ClientHttpResponseWrapper implements ClientHttpResponse {

	private ClientHttpResponse delegate;
	private BufferedInputStream body;

	public ClientHttpResponseWrapper(ClientHttpResponse response, BufferedInputStream bufIn) {
		this.delegate = response;
		this.body = bufIn;
	}

	public HttpHeaders getHeaders() {
		return delegate.getHeaders();
	}

	public InputStream getBody() throws IOException {
		return body;
	}

	public HttpStatus getStatusCode() throws IOException {
		return delegate.getStatusCode();
	}

	public int getRawStatusCode() throws IOException {
		return delegate.getRawStatusCode();
	}

	public String getStatusText() throws IOException {
		return delegate.getStatusText();
	}

	public void close() {
		delegate.close();
	}

}
