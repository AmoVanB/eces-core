package de.tum.ei.lkn.eces.core;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A Component is an element attached to an Entity.
 * An Entity can only hold one instance of a given Component type.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class Component implements Cloneable {
	/**
	 * Logger.
	 */
	private final static Logger logger = Logger.getLogger(Component.class);

	/**
	 * Read-write lock on the Component.
	 */
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Entity to which the Component is attached.
	 */
	private Entity entity;

	/**
	 * Gets the read-write lock on the Component.
	 * @return The lock.
	 */
	protected synchronized ReentrantReadWriteLock getLock() {
		return this.lock;
	}

	/**
	* Gets the Entity to which the Component is attached.
	* @return Entity
	*/
	public synchronized Entity getEntity() {
		return entity;
	}

   /**
	* Gets the Entity to which the Component is attached.
	* If the Component is not yet attached to any Entity, waits until this is
	* done and then returns the Entity.
	* @return Entity
	*/
	public synchronized Entity getWaitEntity() {
		try {
			while(entity == null)
				this.wait();
		} catch (InterruptedException e) {
			return null;
		}

		return entity;
	}

	/**
	* Sets the Entity to which the Component is attached.
	* @param entity Entity to which the Component must be attached. If null,
	*               the current Entity of the Component is removed from it.
	*/
	protected synchronized void setEntity(Entity entity) {
		if(entity == null) {
	   		removeEntity();
	   		return;
		}

		this.entity = entity;
		logger.trace("Entity of Component '" + this + "' set to '" + entity + "' (ID: " + entity.getId() + ").");
		// Notify that Component has now an Entity, in case someone is waiting.
		this.notifyAll();
	}

	/**
	 * Removes the Entity of the Component.
	 */
	protected synchronized void removeEntity() {
		this.entity = null;
		logger.trace("Entity of Component '" + this + "' removed.");
	}

	/**
	 * Creates a JSON Object out of the Component.
	 * The JSON Object is empty. This can be overridden by deriving classes.
	 * @return JSONObject.
	 */
	public JSONObject toJSONObject() {
		return new JSONObject();
	}

	/**
	 * Returns the identifier of this Component. The ID is the ID of the Entity
	 * holding this Component. This is unique among all Components of this type.
	 * @return the ID.
	 */
	public long getId() {
		return this.getEntity().getId();
	}

	@Override
	public String toString() {
		if(getEntity() == null)
			return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
		return getClass().getSimpleName() + "@" + getId();
	}

	@Override
	public Component clone() {
		Component clone = null;
		try {
			clone = (Component) super.clone();
		} catch (CloneNotSupportedException e) {
			// Should not happen.
			e.printStackTrace();
			return null;
		}

		clone.removeEntity();
		clone.lock = new ReentrantReadWriteLock();
		return clone;
	}
}
