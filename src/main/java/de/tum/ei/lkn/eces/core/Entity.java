package de.tum.ei.lkn.eces.core;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * An Entity holds Components belonging to different Systems.
 * For a given System, one Entity can only hold one instance of a given
 * Component class.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 *
 */
public class Entity implements Comparable<Entity> {
	/**
	 * Logger.
	 */
	private final static Logger logger = Logger.getLogger(Entity.class);

	/**
	 * ID of the Entity.
	 */
	private long id;

	/**
	 * Two dimensional array containing the Components attached to the Entity.
	 * The first dimension represents all the Systems while the second
	 * dimension corresponds to the Components of each System.
	 */
	private Component[][] data;

	/**
	 * Creates an Entity.
	 * @param id ID of the Entity.
	 */
	protected Entity(long id) {
		this.id = id;
		logger.trace("New Entity created with id '" + id + "'.");
	}

	/**
	 * Creates an Entity.
	 * @param id ID of the Entity.
	 * @param numSystems number of Systems that the Entity can handle.
	 */
	protected Entity(long id, int numSystems) {
		this.id = id;
		data = new Component[numSystems][];
		logger.trace("New Entity created with id '" + id + "' and max number of systems '" + numSystems + "'.");
	}

	/**
	 * Gets the ID of the entity.
	 * @return id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Checks if the Entity has a given Component.
	 * @param systemIdentifier ID of the System.
	 * @param componentIdentifier ID of the Component within the System.
	 * @return boolean based on existence or not of the Component.
	 */
	protected synchronized boolean hasComponent(int systemIdentifier, int componentIdentifier) {
		return !(this.data == null
			|| this.data.length <= systemIdentifier
			|| this.data[systemIdentifier] == null
			|| data[systemIdentifier].length <= componentIdentifier
			|| data[systemIdentifier][componentIdentifier] == null);
	}

	/**
	 * Gets a given Component attached to the Entity.
	 * @param systemIdentifier ID of the System.
	 * @param componentIdentifier ID of the Component within the System.
	 * @return Component or null if no component with these IDs exist.
	 */
	protected synchronized Component getComponent(int systemIdentifier, int componentIdentifier) {
		if(hasComponent(systemIdentifier, componentIdentifier))
			return data[systemIdentifier][componentIdentifier];
		return null;
	}

	/**
	 * Removes a given Component attached to the Entity.
	 * @param systemIdentifier ID of the System.
	 * @param componentIdentifier ID of the Component within the System.
	 * @return Component removed or null if no Component with these IDs exist.
	 */
	protected synchronized Component removeComponent(int systemIdentifier, int componentIdentifier) {
		Component comp = getComponent(systemIdentifier, componentIdentifier);
		if(comp == null) {
			logger.warn("Impossible to remove Component with sysID: '" + systemIdentifier + "' and compID: '" + componentIdentifier + "' from Entity '" + this + "' (ID: " + this.id + "): does not exist.");
			return null;
		}

		// The Component exists. We can remove it safely.
		data[systemIdentifier][componentIdentifier] = null;
		logger.trace("Component '" + comp + "' (sysID: " + systemIdentifier + ", compID: " + componentIdentifier + ") removed from Entity '" + this + "' (ID: " + this.id + ").");
		return comp;
	}

	/**
	 * Attaches a given Component to the Entity.
	 * @param component Component to attach.
	 * @param systemIdentifier ID of the System.
	 * @param componentIdentifier ID of the Component within the System.
	 * @throws ArrayIndexOutOfBoundsException if the systemIdentifier and
	 *                                        componentIdentifier do not
	 *                                        correspond to an existing
	 *                                        Component.
	 * @throws NullPointerException if the systemIdentifier and
	 *                              componentIdentifier do not correspond to an
	 *                              existing Component.
	 */
	protected synchronized void setComponent(Component component, int systemIdentifier, int componentIdentifier) throws ArrayIndexOutOfBoundsException, NullPointerException {
		data[systemIdentifier][componentIdentifier] = component;
		data[systemIdentifier][componentIdentifier].setEntity(this);
		this.notifyAll();
		logger.trace("Component '" + component + "' (sysID: " + systemIdentifier + ", compID: " + componentIdentifier + ") attached to Entity '" + this + "' (ID: " + this.id + ").");
	}

	/**
	 * Extends the maximum number of different:
	 * - Systems the Entity can handle,
	 * - Component classes the Entity can carry for a given System.
	 * All the Components currently attached to this Entity are kept.
	 * @param systemIdentifier ID of the System for which the maximum number of
	 *                         Components must be increased.
	 * @param maxSystemCount New max number of Systems.
	 * @param maxComponentCount New max number of Components for the System
	 *                          specified by systemIdentifier.
	 */
	protected synchronized void extend(int systemIdentifier, int maxSystemCount, int maxComponentCount) {
		// Extending number of Systems if necessary.
		if(this.data == null || this.data.length < maxSystemCount) {
			Component[][] temp = this.data;
			this.data = new Component[maxSystemCount][];
			if(temp != null)
				System.arraycopy(temp, 0, this.data, 0, temp.length);
			logger.trace("New max number of Systems for Entity '" + this + "' (ID: " + this.id + "): " + maxSystemCount + ".");
		}

		// Extending number of Components for specified System if necessary.
		if(this.data[systemIdentifier] == null || this.data[systemIdentifier].length < maxComponentCount) {
			Component[] temp = this.data[systemIdentifier];
			this.data[systemIdentifier] = new Component[maxComponentCount];
			// Copy the previous Components in the new array.
			if(temp != null)
				System.arraycopy(temp, 0, this.data[systemIdentifier], 0, temp.length);
			logger.trace("Max number of Components for sysID '" + systemIdentifier + "' of Entity '" + this + "' (ID: " + this.id + ") set to " + maxComponentCount + ".");
		}
		else {
			logger.warn("Max number of Components for sysID '" + systemIdentifier + "' not set to " + maxComponentCount + " because already bigger or equal (" + this.data[systemIdentifier].length + ").");
		}
	}

	/**
	 * Gets the number of Systems currently known by the Entity. Components
	 * attached to the Entity are all belonging to a System known by the Entity.
	 * @return The number of Systems.
	 */
	protected int getNumberOfSystems() {
		if(data == null)
			return 0;
		return data.length;
	}

	/**
	 * For a given System, gets the number of different Components types
	 * known by the Entity.
	 * @param system The System ID.
	 * @return Number of Components for this System.
	 */
	protected int getNumberOfComponents(int system) {
		if(data[system] == null)
			return 0;
		return data[system].length;
	}

	/**
	 * Compares two Entities.
	 * @param other Entity to compare the current Entity with.
	 * @return 0  if they have the same ID,
	 *         -1 if ID of current Entity is bigger,
	 *         +1 if ID of current Entity is smaller.
	 */
	@Override
	public int compareTo(Entity other) {
		return Long.compare(other.id, this.id);
	}

	/**
	 * String representation of the Entity.
	 * Returns the string: "Entity: x y | z | ..."
	 * where x and y are the Component classes names of the Components of the
	 * first System, z of the second System, and so on.
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder data = new StringBuilder("Entity:");
		if(this.data != null)
			for(Component c[] : this.data) {
				if(c != null)
					for(Component z : c)
						if(z != null)
							data.append(" ").append(z.getClass().getSimpleName());
				data.append(" |");
			}

		return data.toString();
	}

	public JSONObject toJSONObject() {
		JSONObject result = new JSONObject();
		for (Component[] datum : this.data) {
			if (datum != null) {
				for (Component component : datum) {
					if (component != null)
						result.put(component.getClass().getSimpleName(), component.toJSONObject());
				}
			}
		}

		return result;
	}
}
