package de.tum.ei.lkn.eces.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import de.tum.ei.lkn.eces.core.exceptions.ControllerException;
import de.tum.ei.lkn.eces.core.mocks.*;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for de.tum.ei.lkn.eces.core.Controller.java.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ControllerTest {

	@Test
	public final void testGetDefaultController() {
		assertSame("Default controller is not always the same", Controller.getDefaultController(), Controller.getDefaultController());
	}

	@Test
	public final void testGetMapper() {
		Controller c = new Controller();

		Mapper<C4S1> c4s1Mapper = c.getMapper(C4S1.class);

		Entity ent = new Entity(0,2);
		C4S1 c4s1 = new C4S1();
		c4s1Mapper.attachComponent(ent, c4s1);
		assertNotNull("Entity is not set", c4s1.getEntity());
		assertSame("Deletion failed", c4s1Mapper.detachComponent(ent), c4s1);
		assertFalse(c4s1Mapper.isIn(ent));
		assertSame("Entity should still be set on the Component", c4s1.getEntity(), ent);
	}

	@Test
	public final void testRegisterSystemSystem() {
		Controller c = new Controller();
		S1 s = new S1(c);
		assertEquals("Wrong system ID (should be 0 but is " + s.getId() + ")", 0, s.getId());
		S2 s2 = new S2(c);
		assertEquals("Wrong system ID (should be 1 but is " + s.getId() + ")", 1, s2.getId());
		try {
			new S1(c);
			fail("Registration of two times the same System should not be possible");
		} catch(ControllerException e) {
			assertTrue(true);
		}
	}

	@Test
	public final void testGetSystemIdentifier() {
		Controller c = new Controller();
		try {
			c.getSystemIdentifier(S2.class);
			fail("An unknown System should throw a ControllerException");
		} catch(ControllerException e) {
			assertTrue(true);
		}

		new S1(c);
		new S2(c);
		assertEquals("Wrong system ID (should be 1 but is " + c.getSystemIdentifier(S2.class) + ")", 1, c.getSystemIdentifier(S2.class));
		assertEquals("Wrong system ID (should be 0 but is " + c.getSystemIdentifier(S2.class) + ")", 0, c.getSystemIdentifier(S1.class));
	}

	@Test
	public final void testGetMaximumComponentCount() {
		Controller c = new Controller();
		try {
			c.getMaximumComponentCount(0);
			fail("An unknown System should throw a ControllerException");
		} catch(ControllerException e) {
			assertTrue(true);
		}

		S1 s = new S1(c);
		try {
			c.getMaximumComponentCount(0);
			fail("An unknown System should throw a ControllerException");
		} catch(ControllerException e) {
			assertTrue(true);
		}

		c.getMapper(C1S1.class);
		assertEquals("Component count is wrong (should be 1 but is " + c.getMaximumComponentCount(s.getId()) + ")", 1, c.getMaximumComponentCount(s.getId()));
		c.getMapper(C2S1.class);
		assertEquals("Component count is wrong (should be 2 but is " + c.getMaximumComponentCount(s.getId()) + ")", 2, c.getMaximumComponentCount(s.getId()));
	}

	@Test
	public final void testSubmitAttachment() {
		Controller c = new Controller();
		S3 s = new S3(c);

		c.runAttachmentListeners(new C1S1());
		c.runAttachmentListeners(new C2S1());
		c.runAttachmentListeners(new C3S1());
		c.runAttachmentListeners(new C4S1());

		assertEquals("Event count is wrong", 1, s.count);
		assertEquals("Event count is wrong", 3, s.count2);
		assertEquals("Event count is wrong", 2, s.count3);
		assertEquals("Event count is wrong", 1, s.count4);
		assertEquals("Event count is wrong", 1, s.count5);
	}

	@Test
	public final void testSubmitDetachment() {
		Controller c = new Controller();
		S3 s = new S3(c);
		Mapper<C1S1> c1S1Mapper = c.getMapper(C1S1.class);
		c.runDetachmentListeners(new C1S1());

		assertEquals("Event count is wrong", 0, s.count);

		Entity entity = c.createEntity();
		C1S1 c1S1 = new C1S1();
		c1S1Mapper.attachComponent(entity,c1S1);

		s.count = 0;
		c.runDetachmentListeners(c1S1);

		assertEquals("Event count is wrong", -1, s.count);
		assertEquals("Event count is wrong", 0, s.count2);
		assertEquals("Event count is wrong", 0, s.count3);
		assertEquals("Event count is wrong", 0, s.count4);
		assertEquals("Event count is wrong", 0, s.count5);
	}

	@Test
	public final void testSubmitUpdate() {
		Controller c = new Controller();
		S3 s = new S3(c);

		c.runUpdateListeners(new C4S1());

		assertEquals("Event count is wrong", 0, s.count);
		assertEquals("Event count is wrong", 0, s.count2);
		assertEquals("Event count is wrong", 0, s.count3);
		assertEquals("Event count is wrong", 0, s.count4);
		assertEquals("Event count is wrong", 1, s.count5);
	}

	@Test
	public final void testGetClassHierarchy() {
		Controller c = new Controller();

		List<Class<?>> result = c.getClassHierarchy(C4S1.class, true);
		assertEquals("Method detects wrong number of classes", 1, result.size());
		assertSame("Wrong class detected", result.get(0), C2S1.class);

		result = c.getClassHierarchy(C4S1.class, false);
		assertEquals("Method detects wrong number of classes", 3, result.size());
		assertSame("Wrong class detected", result.get(0), C4S1.class);
		assertSame("Wrong class detected", result.get(1), C3S1.class);
		assertSame("Wrong class detected", result.get(2), C2S1.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void testGetSystemId() {
		Controller c = new Controller();

		List<Class<?>> result = c.getClassHierarchy(C4S1.class, true);
		assertEquals("Wrong system id", 0, c.getSystemId((Class<? extends Component>) result.get(0)));
		assertEquals("Wrong system id", 1, c.getSystemId(C1S2.class));

	}

	@Test
	public final void testGetAnnotatedMethods() {
		Controller controller = new Controller();
		HashSet<String> results = new HashSet<>();
		results.add("public void de.tum.ei.lkn.eces.core.mocks.S4.countUp(de.tum.ei.lkn.eces.core.mocks.C4S1)");
		results.add("public void de.tum.ei.lkn.eces.core.mocks.S4.countNot(de.tum.ei.lkn.eces.core.mocks.C1S1)");
		results.add("public void de.tum.ei.lkn.eces.core.mocks.S4.countUp(de.tum.ei.lkn.eces.core.mocks.C3S1)");
		results.add("public void de.tum.ei.lkn.eces.core.mocks.S4.countUp(de.tum.ei.lkn.eces.core.mocks.C2S1)");
		results.add("public void de.tum.ei.lkn.eces.core.mocks.S4.countDown(de.tum.ei.lkn.eces.core.mocks.C1S1)");
		results.add("public void de.tum.ei.lkn.eces.core.mocks.S4.countUp(de.tum.ei.lkn.eces.core.mocks.C1S1)");
		results.add("public void de.tum.ei.lkn.eces.core.mocks.S4.dummy(de.tum.ei.lkn.eces.core.mocks.C4S1)");
		ImmutableList<Method> result = controller.getAnnotatedMethods(S4.class);
		for(Method value: result)
			assertTrue(value + " is not annotated", results.contains(value.toString().trim()));
	}

	@Test
	public final void testFindAllSubscribers() {
		Controller c = new Controller();
		S3 s = new S3(c);
		Multimap<Class<? extends Component>, Listener> result = c.findAllListeners(s, ComponentStatus.New);
		assertEquals("Not the right amount of listeners", 6, result.size());

		result = c.findAllListeners(s, ComponentStatus.Updated);
		assertEquals("Not the right amount of listeners", 2, result.size());

		result = c.findAllListeners(s, ComponentStatus.Destroyed);
		assertEquals("Not the right amount of listeners", 2, result.size());
	}
}