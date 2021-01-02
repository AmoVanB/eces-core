package de.tum.ei.lkn.eces.core;

/**
 * Component allowing to have different instances of data stored for a single
 * Component of a System. Indeed, an Entity can usually store only a single
 * instance of a Component for a System. This allows a System to have different
 * Objects working on different information associated to a Component.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class LocalComponent extends Component {
	/**
	 * Array of data instances. The data is specified as an Object. Classes
	 * deriving from LocalComponent can then define the Object subclass they
	 * want to use.
	 */
	private Object data[] = new Object[0];

	/**
	 * Returns a specific data instance.
	 * @param index Index of the instance.
	 * @return Object representing the data.
	 */
	protected Object get(int index) {
		if(index >= data.length) {
			// Increase the size of the data array.
			Object temp[] = data;
			data = new Object[index + 1];
			System.arraycopy(temp, 0, data, 0, temp.length);
		}

		if(data[index] == null)
			data[index] = init();

		return data[index];
	}

	/**
	 * Initialization method returning the default data.
	 * @return Default Object representing the data.
	 */
	abstract public Object init();
}