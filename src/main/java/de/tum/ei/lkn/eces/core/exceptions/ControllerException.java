package de.tum.ei.lkn.eces.core.exceptions;

/**
 * General runtime exception (means that it does not necessarily have to be
 * caught) related to the operation of a Controller.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ControllerException extends RuntimeException {
	private static final long serialVersionUID = 3913533029680219603L;

	public ControllerException(String message) {
		super(message);
	}
	public ControllerException(String message, Throwable cause) {
		super(message,cause);
	}
}