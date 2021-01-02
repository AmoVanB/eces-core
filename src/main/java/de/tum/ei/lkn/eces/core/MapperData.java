package de.tum.ei.lkn.eces.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

/**
 * Class containing the data of a MapperSpace.
 * This data consists of the read locks that have been acquired by the Mapper
 * Space and that have to be released when the latter is closed and of the
 * jobs that have been asked in the MapperSpace and that have to be triggered
 * when the latter is closed.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MapperData {
	/**
	 * List of all read locks acquired.
	 */
	private List<ReadLock> readLocks = new LinkedList<>();

	/**
	 * List of all jobs attaching a Component to an Entity.
	 */
	private List<Callable<Boolean>> entityAttachmentJobs = new LinkedList<>();

	/**
	 * List of all jobs detaching a Component from an Entity.
	 */
	private List<Runnable> entityDetachmentJobs = new LinkedList<>();

	/**
	 * List of all jobs updating a Component.
	 */
	private HashMap<Component, List<Runnable>> componentUpdateJobs = new HashMap<>();

	/**
	 * List of all actions to be taken due to the event triggered by all the
	 * attachment jobs.
	 */
	private List<Runnable> attachmentListeners = new LinkedList<>();
	/**
	 * List of all actions to be taken due to the event triggered by all the
	 * update jobs.
	 */
	private List<Runnable> updateListeners = new LinkedList<>();
	/**
	 * List of all actions to be taken due to the event triggered by all the
	 * detachment jobs.
	 */
	private List<Runnable> detachmentListeners = new LinkedList<>();

	/**
	 * Tells whether the MapperSpace is currently in write phase or not. A write
	 * phase means we have acquired a write lock on an Object.
	 */
	private boolean writePhase = false;

	/**
	 * Gets the list of read locks.
	 * @return List of ReadLock.
	 */
	protected List<ReadLock> getReadLocks() {
		return this.readLocks;
	}

	/**
	 * Adds a read lock to the list of read locks.
	 * @param lock read lock to add.
	 */
	protected void addReadLock(ReadLock lock) {
		this.readLocks.add(lock);
	}

	/**
	 * Empties the list of read locks.
	 */
	protected void removeReadLocks() {
		this.readLocks.clear();
	}

	/**
	 * Gets the list of Entity attachment jobs.
	 * @return List of attachment jobs.
	 */
	protected List<Callable<Boolean>> getEntityAttachmentJobs() {
		return this.entityAttachmentJobs;
	}

	/**
	 * Adds a job to the Entity attachment jobs.
	 * @param job The Runnable to be run.
	 */
	protected void addEntityAttachmentJob(Callable<Boolean> job) {
		this.entityAttachmentJobs.add(job);
	}

	/**
	 * Gets the list of Entity detachment jobs.
	 * @return List of detachment jobs.
	 */
	protected List<Runnable> getEntityDetachmentJobs() {
		return this.entityDetachmentJobs;
	}

	/**
	 * Adds a job to the Entity detachment jobs.
	 * @param job The Runnable to be run.
	 */
	protected void addEntityDetachmentJob(Runnable job) {
		this.entityDetachmentJobs.add(job);
	}

	/**
	 * Gets the list of Component update jobs.
	 * @return List of Component update jobs, separated per target Component.
	 */
	protected HashMap<Component, List<Runnable>> getComponentUpdateJobs() {
		return this.componentUpdateJobs;
	}

	/**
	 * Adds a job to the componentUpdateJobs list.
	 * @param component Component concerned by the update.
	 * @param run The Runnable to be run.
	 */
	protected void addComponentUpdateJob(Component component, Runnable run) {
		List<Runnable> list = componentUpdateJobs.computeIfAbsent(component, k -> new LinkedList<>());
		list.add(run);
	}

	/**
	 * Gets the list of listeners to attachment jobs.
	 * @return List of attachment and update listeners.
	 */
	protected List<Runnable> getAttachmentListeners() {
		return this.attachmentListeners;
	}

	/**
	 * Adds a job to the attachment listeners.
	 * @param job The Runnable to be run.
	 */
	protected void addAttachmentListener(Runnable job) {
		this.attachmentListeners.add(job);
	}

	/**
	 * Gets the list of listeners to update jobs.
	 * @return List of attachment and update listeners.
	 */
	protected List<Runnable> getUpdateListeners() {
		return this.updateListeners;
	}

	/**
	 * Adds a job to the update listeners.
	 * @param job The Runnable to be run.
	 */
	protected void addUpdateListener(Runnable job) {
		this.updateListeners.add(job);
	}

	/**
	 * Gets the list of listeners to detachment jobs.
	 * @return List of detachment listeners.
	 */
	protected List<Runnable> getDetachmentListeners() {
		return this.detachmentListeners;
	}

	/**
	 * Adds a job to the detachment listeners.
	 * @param job The Runnable to be run.
	 */
	protected void addDetachmentListener(Runnable job) {
		this.detachmentListeners.add(job);
	}

	/**
	 * Tells whether we are currently in a write phase or not.
	 * @return true if we are in a write phase, false otherwise.
	 */
	public boolean isWritePhase() {
		return writePhase;
	}

	/**
	 * Starts a write phase.
	 */
	public void startWritePhase() {
		writePhase = true;
	}

	/**
	 * Stops a write phase.
	 */
	public void stopWritePhase() {
		writePhase = false;
	}


}