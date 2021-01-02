package de.tum.ei.lkn.eces.core;

/**
 * Filter accepting Components (destination Components) which are in the
 * Entity of a given Component (source Component).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class HasComponentFilter extends Filter {
	/**
	 * Mapper for the source Component.
	 */
	private Mapper componentMapper;

	/**
	 * Creates a HasComponent Filter.
	 * @param controller Controller responsible for creating a Mapper for the
	 *                   source Component.
	 * @param clazz Class of the source Component.
	 */
	public HasComponentFilter(Controller controller, Class<? extends Component> clazz) {
		componentMapper = controller.getMapper(clazz);
	}

	@Override
	public boolean isAccepted(Component component) {
		Entity entity = component.getEntity();
		return (entity != null && componentMapper.isIn(entity));
	}

	@Override
	public String toString() {
		return "has " + componentMapper.getType().getName();
	}
}
