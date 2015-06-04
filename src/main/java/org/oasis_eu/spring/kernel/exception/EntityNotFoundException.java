package org.oasis_eu.spring.kernel.exception;

/**
 * User: schambon
 * Date: 8/13/14
 */
public class EntityNotFoundException extends WrongQueryException {
	private static final long serialVersionUID = 8440935770083907383L;
	
	public EntityNotFoundException(String translatedBusinessMessage) {
		super(translatedBusinessMessage);
	}

	public EntityNotFoundException(String translatedBusinessMessage, int statusCode) {
		super(translatedBusinessMessage,statusCode);
	}

	public EntityNotFoundException(int statusCode) {
		super(statusCode);
	}

}
