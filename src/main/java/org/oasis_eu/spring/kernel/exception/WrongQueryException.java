package org.oasis_eu.spring.kernel.exception;

/**
 * User: schambon
 * Date: 8/13/14
 */
public class WrongQueryException extends RuntimeException {
    private static final long serialVersionUID = -5331781161327925953L;

    private int statusCode;
	private String translatedBusinessMessage;

	/**
	 * @param message Kernel error response body if any
	 * (is ALWAYS a useful message that can be displayed to end user,
	 * rather than a technical error that is leaking secure information)
	 */
	public WrongQueryException() {
		super();
		this.statusCode = 400;
	}
	public WrongQueryException(String translatedBusinessMessage) {
		super(translatedBusinessMessage);
		this.translatedBusinessMessage = translatedBusinessMessage;
	}

	public WrongQueryException(String translatedBusinessMessage, int statusCode) {
		this.translatedBusinessMessage = translatedBusinessMessage;
		this.statusCode = statusCode;
	}

	public WrongQueryException( int statusCode) {
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
	public String getTranslatedBusinessMessage() {
		return translatedBusinessMessage;
	}
	public void setTranslatedBusinessMessage(String translatedBusinessMessage) {
		this.translatedBusinessMessage =  translatedBusinessMessage;
	}

}