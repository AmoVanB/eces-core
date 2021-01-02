package de.tum.ei.lkn.eces.core.mocks;

import de.tum.ei.lkn.eces.core.ComponentStatus;
import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.RootSystem;
import de.tum.ei.lkn.eces.core.annotations.ComponentStateIs;
import de.tum.ei.lkn.eces.core.annotations.HasComponent;
import de.tum.ei.lkn.eces.core.annotations.HasNotComponent;

/**
 * Mock System.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class S3 extends RootSystem {
	public int count  = 0;
	public int count2 = 0;
	public int count3 = 0;
	public int count4 = 0;
	public int count5 = 0;

	public S3(Controller controller) {
		super(controller);
	}

	@ComponentStateIs(State = ComponentStatus.New)
	public void countUp(C1S1 c) {
		count++;
	}

	@ComponentStateIs(State = ComponentStatus.Destroyed)
	@HasComponent(component = C1S1.class)
	public void countDown(C1S1 c) {
		count--;
	}

	@ComponentStateIs(State = ComponentStatus.Updated)
	@HasComponent(component = C1S1.class)
	@HasNotComponent(component = C1S1.class)
	public void countNot(C1S1 c) {
	}

	@ComponentStateIs(State = ComponentStatus.New)
	public void countUp(C2S1 c) {
		count2++;
	}

	@ComponentStateIs(State = ComponentStatus.New)
	public void countUp(C3S1 c) {
		count3++;
	}

	@ComponentStateIs(State = ComponentStatus.New)
	public void countUp(C4S1 c) {
		count4++;
	}

	@ComponentStateIs(State = ComponentStatus.Any)
	public void dummy(C4S1 c) {
		count5++;
	}

	@ComponentStateIs(State = ComponentStatus.New)
	@HasComponent(component = C1S1.class)
	public void testInvocationTargetException(C4S1 c) {
		Integer array[] = new Integer[0];
		array[1] = 4;
	}
}