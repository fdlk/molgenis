package org.molgenis.ui.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.molgenis.data.settings.AppSettings;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class MenuReaderServiceImpl implements MenuReaderService
{
	private final AppSettings appSettings;
	private final Gson gson = new GsonBuilder().create();

	public MenuReaderServiceImpl(AppSettings appSettings)
	{
		this.appSettings = requireNonNull(appSettings);
	}

	@Override
	public Menu getMenu()
	{
		return Optional.ofNullable(appSettings.getMenu())
					   .map(menuJson -> gson.fromJson(menuJson, Menu.class))
					   .orElse(null);
	}

	@Override
	public String findMenuItemPath(String id)
	{
		return MenuUtils.findMenuItemPath(id, getMenu());
	}
}
