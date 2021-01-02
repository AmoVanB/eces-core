package de.tum.ei.lkn.eces.core;

import de.tum.ei.lkn.eces.core.annotations.HasComponent;
import de.tum.ei.lkn.eces.core.annotations.HasComponents;
import de.tum.ei.lkn.eces.core.annotations.HasNotComponent;
import de.tum.ei.lkn.eces.core.annotations.HasNotComponents;
import de.tum.ei.lkn.eces.core.exceptions.ControllerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Class representing a method listening to ComponentStatus changes.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
class Listener {
	/**
	 * The method.
	 */
	private Method method;

	/**
	 * The Object on which the method has to be run.
	 */
	private Object object;

	/**
	 * Filters filtering Components that may trigger the listener.
	 */
	private Filter[] filters;

	/**
	 * Controller responsible for the listener.
	 */
	private Controller controller;

	/**
	 * Creates a new listener.
	 * @param controller Controller responsible for the listener.
	 * @param object Object on which the listener method has to be executed.
	 * @param method Method to execute.
	 */
	public Listener(Controller controller, Object object, Method method) {
		super();
		this.object = object;
		this.method = method;
		this.controller = controller;

		Vector<Filter> filters = new Vector<>();
		// Add the possible HasComponent filters(s).
		if(method.isAnnotationPresent(HasComponent.class))
			filters.add(new HasComponentFilter(controller, method.getAnnotation(HasComponent.class).component()));
		else if(method.isAnnotationPresent(HasComponents.class))
			for(HasComponent filter : method.getAnnotation(HasComponents.class).value())
				filters.add(new HasComponentFilter(controller, filter.component()));

		// Add the possible HasNotComponent filters(s).
		if(method.isAnnotationPresent(HasNotComponent.class))
			filters.add(new HasNotComponentFilter(controller, method.getAnnotation(HasNotComponent.class).component()));
		else if(method.isAnnotationPresent(HasNotComponents.class))
			for(HasNotComponent filter : method.getAnnotation(HasNotComponents.class).value())
				filters.add(new HasNotComponentFilter(controller, filter.component()));

		this.filters = filters.toArray(new Filter[filters.size()]);
	}

	/**
	 * Runs the Listener.
	 * The Listener is run only if the Component is accepted by all the filters
	 * of the Listener. If run, the Listener is run in a MapperSpace.
	 * A read lock is acquired for the Component.
	 * @param component Component to give as parameter to the method.
	 */
	public void runTask(Component component) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			if(filter(component)) {
				component.getLock().readLock().lock();
				controller.getMapperData().addReadLock(component.getLock().readLock());
				method.invoke(object, component);
			}
		}catch (IllegalAccessException e) {
			throw new ControllerException("Impossible to run listener ("+ method.toString() + "; "+ component.getClass().getName() +"):\n", e.getCause());
		} catch (InvocationTargetException e) {
			throw new ControllerException("Impossible to run listener ("+ method.toString() + "; "+ component.getClass().getName() +"):\n",e.getCause());
		}
	}

	/**
	 * Helper function telling whether a given Component is accepted by all
	 * the filters.
	 * @param component Component on which to apply the filters.
	 * @return false if at least one of the filters refuses the Component, true
	 *         otherwise.
	 */
	private boolean filter(Component component) {
		for(Filter filter : filters) {
			if(!filter.isAccepted(component)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String result = "Filters: ";
		for(Filter filter : filters) {
			if(filter != null)
				result += filter.toString() + "; ";
		}

		return object.getClass().getName() + " - " + method.getName() + "- " + result;
	}
}
