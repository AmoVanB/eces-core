package de.tum.ei.lkn.eces.core;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * A System consists of different Component types which can be attached to
 * Entities. A System is controlled and handled by a Controller. The RootSystem
 * is the parent of any System.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class RootSystem implements Comparable<RootSystem> {
	/**
	 * Logger.
	 */
	protected final Logger logger;

	/**
	 * ID of the System.
	 */
	private int id;

	/**
	 * Controller handling the System.
	 */
	protected Controller controller;

	/**
	 * Creates a new System.
	 * @param controller Controller responsible for handling the System.
	 */
	public RootSystem(Controller controller) {
		this.controller = controller;
		this.controller.registerSystem(this);
		this.id = controller.getSystemIdentifier(this.getClass());
		logger = Logger.getLogger(this.getClass());
	}

	/**
	 * Creates a new System handled by a new default Controller.
	 */
	public RootSystem() {
		this(Controller.getDefaultController());
	}

	/**
	 * Gets the ID of the System (what a nice and useful doc :D)
	 * @return ID of the System.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the Controller handling the System.
	 * @return Controller object.
	 */
	protected Controller getController() {
		return this.controller;
	}

	/**
	 * Creates a JSONObject describing a given Component.
	 * @param component Given Component.
	 * @return The JSONObject.
	 */
	protected JSONObject toJSONObject(Component component) {
		return component.toJSONObject();
	}

	/**
	 * Creates a JSONObject describing a given Entity.
	 * @param entity Given Entity.
	 * @return The JSONObject.
	 */
	public JSONObject toJSONObject(Entity entity) {
		JSONObject systemJSON = new JSONObject();
		for(int j = 0; j < entity.getNumberOfComponents(this.getId()); j++) {
			if(entity.hasComponent(this.getId(), j)) {
				Component component = entity.getComponent(this.getId(), j);
				JSONObject componentJSON = this.toJSONObject(component);
				componentJSON.put("class", component.getClass().getSimpleName());
				systemJSON.put(Integer.toString(j), componentJSON);
			}
		}
		return systemJSON;
	}

	/**
	 * Compares two Systems.
	 * @param other System to compare the current System with.
	 * @return 0  if they have the same ID,
	 *         -1 if ID of current System is bigger,
	 *         +1 if ID of current System is smaller.
	 */
	@Override
	public int compareTo(RootSystem other) {
		return Integer.compare(other.getId(), this.getId());
	}
}
