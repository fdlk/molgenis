package org.molgenis.web.menu;

import org.molgenis.web.menu.model.Menu;

public interface MenuReaderService
{
	Menu getMenu();

	String findMenuItemPath(String id);
}
