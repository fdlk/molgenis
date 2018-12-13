package org.molgenis.web.menu;

import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.model.Menu;

public class MenuReaderServiceImpl implements MenuReaderService {
  private final AppSettings appSettings;
  private final Gson gson;

  public MenuReaderServiceImpl(AppSettings appSettings, Gson gson) {
    this.appSettings = requireNonNull(appSettings);
    this.gson = requireNonNull(gson);
  }

  @Override
  public Menu getMenu() {
    return Optional.ofNullable(appSettings.getMenu())
        .map(menuJson -> gson.fromJson(menuJson, Menu.class))
        .orElse(null);
  }

  @Override
  public String findMenuItemPath(String id) {
    return getMenu()
        .getPath(id)
        .map(List::stream)
        .map(stream -> stream.collect(Collectors.joining(",", "menu/", "")))
        .orElseThrow(NullPointerException::new);
  }
}
