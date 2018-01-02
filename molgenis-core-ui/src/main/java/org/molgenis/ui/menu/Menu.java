package org.molgenis.ui.menu;

import java.util.Iterator;
import java.util.List;

public class Menu extends MenuItem implements Iterable<MenuItem>
{
	public String findMenuItemPath(String id)
	{
		return MenuUtils.findMenuItemPath(id, this);
	}

	public List<MenuItem> deleteMenuItem(String id)
	{
		return MenuUtils.deleteMenuItem(id, getItems());
	}

	@Override
	public Iterator<MenuItem> iterator()
	{
		return getItems().iterator();
	}
}
