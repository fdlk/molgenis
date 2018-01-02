package org.molgenis.ui.menumanager;

import com.google.gson.GsonBuilder;
import org.molgenis.data.DataService;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.menu.Menu;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class MenuManagerServiceImpl implements MenuManagerService
{
	private final AppSettings appSettings;
	private final DataService dataService;

	public MenuManagerServiceImpl(AppSettings appSettings, DataService dataService)
	{
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	@RunAsSystem
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_READ_menumanager')")
	@Transactional(readOnly = true)
	public Iterable<Plugin> getPlugins()
	{
		return dataService.findAll(PluginMetadata.PLUGIN, Plugin.class).collect(toList());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_WRITE_menumanager')")
	@Transactional
	public void saveMenu(Menu molgenisMenu)
	{
		String menuJson = new GsonBuilder().create().toJson(molgenisMenu);
		appSettings.setMenu(menuJson);
	}
}
