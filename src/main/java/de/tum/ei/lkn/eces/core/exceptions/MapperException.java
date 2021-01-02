package de.tum.ei.lkn.eces.core.exceptions;

/**
 * General runtime exception (means that it does not necessarily have to be
 * caught) related to the operation of a Mapper.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MapperException extends RuntimeException {
	private static final long serialVersionUID = 3913533029680219602L;

	public MapperException(String message) {
		super(message);
	}
}