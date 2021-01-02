package de.tum.ei.lkn.eces.core;

/**
 * A Filter has to be able to tell whether a given Component is accepted or
 * refused by some filtering.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class Filter {
	/**
	 * Tells if a Component is accepted or refused by the Filter.
	 * @param component Component to submit to the Filter.
	 * @return true if the Component is accepted by the Filter, false otherwise.
	 */
	public abstract boolean isAccepted(Component component);
}
