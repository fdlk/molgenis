package org.molgenis.ui.menu;

import org.molgenis.ui.menu.model.Menu;
import org.molgenis.ui.menu.model.MenuItem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.testng.Assert.assertEquals;

public class MenuTest
{
	private Menu menu;

	@BeforeMethod
	public void setUp()
	{
		MenuItem p30 = MenuItem.create("p3_0", "lbl");
		MenuItem p31 = MenuItem.create("p3_1", "lbl");

		Menu p20 = Menu.create("p2_0", "lbl", Arrays.asList(p30, p31));
		MenuItem p21 = MenuItem.create("p2_1", "lbl");

		MenuItem p10 = MenuItem.create("p1_0", "lbl");
		Menu p11 = Menu.create("p1_1", "lbl", Arrays.asList(p20, p21));

		menu = Menu.create("root", "Home", Arrays.asList(p10, p11));
	}

	@Test
	public void findMenuItemPath()
	{
		assertEquals(menu.getPath("p1_0"), Optional.of(Arrays.asList("root", "p1_0");
		//		assertEquals(MenuUtils.findMenuItemPath("p1_1", menu), "/menu/root/p1_1");
		//		assertEquals(MenuUtils.findMenuItemPath("p2_0", menu), "/menu/p1_1/p2_0");
		//		assertEquals(MenuUtils.findMenuItemPath("p2_1", menu), "/menu/p1_1/p2_1");
		//		assertEquals(MenuUtils.findMenuItemPath("p3_0", menu), "/menu/p2_0/p3_0");
		//		assertEquals(MenuUtils.findMenuItemPath("p3_1", menu), "/menu/p2_0/p3_1");
		//		assertNull(MenuUtils.findMenuItemPath("non_existing", menu));
	}
}
