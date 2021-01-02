package de.tum.ei.lkn.eces.core;

import com.google.common.base.Objects;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.core.annotations.ComponentStateIs;
import de.tum.ei.lkn.eces.core.exceptions.ControllerException;
import de.tum.ei.lkn.eces.core.exceptions.MapperException;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A Controller is responsible for the handling of events and for the triggering
 * of associated listeners.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class Controller {
	/**
	 * Default Controller that can be used.
	 */
	private final static Controller defaultController = new Controller();

	/**
	 * Logger.
	 */
	private final static Logger logger = Logger.getLogger(Controller.class);

	/**
	 * Object storing a MapperData object for each thread. This allows
	 * MapperSpaces to run in parallel.
	 */
	private final ThreadLocal<MapperData> threadLocalMemory = new ThreadLocal<>();

	/**
	 * Thread pool to which parallel jobs can be submitted.
	 */
	private ExecutorService executorService = Executors.newCachedThreadPool(
			new ThreadFactory() {
				public Thread newThread(Runnable r) {
					Thread t = Executors.defaultThreadFactory().newThread(r);
					t.setDaemon(true);
					return t;
				}
			}
	);

	/**
	 * Number of Systems registered to the Controller.
	 */
	private int numberOfSystems = 0;

	/**
	 * Number of Entity instances created by the System.
	 */
	private long numberOfEntities = 0;

	/**
	 * Map from System classes to their ID.
	 */
	private Map<Class<? extends RootSystem>, Integer> systemClassToId = new HashMap<>();

	/**
	 * Map from System ID to System classes.
	 */
	private Map<Integer, Class<? extends RootSystem>> systemIdToClass = new HashMap<>();

	/**
	 * Map from System ID to System objects.
	 */
	private Map<Integer, RootSystem> systemIdToObject = new HashMap<>();

	/**
	 * Map from System IDs to their maximum Component count.
	 */
	private Map<Integer, Integer> systemIdToMaxComponentCount = new HashMap<>();

	/**
	 * Map from Component classes to their ID within their System.
	 */
	private Map<Class<? extends Component>, Integer> componentToId = new HashMap<>();

	/**
	 * Map from Component IDs ("systemId"-"componentId") to their class.
	 */
	private Map<String, Class<? extends Component>> componentIdToClass = new HashMap<>();

	/**
	 * Map from Component classes to their Mapper.
	 */
	private Map<Class<? extends Component>, Mapper> componentClassToMapper = new HashMap<>();

	/**
	 * Maps an owner and a LocalComponent class to the index of the data of this
	 * owner in the LocalComponent.
	 *
	 * The owner and LocalComponent pair is stored as a concatenation of their
	 * String representation.
	 */
	private Map<String, Integer> ownerAndLocalComponentClassToIndex = new HashMap<>();

	/**
	 * Maps a LocalComponent class to the next index to use for a hypothetical
	 * new data instance.
	 */
	private Map<Class<? extends LocalComponent>, Integer> localComponentClassToNextIndex = new HashMap<>();

	/**
	 * Set of Systems registered to the Controller.
	 */
	private Set<Object> registeredSystems = new HashSet<>();

	/**
	 * List of listeners to ComponentStateIs.New events, sorted by Component
	 * type.
	 */
	private final SetMultimap<Class<? extends Component>, Listener> listenersToNewEvents = HashMultimap.create();

	/**
	 * List of listeners to ComponentStateIs.Updated events, sorted by Component
	 * type.
	 */
	private final SetMultimap<Class<? extends Component>, Listener> listenersToUpdatedEvents = HashMultimap.create();

	/**
	 * List of listeners to ComponentStateIs.Deleted events, sorted by Component
	 * type.
	 */
	private final SetMultimap<Class<? extends Component>, Listener> listenersToDeletedEvents = HashMultimap.create();

	/**
	 * Gets the default Controller. The Controller returned is always the same
	 * instance.
	 * @return default Controller.
	 */
	public static Controller getDefaultController() {
		return defaultController;
	}


	public void close(){
		executorService.shutdown();
	}

	/**
	 * Gets the MapperData for current thread.
	 * @return The MapperData object.
	 */
	protected MapperData getMapperData() {
		return threadLocalMemory.get();
	}

	/**
	 * Starts a MapperSpace.
	 * All Mapper methods executed inside a MapperSpace will be pushed
	 * to the Event-Based Component Entity System (ECES) when the Mapper
	 * Space will be closed.
	 * Hence, a MapperSpace must always be closed for the Mapper actions
	 * inside to be taken into account and so that the corresponding events
	 * can be triggered and hence the listeners to these events executed
	 * (event-based system).
	 *
	 * Note that a MapperSpace implements the AutoCloseable interface and
	 * can therefore be used with the Java try-with-resources statement so
	 * that the MapperSpace is closed automatically.
	 *
	 * @return the started Mapper Space.
	 */
	public MapperSpace startMapperSpace() {
		// Getting the data of current thread.
		MapperData data = threadLocalMemory.get();
		if(data != null) {
			/* MapperSpace is already started in this thread so no need to
			 * start it and neither to close it. */
			return new MapperSpace() {
				@Override
				public String toString() {
					return "Already in a MapperSpace";
				}

				@Override
				public void close() {}
			};
		} else {
			/* No Mapper Space yet. We create new data for it and we define how
			 * it will be closed. */
			threadLocalMemory.set(new MapperData());
			logger.debug("Mapper Space started (thread: " + Thread.currentThread().getId() + ")");
			return new MapperSpace() {
				@Override
				public String toString() {
					return "New MapperSpace started";
				}

				@Override
				public void close() {
					try {
						logger.debug( "Closing Mapper Space (thread: " + Thread.currentThread().getId() + ").");

						MapperData data = threadLocalMemory.get();
						data.startWritePhase();
						// Releasing read locks acquired by the MapperSpace.
						for(ReentrantReadWriteLock.ReadLock readlock : data.getReadLocks())
							readlock.unlock();
						data.removeReadLocks();

						/* Because users are supposed to first delete Components,
						 * then create some Components and finally update some
						 * Components, we process the operations in the following
						 * order: detach, attach, update. */
						logger.debug( "Processing detachments (thread: " + Thread.currentThread().getId() + ").");
						detachComponents(data);
						logger.debug( "Processing attachments (thread: " + Thread.currentThread().getId() + ").");
						attachComponents(data);
						logger.debug( "Processing updates (thread: " + Thread.currentThread().getId() + ").");
						updateComponents(data);
						logger.debug( "Processing update listeners (thread: " + Thread.currentThread().getId() + ").");
						processUpdateListeners(data);
						logger.debug( "Processing attachment listeners (thread: " + Thread.currentThread().getId() + ").");
						processAttachmentListeners(data);
						logger.debug( "Processing detachment listeners (thread: " + Thread.currentThread().getId() + ").");
						processDetachmentListeners(data);

						data.stopWritePhase();
					} finally {
						/* We finally remove the MapperData of this thread so that
					 	* another MapperSpace can be opened later. */
						resetThreadLocal();
						logger.debug( "Mapper Space closed (thread: " + Thread.currentThread().getId() + ").");
					}
				}
			};
		}
	}

	/**
	 * Resets the data of all Threads.
	 */
	public void resetThreadLocal() {
		threadLocalMemory.remove();
	}

	/**
	 * Helper method running the detachment jobs.
	 */
	private void detachComponents(MapperData data) {
		for(Runnable job : data.getEntityDetachmentJobs())
			job.run();
	}

	/**
	 * Helper method running the attachment jobs.
	 */
	private void attachComponents(MapperData data) {
		List<Callable<Boolean>> jobList = data.getEntityAttachmentJobs();
		do{
			List<Callable<Boolean>> blockedList = new LinkedList<>();
			boolean success = false;
			for(Callable<Boolean> job : jobList) {
				try {
					if(!job.call()) {
						blockedList.add(job);
					}else{
						success = true;
					}
				} catch (Exception e) {
					throw new MapperException(e.getMessage());
				}
			}
			if(!success & !jobList.isEmpty())
				throw new MapperException("There was a ring relation of components which are not attached to an entity");
			jobList = blockedList;
		} while (!jobList.isEmpty());

	}

	/**
	 * Helper method running the update jobs.
	 */
	private void updateComponents(MapperData data) {
		for(Map.Entry<Component, List<Runnable>> job : data.getComponentUpdateJobs().entrySet()) {
			/* Update jobs are sorted per Component so that we only
			 * lock each Component once. Indeed, we need to lock a
			 * Component before updating it. */
			job.getKey().getLock().writeLock().lock();
			try {
				for(Runnable task : job.getValue())
					task.run();

				if(data.getReadLocks().size() > 0) {
					/* Throwing an exception inside a MapperSpace will exit the
					 * MapperSpace without closing it properly. We therefore
					 * have to reset the Thread data to manually close it.
					 * Note: this is kind of us because the exception thrown
					 * should anyway stop the user's program. */
					resetThreadLocal();
					throw new ControllerException("A read lock should not be acquired within an update job because of deadlock risk.");
				}
			} finally {
				job.getKey().getLock().writeLock().unlock();
			}
		}
	}

	/**
	 * Helper method calling all the detachment event listeners listed in a
	 * MapperData object.
	 * @param data MapperData object containing the jobs to execute.
	 */
	@SuppressWarnings("unchecked")
	private void processDetachmentListeners(MapperData data) {
		Vector <Future<Object>> futures = new Vector<>();

		// Submitting all the jobs to the thread pool.
		for(Runnable item : data.getDetachmentListeners())
			futures.add((Future<Object>) executorService.submit(item));

		for(Future<Object> item : futures) {
			try {
				// Will only return when the job is finished (kind of join())
				item.get();
			} catch (InterruptedException e) {
				logger.error("Detachment listeners interrupted.", e);
			} catch (ExecutionException e) {
				logger.error( "Execution exception in detachment listeners.", e);
			}
		}
	}

	/**
	 * Helper method calling all the attachment event listeners listed in a
	 * MapperData object.
	 * @param data MapperData object containing the jobs to execute.
	 */
	@SuppressWarnings("unchecked")
	private void processAttachmentListeners(MapperData data) {
		Vector <Future<Object>> futures = new Vector<>();

		// Submitting all the jobs to the thread pool.
		for(Runnable item : data.getAttachmentListeners())
			futures.add((Future<Object>) executorService.submit(item));

		for(Future<Object> item : futures) {
			try {
				// Will only return when the job is finished (kind of join())
				item.get();
			} catch (InterruptedException e) {
				logger.error( "Attachment listeners interrupted.", e);
			} catch (ExecutionException e) {
				logger.error( "Execution exception in attachment listeners.", e);
			}
		}
	}

	/**
	 * Helper method calling all the attachment event listeners listed in a
	 * MapperData object.
	 * @param data MapperData object containing the jobs to execute.
	 */
	@SuppressWarnings("unchecked")
	private void processUpdateListeners(MapperData data) {
		Vector <Future<Object>> futures = new Vector<>();

		// Submitting all the jobs to the thread pool.
		for(Runnable item : data.getUpdateListeners())
			futures.add((Future<Object>) executorService.submit(item));

		for(Future<Object> item : futures) {
			try {
				// Will only return when the job is finished (kind of join())
				item.get();
			} catch (InterruptedException e) {
				logger.error( "Update listeners interrupted.", e);
			} catch (ExecutionException e) {
				logger.error( "Execution exception in update listeners.", e);
			}
		}
	}

	/**
	 * Gets a new LocalMapper.
	 * @param owner Object using the instance of data handled by the newly
	 *              created LocalMapper.
	 * @param componentClassType Class of the LocalComponent to handle.
	 * @param <C> Class used to store data in the LocalComponent.
	 * @return The created LocalMapper.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <C> LocalMapper<C> getLocalMapper(Object owner, Class<? extends LocalComponent> componentClassType) {
		// We have to find the index to use in the LocalMapper for this owner.
		if(!ownerAndLocalComponentClassToIndex.containsKey(Integer.toHexString(owner.hashCode()) + componentClassType.toString())) {
			if(!localComponentClassToNextIndex.containsKey(componentClassType))
				localComponentClassToNextIndex.put(componentClassType, 0);
			int nextIndex = localComponentClassToNextIndex.get(componentClassType);
			ownerAndLocalComponentClassToIndex.put(Integer.toHexString(owner.hashCode()) + componentClassType.toString(), nextIndex);
			localComponentClassToNextIndex.put(componentClassType, ++nextIndex);
		}
		return new LocalMapper(this, componentClassType, ownerAndLocalComponentClassToIndex.get(Integer.toHexString(owner.hashCode()) + componentClassType.toString()));
	}

	/**
	 * Gets a new Mapper.
	 * @param componentClassType Component class the Mapper has to handle.
	 * @param <C> Class the mapper has to handle.
	 * @return The new Mapper.
	 */
	@SuppressWarnings({"unchecked", "rawtypes" })
	public <C extends Component> Mapper<C> getMapper(Class componentClassType) {
		if(!componentClassToMapper.containsKey(componentClassType)) {
			/* Get system/component ID of provided class type.
			 * The class for which we get the system/component ID is the first
			 * parent which is a Component of the System (true parameter for
			 * getClassHierarchy) which could be the provided class itself. */
			List<Class<?>> classList = getClassHierarchy(componentClassType, true);
			int systemIdentifier = getSystemId((Class<? extends Component>) classList.get(0));
			Integer componentId = componentToId.get(classList.get(0));
			if(componentId == null) {
				Integer maxCount = systemIdToMaxComponentCount.getOrDefault(systemIdentifier, 0);
				componentToId.put((Class<? extends Component>) classList.get(0), maxCount);
				componentIdToClass.put(systemIdentifier + "-" + maxCount, (Class<? extends Component>) classList.get(0));
				systemIdToMaxComponentCount.put(systemIdentifier, ++maxCount);
				componentId = componentToId.get(classList.get(0));
			}
			int componentIdentifier = componentId;
			componentClassToMapper.put(componentClassType, new Mapper(this, systemIdentifier, componentIdentifier, componentClassType));
		}
		return componentClassToMapper.get(componentClassType);
	}

	/**
	 * Creates a new Entity.
	 * @return The created Entity.
	 */
	public Entity createEntity() {
		return new Entity(numberOfEntities++, numberOfSystems);
	}

	/**
	 * Gets the number of Systems registered to the Controller.
	 * @return number of Systems.
	 */
	public int getNumberOfSystems() {
		return this.numberOfSystems;
	}

	/**
	 * Registers a System to the Controller.
	 * @param system instance of the System to register.
	 * @throws ControllerException if one tries to register a System which is
	 *                             already registered.
	 */
	protected void registerSystem(RootSystem system) {
		if(registeredSystems.contains(system.getClass()))
			throw new ControllerException("Tried to register a System (" + system.getClass() + ") which is already registered.");

		// Add all the listeners of the System.
		listenersToNewEvents.putAll(findAllListeners(system, ComponentStatus.New));
		listenersToUpdatedEvents.putAll(findAllListeners(system, ComponentStatus.Updated));
		listenersToDeletedEvents.putAll(findAllListeners(system, ComponentStatus.Destroyed));
		if(!systemClassToId.containsKey(system.getClass())) {
			systemClassToId.put(system.getClass(), numberOfSystems);
			systemIdToObject.put(numberOfSystems, system);
			systemIdToClass.put(numberOfSystems++, system.getClass());
		}
		else {
			int sysID = systemClassToId.get(system.getClass());
			systemIdToObject.put(sysID, system);
		}

		registeredSystems.add(system.getClass());

		logger.debug("New System " + system + " registered to " + this + ".");
	}

	/**
	 * Runs all the methods listening to creations of a given Component type.
	 * @param component Component instance given to the listener method as
	 *                  parameter.
	 */
	protected void runAttachmentListeners(Component component) {
		for(Listener task : getTasks(component, listenersToNewEvents))
			task.runTask(component);
	}

	/**
	 * Runs all the methods listening to deletions of a given Component type.
	 * @param component Component instance given to the listener method as
	 *                  parameter.
	 */
	protected void runDetachmentListeners(Component component) {
		for(Listener task : getTasks(component, listenersToDeletedEvents))
			task.runTask(component);
	}

	/**
	 * Runs all the methods listening to updates of a given Component type.
	 * @param component Component instance given to the listener method as
	 *                  parameter.
	 */
	protected void runUpdateListeners(Component component) {
		for(Listener task : getTasks(component, listenersToUpdatedEvents))
			task.runTask(component);
	}

	/**
	 * Gets the ID of the System to which a Component class belongs.
	 * @param componentClass Component class.
	 * @return The ID.
	 * @throws ControllerException if the Component's class has not one single
	 *                             ComponentBelongsTo annotation.
	 */
	@SuppressWarnings("unchecked")
	protected int getSystemId(Class<? extends Component> componentClass) {
		ComponentBelongsTo[] systemClass = componentClass.getAnnotationsByType(ComponentBelongsTo.class);
		if(systemClass.length != 1)
			throw new ControllerException(componentClass.getName() + " should have only one @ComponentBelongsTo annotation.");

		Class system = systemClass[0].system();
		Integer systemId = systemClassToId.get(system);
		if(systemId == null) {
			if(!systemClassToId.containsKey(system.getClass())) {
				systemClassToId.put(system, numberOfSystems);
				systemIdToClass.put(numberOfSystems++, system);
			}
			systemId = systemClassToId.get(system);
		}

		return systemId;
	}

	/**
	 * Gets the maximum number of Component classes for a System.
	 * @param systemId Identifier of the System.
	 * @return Max number of Component classes.
	 * @throws ControllerException if there is no maximum component count for
	 *                             this System ID.
	 */
	protected int getMaximumComponentCount(int systemId) {
		try {
			return systemIdToMaxComponentCount.get(systemId);
		} catch(NullPointerException e) {
			throw new ControllerException("There is no max component count for this System ID.");
		}
	}

	/**
	 * Gets the System ID of a System class.
	 * @param clazz System class.
	 * @return System ID.
	 * @throws ControllerException if the class is not known.
	 */
	protected int getSystemIdentifier(Class<? extends RootSystem> clazz) {
		try {
			return systemClassToId.get(clazz);
		} catch(NullPointerException e) {
			throw new ControllerException("Class " + clazz + " of system is not known.");
		}
	}

	/**
	 * Gets the System class corresponding to a System ID.
	 * @param  systemID The system Id.
	 * @return System class.
	 * @throws ControllerException if id is not known.
	 */
	protected Class<? extends RootSystem> getSystemClass(int systemID) {
		Class<? extends RootSystem> clazz = systemIdToClass.get(systemID);
		if(clazz != null)
			return clazz;
		throw new ControllerException("SystemID " + systemID + " is not known.");
	}

	/**
	 * Gets the System object corresponding to as System ID.
	 * @param systemID The system Id.
	 * @return System object or null if system is registered but the
	 * class is not known.
	 * @throws ControllerException if id is not known.
	 */
	protected RootSystem getSystemObject(int systemID) {
		RootSystem system = systemIdToObject.get(systemID);
		if(system != null)
			return system;

		/* If object does not exist, maybe the class is registered.
		 * In this case, we return null. If the class is also not
		 * registered, we throw the exception. */
		if(this.getSystemClass(systemID) != null)
			return null;
		throw new ControllerException("SystemID " + systemID + " is not known.");
	}

	/**
	 * Gets the Component class corresponding to a System ID and Component ID.
	 * @param  systemID The System ID.
	 * @param  componentID The Component ID.
	 * @return Component class.
	 * @throws ControllerException if IDs are not known.
	 */
	protected Class<? extends Component> getComponentClass(int systemID, int componentID) {
		Class<? extends Component> clazz = componentIdToClass.get(systemID + "-" + componentID);
		if(clazz != null)
			return clazz;
		throw new ControllerException("Component ID " + componentID + " is not known in system ID " + systemID + ".");
	}

	/**
	 * Helper function getting a list of jobs corresponding to a given
	 * Component's class and parent classes from a list of jobs corresponding
	 * to many different classes.
	 * @param component Subject Component.
	 * @param jobs Initial job Map.
	 * @return Set of Listeners.
	 */
	private Set<Listener> getTasks(Component component, SetMultimap<Class<? extends Component>, Listener> jobs) {
		Set<Listener> tasks = new HashSet<>();
		for(Class clazz : getClassHierarchy(component.getClass(), false)) {
			//noinspection unchecked
			tasks.addAll(jobs.get(clazz));
		}
		tasks.addAll(jobs.get(Component.class));
		return tasks;
	}

	/**
	 * Finds all listeners in a class for a given ComponentStatus.
	 * @param listenerClass Instance of the class to parse.
	 * @param status ComponentStatus to find.
	 * @return Map from Component type to a set of Listeners. The Component type
	 *         corresponds to the type of Component to which the Listener
	 *         listens and that has to be given as parameter to the Listener.
	 */
	protected Multimap<Class<? extends Component>, Listener> findAllListeners(Object listenerClass, ComponentStatus status) {
		Multimap<Class<? extends Component>, Listener> listeners = HashMultimap.create();
		Class<?> clazz = listenerClass.getClass();
		for(Method method : getAnnotatedMethods(clazz)) {
			ComponentStatus methodState = method.getAnnotation(ComponentStateIs.class).State();
			if(methodState == status || methodState == ComponentStatus.Any) {
				Class<?>[] parameterTypes = method.getParameterTypes();
				@SuppressWarnings("unchecked")
				Class<? extends Component> componentType = (Class<? extends Component>) parameterTypes[0];
				listeners.put(componentType, new Listener(this, listenerClass, method));
			}
		}
		return listeners;
	}

	/**
	 * Gets a list of methods annotated with the @ComponentStateIs annotation
	 * within a class and its parents.
	 * @param clazz class in which and in whose parents the search has to be
	 *              performed.
	 * @return List of the methods.
	 */
	protected ImmutableList<Method> getAnnotatedMethods(Class<?> clazz) {
		// Get parent of the class, and class itself.
		Set<? extends Class<?>> supers = TypeToken.of(clazz).getTypes().rawTypes();

		// List of Methods identified by an identifier.
		Map<MethodIdentifier, Method> identifiers = Maps.newHashMap();

		// Going through all methods of the class and its parents.
		for(Class<?> superClass : supers) {
			for(Method superClassMethod : superClass.getMethods()) {
				if(superClassMethod.isAnnotationPresent(ComponentStateIs.class) && !superClassMethod.isBridge()) {
					Class<?>[] parameterTypes = superClassMethod.getParameterTypes();
					if(parameterTypes.length != 1) {
						logger.warn("Method " + superClassMethod.getName() + " has @ComponentStateIs annotation which requires 1 argument, but has " + parameterTypes.length + " arguments. Method ignored.");
						continue;
					} else if(!Component.class.isAssignableFrom(parameterTypes[0])) {
						logger.warn("Method " + superClassMethod.getName() + " has @ComponentStateIs annotation which requires argument of type Component, but argument has type " + parameterTypes[0] + ". Method ignored.");
						continue;
					}
					// Add method only if not yet in the Map.
					MethodIdentifier identifier = new MethodIdentifier(superClassMethod);
					if(!identifiers.containsKey(identifier))
						identifiers.put(identifier, superClassMethod);
				}
			}
		}

		return ImmutableList.copyOf(identifiers.values());
	}

	/**
	 * Gets a list of the parent classes of a given class. The includes the
	 * class itself. The list stops at the [Local]Component class (which is not
	 * included).
	 * @param childClass starting Class.
	 * @param filter If false, gets the whole class hierarchy. If true, gets only
	 *               the classes with the "ComponentsBelongTo" annotation.
	 * @return List of the parent classes.
	 */
	protected List<Class<?>> getClassHierarchy(Class<? extends Component> childClass, boolean filter) {
		Class<?> clazz = childClass;
		List<Class<?>> list = new LinkedList<>();
		while(clazz != Component.class && clazz != LocalComponent.class	&& Component.class.isAssignableFrom(clazz)) {
			if(clazz.isAnnotationPresent(ComponentBelongsTo.class) || !filter)
				list.add(clazz);
			clazz = clazz.getSuperclass();
		}

		return list;
	}
}

/**
 * Helper class allowing to uniquely identify a method.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
final class MethodIdentifier {
	/**
	 * Name of the method.
	 */
    private final String name;

	/**
	 * List of the parameter types of the method.
	 */
    private final List<Class<?>> parameterTypes;

	/**
	 * Creates the identifier of a method.
	 * @param method the method whose MethodIdentifier must be created.
	 */
    MethodIdentifier(Method method) {
    	this.name = method.getName();
    	this.parameterTypes = Arrays.asList(method.getParameterTypes());
    }

    @Override
    public int hashCode() {
    	return Objects.hashCode(name, parameterTypes);
    }

    @Override
    public boolean equals(Object other) {
    	if(other instanceof MethodIdentifier) {
    		MethodIdentifier identifier = (MethodIdentifier) other;
    		return name.equals(identifier.name) && parameterTypes.equals(identifier.parameterTypes);
    	}
    	return false;
    }
}
