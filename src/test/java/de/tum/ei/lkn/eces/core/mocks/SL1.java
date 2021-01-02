package de.tum.ei.lkn.eces.core.mocks;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.core.RootSystem;

/**
 * Mock System.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SL1 extends RootSystem {
	public LocalMapper<SLC1> map;

	public SL1(Controller con) {
		super(con);
		map = this.getController().getLocalMapper(this, SLC1.class);
	}
}