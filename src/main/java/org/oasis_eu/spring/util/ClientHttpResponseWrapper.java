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

	@Override
	public HttpHeaders getHeaders() {
		return delegate.getHeaders();
	}

	@Override
	public InputStream getBody() throws IOException {
		return body;
	}

	@Override
	public HttpStatus getStatusCode() throws IOException {
		return delegate.getStatusCode();
	}

	@Override
	public int getRawStatusCode() throws IOException {
		return delegate.getRawStatusCode();
	}

	@Override
	public String getStatusText() throws IOException {
		return delegate.getStatusText();
	}

	@Override
	public void close() {
		delegate.close();
	}

}
