package de.tum.ei.lkn.eces.core;

import de.tum.ei.lkn.eces.core.exceptions.ControllerException;
import de.tum.ei.lkn.eces.core.mocks.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for de.tum.ei.lkn.eces.core.Listener.java.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ListenerTest {

	@Test
	public final void testProcessTask() throws NoSuchMethodException {
		Controller controller = new Controller();

		S3 s3 = new S3(controller);
		Listener listener = new Listener(controller, s3, S3.class.getMethod("countNot", C1S1.class));

		assertEquals("Listener is not constructed correctly", 0, listener.toString().trim().compareTo("de.tum.ei.lkn.eces.core.mocks.S3 - countNot- Filters: has de.tum.ei.lkn.eces.core.mocks.C1S1; has not de.tum.ei.lkn.eces.core.mocks.C1S1;"));
	}

	@Test
	public final void testRunTask() throws NoSuchMethodException {
		Controller controller = new Controller();

		S3 s3 = new S3(controller);
		Listener listener = new Listener(controller, s3, S3.class.getMethod("countNot", C1S1.class));

		listener.runTask(new C1S1());
		listener.runTask(new C1());

		Entity ent = new Entity(2);
		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);
		C1S1 c1S1 = new C1S1();
		c1S1Mapper.attachComponent(ent, c1S1);
		listener.runTask(c1S1);

		Listener listener2 = new Listener(controller, s3, S3.class.getMethod("countUp", C1S1.class));

		assertEquals("Counter should be 1", 1, s3.count);
		listener2.runTask(c1S1);
		assertEquals("Counter should be 2", 2, s3.count);

		C2S1 c2S1 = new C2S1();
		Mapper<C2S1> c2S1Mapper = controller.getMapper(C2S1.class);
		c2S1Mapper.attachComponent(ent, c2S1);
		try {
			listener2.runTask(c2S1);
			fail("An IllegalArgumentException should happen");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public final void testInvocationTargetException() throws NoSuchMethodException {
		Controller controller = new Controller();
		S3 s3 = new S3(controller);
		Mapper<C4S1> c4S1Mapper = controller.getMapper(C4S1.class);
		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);
		Entity ent = controller.createEntity();
		C4S1 component = new C4S1();
		c4S1Mapper.attachComponent(ent,component);
		c1S1Mapper.attachComponent(ent,new C1S1());

		try {
			Listener listener = new Listener(controller, s3, S3.class.getMethod("testInvocationTargetException", C4S1.class));
			listener.runTask(component);
		} catch(ControllerException e) {
			return;
		}

		fail("A ControllerException should be thrown");
	}
}