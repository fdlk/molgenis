package org.molgenis.ui.menu;

public interface MenuReaderService
{
	Menu getMenu();

	String findMenuItemPath(String id);
}
