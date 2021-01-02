package de.tum.ei.lkn.eces.core;

/**
 * Filter accepting Components (destination Components) which are *not* in the
 * Entity of a given Component (source Component).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class HasNotComponentFilter extends Filter {
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
	public HasNotComponentFilter(Controller controller, Class<? extends Component> clazz) {
		componentMapper = controller.getMapper(clazz);
	}

	@Override
	public boolean isAccepted(Component component) {
		return !componentMapper.isIn(component.getEntity());
	}

	@Override
	public String toString() {
		return "has not " + componentMapper.getType().getName();
	}
}
