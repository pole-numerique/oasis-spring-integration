package org.oasis_eu.spring.kernel.exception;

/**
 * User: schambon
 * Date: 9/30/14
 */
public class ForbiddenException extends WrongQueryException {
    private static final long serialVersionUID = 4477923359119652107L;

    public ForbiddenException(String translatedBusinessMessage) {
		super(translatedBusinessMessage);
	}

	public ForbiddenException(String translatedBusinessMessage, int statusCode) {
		super(translatedBusinessMessage, statusCode);
	}

	public ForbiddenException(int statusCode) {
		super(statusCode);
	}

	public ForbiddenException() {
	    super();
	}

}