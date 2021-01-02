package de.tum.ei.lkn.eces.core;

/**
 * Enumeration of all the possible events associated to a Component.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public enum ComponentStatus {
	/**
	 * The component has been attached to an Entity.
	 */
	New,
	/**
	 * The component has been detached from an Entity.
	 */
	Destroyed,
	/**
	 * The information in the Component has been Updated.
	 */
	Updated,
	/**
	 * Any of the events listed here.
	 */
	Any,
}