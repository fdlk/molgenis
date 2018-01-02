package org.molgenis.ui.menumanager;

import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.ui.menu.Menu;

public interface MenuManagerService
{
	Iterable<Plugin> getPlugins();

	void saveMenu(Menu molgenisMenu);
}