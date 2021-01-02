package de.tum.ei.lkn.eces.core;

/**
 * Class representing a MapperSpace.
 * A MapperSpace is a environment in which a Mapper can be used to modify
 * Entities and Components. The usage of a Mapper has to be done in such
 * an environment because the environment takes care of all the possible
 * synchronization issues.
 *
 * Within a MapperSpace, all the modification (set, update, delete) actions
 * are not immediately effective. These will only be triggered when the Mapper
 * Space is closed.
 *
 * In a Mapper Space, it is assumed that the user executes the operations
 * in the following order:
 * 1. Removal of Components
 * 2. Creation of Components
 * 3. Update of Components
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public interface MapperSpace extends AutoCloseable {
	/**
	 * Closes the MapperSpace.
	 * This should have the behavior of executing all the jobs and
	 * releasing all the read locks acquired in the Mapper Space.
	 */
	@Override
	void close();
}