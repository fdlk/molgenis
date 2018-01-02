package org.molgenis.ui.menu;

import java.util.Scanner;
import java.util.Stack;

public class MenuUtils
{
	/**
	 * Return URI path to menu item of the given id or null if item does not exist.
	 */
	public static String findMenuItemPath(String id, Menu menu)
	{
		Stack<MenuItem> path = new Stack<>();
		MenuItem menuItem = findMenuItemPathRec(id, menu, path);
		if (menuItem != null)
		{
			StringBuilder pathBuilder = new StringBuilder("/menu/");
			if (path.size() > 1)
			{
				pathBuilder.append(path.get(path.size() - 2).getId()).append('/');
			}
			pathBuilder.append(path.get(path.size() - 1).getId());
			return pathBuilder.toString();
		}
		else
		{
			return null;
		}

	}

	private static MenuItem findMenuItemPathRec(String id, MenuItem menu, Stack<MenuItem> path)
	{
		path.push(menu);
		for (MenuItem item : menu.getItems())
		{
			if (item.getId().equals(id))
			{
				path.push(item);
				return item;
			}
			else if (item.getType() == MenuItemType.MENU)
			{
				MenuItem itemOfInterest = findMenuItemPathRec(id, item, path);
				if (itemOfInterest != null)
				{
					return itemOfInterest;
				}
			}
		}
		path.pop();
		return null;
	}

	public static String readDefaultMenuValueFromClasspath()
	{
		return new Scanner(MenuUtils.class.getResourceAsStream("/molgenis_ui.json")).useDelimiter("\\A").next();
	}
}
