package de.tum.ei.lkn.eces.core.mocks;

import de.tum.ei.lkn.eces.core.ComponentStatus;
import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.RootSystem;
import de.tum.ei.lkn.eces.core.annotations.ComponentStateIs;
import de.tum.ei.lkn.eces.core.annotations.HasComponent;

import static org.junit.Assert.fail;

/**
 * Mock System.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class S2 extends RootSystem {
	public int count1 = 0;
	public int count2 = 0;

	private C1S1Mapper c1S1Mapper = new C1S1Mapper(controller);
	private C2S1Mapper c2S1Mapper = new C2S1Mapper(controller);

	public S2(Controller controller) {
		super(controller);
	}

	@ComponentStateIs(State = ComponentStatus.Destroyed)
	@HasComponent(component = C2S1.class)
	public synchronized void countDelete1(C1S1 c) {
		count1++;
		if(c1S1Mapper.isIn(c.getEntity()))
			fail("Component C1S1 should not be on entity!");
		c2S1Mapper.detachComponent(c);
	}

	@ComponentStateIs(State = ComponentStatus.Destroyed)
	@HasComponent(component = C1S1.class)
	public synchronized void countDelete2(C2S1 c) {
		count2++;
		if(c2S1Mapper.isIn(c.getEntity()))
			fail("Component C2S1 should not be on entity!");
		c1S1Mapper.detachComponent(c);
	}
}
