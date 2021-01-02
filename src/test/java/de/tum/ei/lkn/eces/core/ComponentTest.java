package de.tum.ei.lkn.eces.core;

import de.tum.ei.lkn.eces.core.mocks.C1;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for de.tum.ei.lkn.eces.core.Component.java.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ComponentTest {
	@Test
	public void cloneTest() {
		Controller controller = new Controller();
		Mapper<C1> c1Mapper = controller.getMapper(C1.class);
		Entity ent = new Entity(0);
		C1 comp = new C1();
		c1Mapper.attachComponent(ent, comp);
		// Checking clone() method verifies needed properties.
		assertNotSame("Clone must be different from original Component", comp.clone(), comp);
		assertSame("Clone and original Component must be of the same class", comp.clone().getClass(), comp.getClass());

		Component clone = comp.clone();
		assertNull("The clone Component should have no Entity set", clone.getEntity());
		assertNotSame("The new Component should have a different lock object than the original Component", clone.getLock(), comp.getLock());
	}
}
