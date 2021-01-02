package de.tum.ei.lkn.eces.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for de.tum.ei.lkn.eces.core.Entity.java.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class EntityTest {
	private Entity e1;
	private Entity e2;

	@Before
	public void setup() {
		e1 = new Entity(0);
		e2 = new Entity(1, 2);
	}

	@Test
	public void testGetId() {
		assertEquals("Id returned should be 0 but is " + e1.getId(), 0, e1.getId());
		assertEquals("Id returned should be 1 but is " + e1.getId(), 1, e2.getId());
	}

	@Test
	public void testGetComponent() {
		assertNull("There should be no Component at e1", e1.getComponent(1, 0));
		assertNull("There should be no Component at e2", e2.getComponent(1, 0));
		e2.extend(1, 2, 2);
		e2.setComponent(new Component(), 1, 0);
		assertNull("There should be no Component at e1", e1.getComponent(1, 0));
		assertNotNull("There should be a Component at e2", e2.getComponent(1, 0));
	}

	@Test
	public void testRemoveComponent() {
		e2.extend(1, 2, 2);
		e2.setComponent(new Component(), 1, 0);
		assertNotNull("There should be a Component at e2", e2.getComponent(1, 0));
		e2.removeComponent(1, 0);
		assertNull("There should be no Component at e2", e2.getComponent(1, 0));
	}

	@Test
	public void testSetComponent() {
		e2.extend(1, 2, 2);
		Component c1 = new Component();
		assertNull("There should be no Component at e2", e2.getComponent(1, 0));
		assertNull("There should be no Entity set on c1", c1.getEntity());
		e2.setComponent(c1, 1, 0);
		assertNotNull("There should be a Component at e2", e2.getComponent(1, 0));
		assertSame("There should be e2 set on c1", c1.getEntity(), e2);
	}

	@Test
	public void testExtend() {
		Component c1 = new Component();

		try {
			e1.setComponent(c1, 0, 0);
			fail("Data structure should be null and hence statement not reached");
		} catch(ArrayIndexOutOfBoundsException | NullPointerException e) {
			assertTrue(true);
		}

		e1.extend(0, 2, 2);
		try {
			e1.setComponent(c1, 0, 0);
		} catch(ArrayIndexOutOfBoundsException | NullPointerException e) {
			fail("Data structure should now have the right amount of slots");
		}
		assertSame("There should be c1 at e1", e1.getComponent(0, 0), c1);

		e1.extend(1, 2, 2);
		Component c2 = new Component();
		try {
			e1.setComponent(c2, 1, 0);
		} catch(ArrayIndexOutOfBoundsException | NullPointerException e) {
			fail("Data structure should have the right amount of slots");
		}
		assertSame("There should be c1 at e1", e1.getComponent(0, 0), c1);
		assertSame("There should be c2 at e1", e1.getComponent(1, 0), c2);
	}

	@Test
	public final void testHasComponent() {
		e2.extend(1, 2, 2);
		e2.setComponent(new Component(), 1, 0);
		for(int i = 0; i < 100; i++){
			for(int j = 0; j < 100; j++) {
				try {
					if(i == 1 && j == 0)
						assertTrue("hasComponent provides the wrong result (true)", e2.hasComponent(i, j));
					else
						assertFalse(e2.hasComponent(i, j));
				} catch (Exception e) {
					fail("hasComponent should never throw an exception");
				}
			}
		}
	}

	@Test
	public void testCompareTo() {
		assertEquals(1, e1.compareTo(e2));
		assertEquals(-1, e2.compareTo(e1));
		assertEquals(0, e1.compareTo(e1));
		assertEquals(0, e2.compareTo(e2));
	}

	@Test
	public void testToString() {
		Component c1 = new Component();
		Component c2 = new Component();

		assertEquals("toString output is wrong", 0, e1.toString().compareTo("Entity:"));

		e1.extend(0, 2, 2);
		e1.extend(1, 2, 2);
		e1.setComponent(c1, 0, 0);
		e1.setComponent(c1, 0, 1);
		e1.setComponent(c2, 1, 1);

		assertEquals("toString output is wrong", 0, e1.toString().compareTo("Entity: Component Component | Component |"));
	}
}
