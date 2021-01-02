package de.tum.ei.lkn.eces.core;

import de.tum.ei.lkn.eces.core.mocks.SLC1;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for de.tum.ei.lkn.eces.core.LocalMapper.java.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class LocalMapperTest {

	@Test
	public final void testLocalMapper() {
		Controller c = new Controller();
		Object o = new Object();
		LocalMapper<SLC1> lm = c.getLocalMapper(o, SLC1.class);
		assertEquals("Index of local data is wrong", 0, lm.getIndex());
	}

	@Test
	public final void testGet() {
		Controller c = new Controller();
		Object o1 = new Object();
		Object o2 = new Object();
		LocalMapper<Integer> lm1 = c.getLocalMapper(o1, SLC1.class);
		assertEquals("Index of local data is wrong", 0, lm1.getIndex());
		LocalMapper<Integer> lm2 = c.getLocalMapper(o2, SLC1.class);
		assertEquals("Index of local data is wrong", 1, lm2.getIndex());
		Entity ent = new Entity(0);
		assertNotSame("The different LocalMappers should provide different objects", lm1.get(ent), lm2.get(ent));
		assertSame("The same LocalMapper should provide the same object", lm1.get(ent), lm1.get(ent));
		assertSame("The same LocalMapper should provide the same object", lm2.get(ent), lm2.get(ent));
	}
}