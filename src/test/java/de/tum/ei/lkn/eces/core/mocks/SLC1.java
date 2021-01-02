package de.tum.ei.lkn.eces.core.mocks;

import de.tum.ei.lkn.eces.core.LocalComponent;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;

/**
 * Mock LocalComponent.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = SL1.class)
public class SLC1 extends LocalComponent {
	@Override
	public Object init() {
		return new Object();
	}
}