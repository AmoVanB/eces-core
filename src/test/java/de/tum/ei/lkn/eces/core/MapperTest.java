package de.tum.ei.lkn.eces.core;

import de.tum.ei.lkn.eces.core.exceptions.MapperException;
import de.tum.ei.lkn.eces.core.mocks.*;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for de.tum.ei.lkn.eces.core.Mapper.java.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MapperTest {
	private Controller controller;
	private Mapper<C1> c1Mapper;
	private Mapper<C2> c2Mapper;

	@Before
	public void setup() {
		controller = new Controller();

		new S1(controller);

		c1Mapper = controller.getMapper(C1.class);
		c2Mapper = controller.getMapper(C2.class);
	}

	@After
	public final void cleanThreadLocal() {
		// Clean thread data after each test.
		controller.resetThreadLocal();
	}

	@Test
	public final void testMapper() {
		Controller cont = new Controller();

		new S1(cont);

		c1Mapper = cont.getMapper(C1.class);
		assertSame("Class type is not correctly set", c1Mapper.getType(), C1.class);
	}

	@Test
	public final void testNewMapperInit() throws ClassNotFoundException {
		Controller cont = new Controller();

		new S1(cont);

		c1Mapper = new C1Mapper(cont);
		assertSame("Class type is not correctly set", c1Mapper.getType(), C1.class);
	}

	@Test
	public final void testGet() {
		Entity ent = new Entity(0, 2);
		ent.extend(0, 2, 2);
		C1 c1 = new C1();
		ent.setComponent(c1, 0, 0);

		assertSame("The Mapper does not return the right component", c1Mapper.get(c1.getEntity()), c1);

		try(MapperSpace ms = controller.startMapperSpace()) {
			assertSame("The Mapper does not return the right component", c1Mapper.get(c1.getEntity()), c1);
			assertSame("The Mapper does not return the right component", c1Mapper.get(c1.getEntity()), c1);
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 1)", 1, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 0)", 0, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 0)", 0, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 0)", 0, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 0)", 0, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}
	}

	@Test
	public final void testGetOptimistic() {
		Entity ent = new Entity(0, 2);
		ent.extend(0, 2, 2);
		C1 c1 = new C1();
		ent.setComponent(c1, 0, 0);
		try(MapperSpace ms = controller.startMapperSpace()) {
			assertSame("The Mapper does not return the right component", c1Mapper.getOptimistic(c1.getEntity()), c1);
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 0)", 0, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 0)", 0, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 0)", 0, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 0)", 0, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 0)", 0, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}
	}

	@Test
	public final void testIsIn() {
		Entity ent = new Entity(0, 2);
		ent.extend(0, 2, 2);
		C1 c1 = new C1();
		ent.setComponent(c1, 0, 0);

		assertTrue("The Mapper does not recognize component", c1Mapper.isIn(c1.getEntity()));
		assertFalse("The Mapper recognizes wrong component", c2Mapper.isIn(c1.getEntity()));

		try(MapperSpace ms = controller.startMapperSpace()) {
			assertTrue("The Mapper does not recognize component", c1Mapper.isIn(c1.getEntity()));
			assertFalse("The Mapper recognizes wrong component", c2Mapper.isIn(c1.getEntity()));
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 1)", 1, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 0)", 0, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 0)", 0, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 0)", 0, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 0)", 0, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}
	}

	@Test
	public final void testGetWait() throws InterruptedException {
		Entity ent = new Entity(0);
		C1 c1 = new C1();

		Thread task = new Thread() {
			@Override
			public void run() {
				c1Mapper.getWait(ent);
			}
		};

		task.start();
		Thread.sleep(100);

		if(!(task.getState() == Thread.State.BLOCKED
				|| task.getState() == Thread.State.WAITING
				|| task.getState() == Thread.State.RUNNABLE))
			fail("The getWait failed (thread state: " + task.getState() + ")");

		c1Mapper.attachComponent(ent, c1);

		task.join(100);

		if(!(task.getState() == Thread.State.TERMINATED))
			fail("The getWait failed");
	}

	@Test
	public final void testAttachComponentEntityC() {
		Entity ent = new Entity(0);
		C1 c1 = new C1();
		c1Mapper.attachComponent(ent, c1);
		assertTrue("The Mapper does not set correctly the component", ent.hasComponent(0, 0));

		try(MapperSpace ms = controller.startMapperSpace()) {
			ent = new Entity(0);
			c1 = new C1();
			c1Mapper.attachComponent(ent, c1);
			assertFalse("The Mapper sets component too early (should be done at MapperSpace closure)", ent.hasComponent(0, 0));
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 0)", 0, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 1)", 1, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 0)", 0, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 1)", 1, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 0)", 0, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}

		ent = new Entity(0);
		C2 c2 = new C2();
		c2Mapper.attachComponent(ent, c2);
		assertTrue("The Mapper does not set correctly the component", ent.hasComponent(0, 0));

	}

	@Test
	public final void testAttachComponentComponentC() {
		Entity ent = new Entity(0);
		C1 c1 = new C1();
		C2 c2 = new C2();
		ent.extend(1, 2, 2);
		ent.setComponent(c2, 1, 1);
		c1Mapper.attachComponent(c2, c1);
		assertTrue("The Mapper does not set component correctly", ent.hasComponent(0, 0));

		try(MapperSpace ms = controller.startMapperSpace()) {
			ent = new Entity(0);
			c1 = new C1();
			c2 = new C2();
			ent.extend(1, 2, 2);
			ent.setComponent(c2, 1, 1);
			c1Mapper.attachComponent(c2, c1);
			assertFalse("The Mapper sets component too early (should be done at MapperSpace closure)", ent.hasComponent(0, 0));
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 0)", 0, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 1)", 1, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 0)", 0, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 1)", 1, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 0)", 0, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}
	}

	@Test
	public final void testAttachComponentComponentEntity() {
		C1S1Mapper c1S1Mapper = new C1S1Mapper(controller);
		C2S1Mapper c2S1Mapper = new C2S1Mapper(controller);
		C1S2Mapper c1S2Mapper = new C1S2Mapper(controller);
		Entity ent = controller.createEntity();
		C1S1 c1 = new C1S1();
		C2S1 c2 = new C2S1();
		C1S2 c3 = new C1S2();
		try(MapperSpace ms = controller.startMapperSpace()) {
			c1S1Mapper.attachComponent(c2, c1);
			c2S1Mapper.attachComponent(ent, c2);
			c1S2Mapper.attachComponent(c2, c3);
			assertFalse("The Mapper sets component too early (should be done at MapperSpace closure)", c1S1Mapper.isIn(c2));
			assertFalse("The Mapper sets component too early (should be done at MapperSpace closure)", c1S2Mapper.isIn(c2));
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 0)", 0, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 1)", 3, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 0)", 0, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 1)", 3, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 0)", 0, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}

		assertTrue("The Mapper did not set component", c1S1Mapper.isIn(c2));
		assertTrue("The Mapper did not set component", c1S2Mapper.isIn(c2));
	}
	@Test
	public final void testAttachComponentComponentEntityError() {
		C1S1Mapper c1S1Mapper = new C1S1Mapper(controller);
		C2S1Mapper c2S1Mapper = new C2S1Mapper(controller);
		C1S2Mapper c1S2Mapper = new C1S2Mapper(controller);
		C1S1 c1 = new C1S1();
		C2S1 c2 = new C2S1();
		C1S2 c3 = new C1S2();
		boolean sucess = false;
		try{
			try(MapperSpace ms = controller.startMapperSpace()) {
				c1S1Mapper.attachComponent(c2, c1);
				c2S1Mapper.attachComponent(c1, c2);
				c1S2Mapper.attachComponent(c2, c3);
			}
		}catch (MapperException e){
			sucess = true;
		}
		assertTrue("The Mapper should throw an exception", sucess);
	}
	@Test
	public final void testDetachComponentEntity() {
		Entity ent = new Entity(0,2);
		ent.extend(0, 2, 2);
		C1 c1 = new C1();
		ent.setComponent(c1, 0, 0);

		assertSame("The Mapper does not return the right component", c1Mapper.detachComponent(ent), c1);
		assertNull("The Mapper should return null", c1Mapper.detachComponent(ent));

		ent.setComponent(c1, 0, 0);
		try(MapperSpace ms = controller.startMapperSpace()) {
			assertSame("The Mapper does not return the right component", c1Mapper.detachComponent(ent), c1);
			assertTrue("Entity should still have the component", ent.hasComponent(0, 0));
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 1)", 1, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 0)", 0, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 1)", 1, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 0)", 0, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 1)", 1, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}

		C2 c2 = new C2();
		ent.extend(0, 2, 2);
		ent.extend(1, 2, 2);
		ent.setComponent(c2, 0, 0);
		c2Mapper.detachComponent(ent);
		assertFalse("The Mapper does not remove correctly the component", ent.hasComponent(0, 0));
	}

	@Test
	public final void testDetachComponentComponent() {
		Entity ent = new Entity(0,2);
		ent.extend(0, 2, 2);
		ent.extend(1, 2, 2);
		C1 c1 = new C1();
		ent.setComponent(c1, 0, 0);
		assertSame("The Mapper does not return the right component", c1Mapper.detachComponent(c1), c1);
		assertNull("The Mapper should return null", c1Mapper.detachComponent(ent));

		ent.setComponent(c1, 0, 0);
		C2 c2 = new C2();
		ent.setComponent(c2, 1, 0);

		assertSame("The Mapper does not return the right component", c1Mapper.detachComponent(c2), c1);
		assertNull("The Mapper should return null", c1Mapper.detachComponent(c2));

		ent.setComponent(c1, 0, 0);
		try(MapperSpace ms = controller.startMapperSpace()) {
			assertSame("The Mapper does not return the right component", c1Mapper.detachComponent(ent), c1);
			assertTrue("Entity should still have the component", ent.hasComponent(0, 0));
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 1)", 1, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 0)", 0, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 0)", 0, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 1)", 1, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 0)", 0, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 1)", 1, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 0)", 0, data.getComponentUpdateJobs().size());
		}
	}

	@Test
	public final void testFilteredDetachProcess() {
		Controller controller = new Controller();
		S2 s2 = new S2(controller);

		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);
		Mapper<C2S1> c2S1Mapper = controller.getMapper(C2S1.class);

		Entity ent = controller.createEntity();

		c1S1Mapper.attachComponent(ent, new C1S1());
		c2S1Mapper.attachComponent(ent, new C2S1());

		assertEquals("Count1 (" + s2.count1 + ") should be 0", 0, s2.count1);
		assertEquals("Count2 (" + s2.count2 + ") should be 0", 0, s2.count2);

		c1S1Mapper.detachComponent(ent);

		assertEquals("Count1 (" + s2.count1 + ") should be 1", 1, s2.count1);
		assertEquals("Count2 (" + s2.count2 + ") should be 0", 0, s2.count2);

		assertFalse("Entity should not hold C1S1", c1S1Mapper.isIn(ent));
		assertFalse("Entity should not hold C2S1", c2S1Mapper.isIn(ent));

		c1S1Mapper.attachComponent(ent, new C1S1());
		c2S1Mapper.attachComponent(ent, new C2S1());

		assertEquals("Count1 (" + s2.count1 + ") should be 1", 1, s2.count1);
		assertEquals("Count2 (" + s2.count2 + ") should be 0", 0, s2.count2);

		c2S1Mapper.detachComponent(ent);

		assertEquals("Count1 (" + s2.count1 + ") should be 1", 1, s2.count1);
		assertEquals("Count2 (" + s2.count2 + ") should be 1", 1, s2.count2);

		assertFalse("Entity should not hold C1S1", c1S1Mapper.isIn(ent));
		assertFalse("Entity should not hold C2S1", c2S1Mapper.isIn(ent));
	}

	@Test
	public final void testUpdateComponent() {
		Entity ent = new Entity(1);
		C1 c1 = new C1();
		c1Mapper.attachComponent(ent, c1);

		c1Mapper.updateComponent(c1, c1::countUp);
		assertEquals("Component update has to count up exactly once", 1, c1.getCount());

		try(MapperSpace ms = controller.startMapperSpace()) {
			c1Mapper.updateComponent(c1, c1::countUp);
			assertEquals("Component update has not to count up yet", 1, c1.getCount());
			MapperData data = controller.getMapperData();
			assertEquals("The Mapper registers " + data.getReadLocks().size() + " read locks (should be 0)", 0, data.getReadLocks().size());
			assertEquals("The Mapper registers " + data.getAttachmentListeners().size() + " attachment listener jobs (should be 0)", 0, data.getAttachmentListeners().size());
			assertEquals("The Mapper registers " + data.getUpdateListeners().size() + " update listener jobs (should be 1)", 1, data.getUpdateListeners().size());
			assertEquals("The Mapper registers " + data.getDetachmentListeners().size() + " detachment listener jobs (should be 0)", 0, data.getDetachmentListeners().size());
			assertEquals("The Mapper registers " + data.getEntityAttachmentJobs().size() + " attachment jobs (should be 0)", 0, data.getEntityAttachmentJobs().size());
			assertEquals("The Mapper registers " + data.getEntityDetachmentJobs().size() + " detachment jobs (should be 0)", 0, data.getEntityDetachmentJobs().size());
			assertEquals("The Mapper registers " + data.getComponentUpdateJobs().size() + " update jobs (should be 1)", 1, data.getComponentUpdateJobs().size());
		}

		assertEquals("Component update has to count up twice after the MapperSpace", 2, c1.getCount());
	}

	@Test
	public final void testReadLockException() {
		Controller controller = new Controller();

		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);
		Entity ent = controller.createEntity();
		C1S1 c1S1 = new C1S1();
		c1S1Mapper.attachComponent(ent, c1S1);
		try {
			try(MapperSpace ms = controller.startMapperSpace()) {
				c1S1Mapper.updateComponent(c1S1Mapper.get(ent), () -> c1S1Mapper.get(ent).hashCode());
				c1S1Mapper.updateComponent(c1S1Mapper.get(ent), () -> c1S1Mapper.get(ent).hashCode());
			}
			fail("ControllerException should be thrown.");
		} catch(MapperException e) {
			assertTrue(true);
		}
	}

	@Test(expected=MapperException.class)
	public final void testMapperException() {
		Controller controller = new Controller();

		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);

		Entity ent = controller.createEntity();
		C1S1 c1S1 = new C1S1();
		c1S1Mapper.attachComponent(ent, c1S1);

		c1S1 = new C1S1();
		c1S1Mapper.attachComponent(ent, c1S1);
	}

	@Test
	public final void testMapperException2() {
		Entity ent = new Entity(0);
		C1 c1 = new C1();
		c1Mapper.attachComponent(ent, c1);

		try {
			C2 c2 = new C2();
			c2Mapper.attachComponent(ent, c2);
		} catch(MapperException e){}

		assertSame("The Mapper should not overwrite Component", c1Mapper.getOptimistic(ent), c1);
		assertNull("The Mapper sets the Component c2 partly", c2Mapper.getOptimistic(ent));
	}

	@Test
	public final void testAdvanceDetachment() {
		Entity ent = new Entity(0);
		C2 c2 = new C2();
		c2Mapper.attachComponent(ent, c2);

		c1Mapper.detachComponent(ent);

		assertNull("The Mapper should remove Component", c1Mapper.getOptimistic(ent));
		assertNull("The Mapper should remove Component", c2Mapper.getOptimistic(ent));
	}

	@Test
	public final void testAdvanceAttachment() {
		Entity ent = new Entity(0);
		C2 c2 = new C2();
		c1Mapper.attachComponent(ent, c2);

		assertSame("The Mapper should attach Component", c1Mapper.getOptimistic(ent), c2);
		assertSame("The Mapper should attach Component", c2Mapper.getOptimistic(ent), c2);
	}

	@Test(timeout=1000)
	public final void testAttachmentUpdateDetachment() {
		Controller controller = new Controller();

		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);
		Entity ent = controller.createEntity();
		C1S1 c1S1 = new C1S1();
		c1S1Mapper.attachComponent(ent, c1S1);
		C1S1 c = c1S1Mapper.get(ent);
		c1S1Mapper.updateComponent(c1S1Mapper.get(ent), c::hashCode);
		c1S1Mapper.detachComponent(ent);
	}

	@Test(timeout=1000)
	public final void testAttachmentDetachment() {
		Controller controller = new Controller();

		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);
		Entity ent = controller.createEntity();
		C1S1 c1S1 = new C1S1();
		c1S1Mapper.attachComponent(ent, c1S1);
		c1S1Mapper.detachComponent(ent);
	}

	@Test(timeout=1000)
	public final void testDoubleAttachmentWithOneListener() {
		Controller controller = new Controller();

		C1S1 c1S1 = new C1S1();
		C1S2 c1S2 = new C1S2();

		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);
		Mapper<C1S2> c1S2Mapper = controller.getMapper(C1S2.class);

		Entity ent = controller.createEntity();

		try(MapperSpace ms = controller.startMapperSpace()) {
			c1S1Mapper.attachComponent(ent, c1S1);
			c1S2Mapper.attachComponent(ent, c1S2);
		}
	}

	@Test
	public void testToJSONObject() {
		Controller controller = new Controller();

		new S1(controller);
		Mapper<C1S1> c1S1Mapper = controller.getMapper(C1S1.class);

		new S2(controller);

		C1S1 c1S1 = new C1S1();
		C1S2 c1S2 = new C1S2();

		Mapper<C1S2> c1S2Mapper = controller.getMapper(C1S2.class);

		Entity ent = controller.createEntity();

		try(MapperSpace ms = controller.startMapperSpace()) {
			c1S1Mapper.attachComponent(ent, c1S1);
			c1S2Mapper.attachComponent(ent, c1S2);
		}

		JSONObject json = c1S2Mapper.createJSONObject(ent);

		try {
			assertEquals(0, json.toString().compareTo("{\"data\":{\"0\":{\"0\":{\"class\":\"C1S1\"},\"sysClass\":\"S1\"},\"1\":{\"0\":{\"class\":\"C1S2\"},\"sysClass\":\"S2\"}},\"entityId\":0,\"type\":\"Entity\"}"));
		} catch(ArrayIndexOutOfBoundsException | NullPointerException e) {
			fail("Error while producing JSON Object");
		}
	}
}
