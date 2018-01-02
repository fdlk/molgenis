package org.molgenis.ui.menu;

import java.util.Iterator;

public class Menu extends MenuItem implements Iterable<MenuItem>
{
	public String findMenuItemPath(String id)
	{
		return MenuUtils.findMenuItemPath(id, this);
	}

	@Override
	public Iterator<MenuItem> iterator()
	{
		return getItems().iterator();
	}
}
