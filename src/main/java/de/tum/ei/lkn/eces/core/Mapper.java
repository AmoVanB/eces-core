package de.tum.ei.lkn.eces.core;

import de.tum.ei.lkn.eces.core.exceptions.ControllerException;
import de.tum.ei.lkn.eces.core.exceptions.MapperException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;

/**
 * Class representing a Mapper for a given Component type.
 *
 * The Mapper is the object hiding data structures and synchronization
 * issues from the user. A user willing to create, delete, update Components
 * from Entities has to do this using a Mapper instantiated with the class of
 * the Components the user is willing to interact with.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class Mapper<C extends Component> {
	/**
	 * Logger.
	 */
	private final static Logger logger = Logger.getLogger(Mapper.class);

	/**
	 * Class of the Component types the Mapper is handling.
	 */
	private Class<C> componentClassType;

	/**
	 * System and Component IDs of the Component class handled by
	 * the Mapper.
	 */
	private int systemIdentifier;
	private int componentIdentifier;

	/**
	 * Controller responsible for handling the Mapper.
	 */
	private Controller controller;

	/**
	 * Creates a new Mapper.
	 * @param controller Controller handling the Mapper.
	 * @param systemIdentifier System ID of the Component class handled by the mapper.
	 * @param componentIdentifier Component ID of the Component class handled by the mapper.
	 * @param componentClassType Component class handled by the mapper.
	 */
	protected Mapper(Controller controller, int systemIdentifier, int componentIdentifier, Class<C> componentClassType) {
		super();
		this.controller = controller;
		this.setIdentifiers(systemIdentifier, componentIdentifier);
		this.componentClassType = componentClassType;
	}

	/**
	 * Creates a new Mapper.
	 * To be used by classes extending Mapper&lt;C&gt; such that 'C' is automatically
	 * inferred. There, 'C' will be automatically inferred.
	 * @param controller Controller handling the Mapper.
	 */
	protected Mapper(Controller controller) {
			this.controller = controller;
			componentClassType = this.getTypeClass();
			Mapper<C> mapper = controller.getMapper(componentClassType);
			this.setIdentifiers(mapper.systemIdentifier, mapper.componentIdentifier);
	}

	/**
	 * Gets the type to which the Mapper has been set when created.
	 * @return Class type.
	 */
	private Class<C> getTypeClass() {
		String classname = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
		try {
			return (Class<C>) Class.forName(classname);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Sets the system and components identifiers.
	 * @param systemIdentifier System IDs.
	 * @param componentIdentifier Component IDs.
	 */
	private void setIdentifiers(int systemIdentifier, int componentIdentifier) {
		this.systemIdentifier = systemIdentifier;
		this.componentIdentifier = componentIdentifier;
	}

	/**
	 * Gets the class of the Component the Mapper is responsible for.
	 * @return Class object representing the class handled by the Mapper.
	 */
	public Class<C> getType() {
		return this.componentClassType;
	}

	/**
	 * Gets the Component attached to a given Entity and for which the current
	 * Mapper is responsible.
	 * A read lock is acquired on the Component and the operation is executed
	 * in a MapperSpace (if not already done by the user of the method).
	 * @param entity the Entity.
	 * @return the Component or null if the Entity does not contain a Component
	 *         of the specific type.
	 */
	@SuppressWarnings("unchecked")
	public C get(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			Component c = entity.getComponent(systemIdentifier, componentIdentifier);
			if(c == null)
				return null;
			if(!(this.componentClassType.isInstance(c)))
				return null;
			this.acquireReadLock(c);
			return (C) c;
		}
	}

	/**
	 * Gets the Component attached to a given Entity and for which the current
	 * Mapper is responsible.
	 * If the Entity has no such Component, it will wait until such a Component
	 * is attached to the Entity.
	 * A read lock is acquired for the Component and the operation is executed
	 * in a MapperSpace (if not already done by the user of the method).
	 * @param entity the Entity.
	 * @return the Component or null in case of error.
	 * @throws MapperException if the method got interrupted while waiting.
	 */
	@SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
	public C getWait(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			try {
				synchronized(entity) {
					C c = (C) entity.getComponent(systemIdentifier, componentIdentifier);
					while(c == null) {
						entity.wait();
						c = (C) entity.getComponent(systemIdentifier, componentIdentifier);
					}
					this.acquireReadLock(c);
					return c;
				}
			} catch(InterruptedException e) {
				throw new MapperException("Interrupted while waiting to get Component of an Entity: " + e);
			}
		}
	}

	/**
	 * Gets the Component attached to a given Entity and for which the current
	 * Mapper is responsible.
	 * No read lock is acquired for the Component and the operation is not
	 * executed in a MapperSpace.
	 * @param entity the Entity.
	 * @return the Component or null if the Entity does not contain a Component
	 * of the specific type.
	 */
	@SuppressWarnings("unchecked")
	public C getOptimistic(Entity entity) {
		Component c = entity.getComponent(systemIdentifier, componentIdentifier);
		if(c == null)
			return null;
		if(!this.componentClassType.isInstance(c))
			return null;
		return (C) c;

	}

	/**
	 * Checks whether or not the Components entity contains a Component for which the
	 * Mapper is responsible.
	 * A read lock is acquired for the Component and the operation is executed
	 * in a MapperSpace (if not already done by the user of the method).
	 * @param component the Component.
	 * @return true/false based on result.
	 */
	public boolean isIn(Component component) {
		if(component.getEntity() == null)
			return false;
		return isIn(component.getEntity());
	}

	/**
	 * Checks whether or not the Entity contains a Component for which the
	 * Mapper is responsible.
	 * A read lock is acquired for the Component and the operation is executed
	 * in a MapperSpace (if not already done by the user of the method).
	 * @param entity the Entity.
	 * @return true/false based on result.
	 */
	public boolean isIn(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			if(entity.hasComponent(systemIdentifier, componentIdentifier)) {
				Component c = entity.getComponent(systemIdentifier, componentIdentifier);
				if(!this.componentClassType.isInstance(c))
					return false;
				this.acquireReadLock(c);
				return true;
			}
			return false;
		}
	}

	/**
	 * Attaches a Component to an Entity.
	 * The operation is executed in a MapperSpace.
	 * @param entity The Entity to which the Component should be attached.
	 * @param component Component to attach.
	 */
	public void attachComponent(Entity entity, C component) {
		attachComponentPrivate(entity, component);
	}

	/**
	 * Helper method to attach a Component to the Entity of another Component.
	 * The operation is executed in a MapperSpace.
	 * If the Component to add is not of the basic type of the Mapper, the
	 * job is delegated to the corresponding Mapper.
	 * @param entity The Entity to which the Component should be attached.
	 * @param component Component to attach.
	 */
	private void attachComponentPrivate(Entity entity, Component component) {
		if(component.getClass() == this.componentClassType) {
			try (MapperSpace ms = controller.startMapperSpace()) {
				controller.getMapperData().addEntityAttachmentJob(()->{
					component.getLock().writeLock().lock();
					attachComponentOptimistic(entity, component);
					component.getLock().writeLock().unlock();
					return true;
				});
				logger.trace("Entity attachment job added (" + component + " to " + entity + ").");

				/* Finally, the listeners job list consists in asking the
				 * Controller to execute all the listener jobs. */
				controller.getMapperData().addAttachmentListener(()->controller.runAttachmentListeners(component));
			}
		} else {
			logger.trace("Component (" + component + ") attachment task delegated by " + this.componentClassType + " Mapper to " + component.getClass() + " Mapper.");
			controller.getMapper(component.getClass()).attachComponentPrivate(entity, component);
		}
	}

	/**
	 * Attaches a Component to the Entity of another Component.
	 * The operation is executed in a MapperSpace. If the Component has no
	 * Entity, we wait for it.
	 * @param component Component whose Entity is the destination of the new
	 *                  Component.
	 * @param newComponent Component to attach to the Entity of 'component'.
	 */
	public void attachComponent(Component component, C newComponent) {
		attachComponentPrivate(component, newComponent);
	}

	/**
	 * Helper method to attach a Component to the Entity of another Component.
	 * The operation is executed in a MapperSpace. If the Component has no
	 * Entity, we wait for it.
	 * If the Component to add is not of the basic type of the Mapper, the
	 * job is delegated to the corresponding Mapper.
	 * @param component Component whose Entity is the destination of the new
	 *                  Component.
	 * @param newComponent Component to attach to the Entity of 'component'.
	 */
	private void attachComponentPrivate(Component component, Component newComponent) {
		if(newComponent.getClass() == this.componentClassType) {
			try(MapperSpace ms = controller.startMapperSpace()) {
				controller.getMapperData().addEntityAttachmentJob(()->{
					Entity entity = component.getEntity();
					if(entity == null)
						return false;
					newComponent.getLock().writeLock().lock();
					attachComponentOptimistic(entity, newComponent);
					newComponent.getLock().writeLock().unlock();
					return true;
				});
				logger.trace("Component attachment job added (" + newComponent + " to " + component + "'s Entity).");

				/* Finally, the listeners job list consists in asking the
				 * Controller to execute all the listener jobs. */
				controller.getMapperData().addAttachmentListener(()->controller.runAttachmentListeners(newComponent));
			}
		}
		else {
			logger.trace("Component (" + newComponent + ") attachment task delegated by " + this.componentClassType + " Mapper to " + newComponent.getClass() + " Mapper.");
			controller.getMapper(newComponent.getClass()).attachComponentPrivate(component, newComponent);
		}
	}

	/**
	 * Attaches a Component to an Entity.
	 * The operation is not executed in a MapperSpace. Hence, the attachment
	 * does not trigger any listener.
	 * @param entity The Entity to which the Component should be attached.
	 * @param component Component to attach.
	 * @throws MapperException if such a Component type is already present on
	 *                         this Entity.
	 */
	protected void attachComponentOptimistic(Entity entity, Component component) {
		/* Add the Component at all the System/Component pairs handled by
		 * the Mapper. */
		try {
			if(!entity.hasComponent(systemIdentifier, componentIdentifier))
				entity.setComponent(component, systemIdentifier, componentIdentifier);
			else
				throw new MapperException("Could not attach Component " + component.getClass().getName() + " because Component " + entity.getComponent(systemIdentifier, componentIdentifier).getClass().getName() + " is already there.");
		} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
				/* Extend the Entity data structure if a pair is out of the
				 * bounds of the Entity. */
			entity.extend(systemIdentifier, controller.getNumberOfSystems(), controller.getMaximumComponentCount(systemIdentifier));
			if(!entity.hasComponent(systemIdentifier, componentIdentifier))
				entity.setComponent(component, systemIdentifier, componentIdentifier);
			else
				throw new MapperException("Could not attach Component " + component.getClass().getName() + " because Component " + entity.getComponent(systemIdentifier, componentIdentifier).getClass().getName() + " is already there.");
		}
		logger.trace("Component '" + component + "' attached to Entity '" + entity + "' (ID: " + entity.getId() + ").");
	}

	/**
	 * Detaches from an Entity the Component for which the Mapper is
	 * responsible.
	 * The operation is executed in a MapperSpace.
	 * @param entity The Entity from which the Component should be detached.
	 * @return the removed Component or null if the Entity contains no
	 *         Component for which the Mapper is responsible.
	 */
	@SuppressWarnings("unchecked")
	public C detachComponent(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			if(this.isIn(entity)) {
				C comp = this.get(entity);
				if(comp.getClass() == this.componentClassType) {
					/* The listener job list consists in asking the Controller
				     * to execute all the listeners jobs. */
					controller.getMapperData().addDetachmentListener(()->controller.runDetachmentListeners(comp));

					controller.getMapperData().addEntityDetachmentJob(()->{
						comp.getLock().writeLock().lock();
						entity.removeComponent(systemIdentifier, componentIdentifier);
						comp.getLock().writeLock().unlock();
					});
					logger.trace("Component detachment job (" + comp + " from " + entity + ") added.");
					return comp;
				}
				else {
					/* If the Component is a child class of the type handled by
					 * the Mapper, job is delegated to the Mapper for this child
					 * class. */
					logger.trace("Component (" + comp + ") detachment task delegated by " + this.componentClassType + " Mapper to " + comp.getClass() + " Mapper.");
					return (C) controller.getMapper(comp.getClass()).detachComponent(entity);
				}
			}
			return null;
		}
	}

	/**
	 * Detaches from the Entity of a Component the Component for which the
	 * Mapper is responsible.
	 * The operation is executed in a MapperSpace.
	 * @param component Component from whose Entity the other Component has to
	 *                  be removed.
	 * @return the removed Component or null if the Entity of comp contains no
	 *         Component for which the Mapper is responsible.
	 */
	public C detachComponent(Component component) {
		return this.detachComponent(component.getEntity());
	}

	/**
	 * Updates a Component.
	 * The operation is executed in a MapperSpace.
	 * @param component Component to be updated.
	 * @param run Runnable updating the Component.
	 */
	public void updateComponent(Component component, Runnable run) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			logger.trace("Component update job ("+ run + " on " + component + ") added.");
			controller.getMapperData().addComponentUpdateJob(component, run);
			controller.getMapperData().addUpdateListener(()->controller.runUpdateListeners(component));
		}
	}

	/**
	 * Method acquiring a read lock for a Component.
	 * The lock is added to the list of read locks of the MapperSpace.
	 * Hence, this must only be used within a MapperSpace (otherwise it makes
	 * no sense).
	 * @param component Component to lock.
	 * @throws ControllerException if the method is called within a write lock
	 *                             phase, i.e. within an update job (because of
	 *                             deadlock risk).
	 */
	public void acquireReadLock(Component component) {
		MapperData mapperData = controller.getMapperData();
		if(mapperData != null) {
			if(mapperData.isWritePhase()) {
				/* Throwing an exception inside a MapperSpace will exit the
				* MapperSpace without closing it properly. We therefore
				* have to reset the Thread data to manually close it.
				* Note: this is kind of us because the exception thrown
				* should anyway stop the user's program. */
				controller.resetThreadLocal();
				throw new MapperException("A read lock should not be acquired within a write phase (update job) because of deadlock risk.");
			}

			if(component.getLock().getReadHoldCount() == 0) {
				component.getLock().readLock().lock();
				controller.getMapperData().addReadLock(component.getLock().readLock());
				logger.trace("Read lock acquired on " + component + ".");
			}
		} else {
			throw new MapperException("The use of the locking mechanism works only within a MapperSpace.");
		}
	}

	/**
	 * Creates a JSON Object out of an Entity. The JSON Object lists all the
	 * Systems known by the Entity, and for each of them, the Components
	 * attached to the Entity.
	 * @param entity Entity ID.
	 * @return The created JSON Object.
	 */
	public synchronized JSONObject createJSONObject(Entity entity) {
		return JSONUtil.createJSONObject(controller,entity);
	}
}

