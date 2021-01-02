package de.tum.ei.lkn.eces.core.util;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.ComponentStatus;
import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.RootSystem;
import de.tum.ei.lkn.eces.core.annotations.ComponentStateIs;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test System allowing to easily count and check triggered events for different
 * Component classes.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class EventCountTestSystem extends RootSystem {
	/**
	 * Map of counters.
	 */
	private Map<String, Integer> counter = new HashMap<>();

	/**
	 * Creates the System.
	 */
	public EventCountTestSystem() {
		super();
	}

	/**
	 * Creates the System.
	 * @param controller Controller handling the System.
	 */
	public EventCountTestSystem(Controller controller) {
		super(controller);
	}

	/**
	 * Increase the new event count for a Component's class.
	 * @param component Component whose class counter must be increased.
	 */
	@ComponentStateIs(State = ComponentStatus.New)
	public synchronized void getNew(Component component) {
		countUp("New " + component.getClass().getName());
	}

	/**
	 * Increase the updated event count for a Component's class.
	 * @param component Component whose class counter must be increased.
	 */
	@ComponentStateIs(State = ComponentStatus.Updated)
	public synchronized void getUpdated(Component component) {
		countUp("Updated " + component.getClass().getName());
	}

	/**
	 * Increase the destroyed event count for a Component's class.
	 * @param component Component whose class counter must be increased.
	 */
	@ComponentStateIs(State = ComponentStatus.Destroyed)
	public synchronized void getDestroyed(Component component) {
		countUp("Destroyed " + component.getClass().getName());
	}

	/**
	 * Increases a counter in the System.
	 * @param key Name of the counter.
	 */
	private void countUp(String key) {
		Integer value = counter.get(key);
		if(value == null)
			value = 0;
		value++;
		counter.put(key, value);
	}

	/**
	 * Checks that the new, updated and destroyed events have been observed the
	 * correct amount of times for a given class.
	 * @param clazz Class concerned by the event.
	 * @param eventNewCount New event count expected.
	 * @param eventUpdatedCount Updated event count expected.
	 * @param eventDestroyedCount Destroyed event count expected.
	 */
	public void doFullCheck(Class<? extends Component> clazz, int eventNewCount, int eventUpdatedCount, int eventDestroyedCount) {
		checkEvent(clazz, ComponentStatus.New, eventNewCount);
		checkEvent(clazz, ComponentStatus.Updated, eventUpdatedCount);
		checkEvent(clazz, ComponentStatus.Destroyed, eventDestroyedCount);
	}

	/**
	 * Checks if the counter Map is empty.
	 * @throws AssertionError if not.
	 */
	public void checkIfEmpty() {
		if(!counter.isEmpty()) {
			StringBuilder events = new StringBuilder("The following events are left:");
			for(Map.Entry<String,Integer> entry:counter.entrySet())
				events.append("\n").append(entry.getKey()).append(" - count = ").append(entry.getValue());
			fail(events.toString());
		}
	}

	/**
	 * Clears the counter map.
	 */
	public void reset() {
		counter.clear();
	}

	/**
	 * Checks that a given event for a Component type has the right count.
	 * The counter is then removed from the counters Map.
	 * @param clazz Class of the Component.
	 * @param status Event considered.
	 * @param count Count that is expected.
	 * @throws AssertionError if the count is not correct.
	 */
	private void checkEvent(Class<? extends Component> clazz, ComponentStatus status, int count) {
		String key;
		switch(status) {
			case New:
				key = "New ";
				break;
			case Destroyed:
				key = "Destroyed ";
				break;
			case Updated:
				key = "Updated ";
				break;
			default:
				key = "";
		}
		key = key + clazz.getName();
		Integer value = counter.get(key);
		counter.remove(key);
		if(value == null)
			value = 0;
		assertEquals("The event count for class event " + key + " should be " + count + " but is " + value + ".", count, (int) value);
	}
}