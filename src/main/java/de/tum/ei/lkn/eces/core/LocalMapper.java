package de.tum.ei.lkn.eces.core;

import de.tum.ei.lkn.eces.core.exceptions.MapperException;

import java.lang.reflect.InvocationTargetException;

 /**
 * Object for handling a LocalComponent.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class LocalMapper<C> {
	/**
	 * Underlying Mapper for the LocalComponent.
	 */
	private Mapper<? extends LocalComponent> mapper;

	/**
	 * Class of the Component types the Mapper is handling.
	 */
	private Class<? extends LocalComponent> componentClassType;

	/**
	 * Index of the data instance the LocalMapper is handling.
	 */
	private int index;

	/**
	 * Creates a LocalMapper.
	 * @param controller Controller responsible for handling the LocalMapper.
	 * @param componentClassType Class of the Component the LocalMapper should
	 *                           handle.
	 * @param index Index of the data instance the LocalMapper is responsible
	 *              for.
	 */
	public LocalMapper(Controller controller, Class<? extends LocalComponent> componentClassType, int index) {
		this.mapper = controller.getMapper(componentClassType);
		this.componentClassType = componentClassType;
		this.index = index;
	}

	/**
	 * Gets the index of the data instance handled by the LocalMapper.
	 * @return the index.
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Gets the data of the LocalComponent attached to a given Entity and for
	 * which the current Mapper is responsible.
	 * Since the LocalComponent is local to a given owner, no lock and no
	 * MapperSpace is needed nor used.
	 * If the Entity does not contain a LocalComponent, such a LocalComponent
	 * is created, attached to the Entity and then returned.
	 * @param entity the Entity.
	 * @return the instance of the data of the LocalComponent that is handled by
	 *         the LocalMapper.
	 * @throws MapperException if the creation of the LocalComponent (in the
	 *                         case it did not exist yet) fails.
	 */
	@SuppressWarnings("unchecked")
	public C get(Entity entity) {
		LocalComponent lc = mapper.getOptimistic(entity);
		if(lc == null) {
			try {
				lc = (LocalComponent) componentClassType.getConstructors()[0].newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				throw new MapperException("Error occurred while creating LocalComponent: " + e);
			}
			mapper.attachComponentOptimistic(entity, lc);
		}

		return (C) lc.get(index);
	}
}