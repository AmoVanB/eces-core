package de.tum.ei.lkn.eces.core.mocks;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;

/**
 * Mock Component.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = S1.class)
public class C1 extends Component {
	private int count = 0;

	public int getCount() {
		return count;
	}

	public void countUp() {
		this.count++;
	}
}
