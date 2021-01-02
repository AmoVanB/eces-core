package de.tum.ei.lkn.eces.core;

import org.json.JSONObject;

/**
 * Class carrying util methods for JSON manipulation.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class JSONUtil {
	/**
	 * Merges two JSON Objects.
	 * @param json1 First object.
	 * @param json2 Second object.
	 * @return Merged object.
	 */
	public static JSONObject merge(JSONObject json1, JSONObject json2) {
		JSONObject mergedJSON = new JSONObject();

		// Adding first JSON
		for (String key : json1.keySet())
			mergedJSON.put(key, json1.get(key));

		// Adding second JSON
		for (String key : json2.keySet())
			mergedJSON.put(key, json2.get(key));

		return mergedJSON;
	}

	/**
	 * Creates a JSON Object out of an Entity. The JSON Object lists all the
	 * Systems known by the Entity, and for each of them, the Components
	 * attached to the Entity.
	 * @param controller Controller responsible for the entity.
	 * @param entity Entity ID.
	 * @return The created JSON Object.
	 */
	public static synchronized JSONObject createJSONObject(Controller controller, Entity entity) {
		JSONObject entityJSON = new JSONObject();
		entityJSON.put("type", "Entity");
		entityJSON.put("entityId", entity.getId());
		JSONObject data = new JSONObject();
		for(int i = 0; i < entity.getNumberOfSystems(); i++) {
			RootSystem rootSystem = controller.getSystemObject(i);
			JSONObject systemJSON = new JSONObject();
			if(rootSystem != null) {
				systemJSON = rootSystem.toJSONObject(entity);
				if(systemJSON.length() != 0) {
					systemJSON.put("sysClass", controller.getSystemClass(i).getSimpleName());
					data.put(Integer.toString(i), systemJSON);
				}
			}
		}
		entityJSON.put("data",  data);
		return entityJSON;
	}
}
