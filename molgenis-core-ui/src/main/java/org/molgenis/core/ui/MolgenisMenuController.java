package org.molgenis.core.ui;

import static java.time.Instant.now;
import static java.time.format.FormatStyle.MEDIUM;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.menumanager.MenuManagerController.URI;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.web.PluginAttributes.KEY_CONTEXT_URL;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownPluginException;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.molgenis.web.menu.model.MenuNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

@Controller
@RequestMapping(URI)
public class MolgenisMenuController {
  private static final Logger LOG = LoggerFactory.getLogger(MolgenisMenuController.class);

  public static final String URI = "/menu";

  private static final String KEY_MENU_ID = "menu_id";
  private static final String KEY_MOLGENIS_VERSION = "molgenis_version";
  private static final String KEY_MOLGENIS_BUILD_DATE = "molgenis_build_date";

  private final MenuReaderService menuReaderService;
  private final String molgenisVersion;
  private final String molgenisBuildDate;
  private final DataService dataService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  MolgenisMenuController(
      MenuReaderService menuReaderService,
      @Value("${molgenis.version}") String molgenisVersion,
      @Value("${molgenis.build.date}") String molgenisBuildDate,
      DataService dataService,
      UserPermissionEvaluator permissionEvaluator) {
    this.dataService = requireNonNull(dataService);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.molgenisVersion = requireNonNull(molgenisVersion, "molgenisVersion is null");
    requireNonNull(molgenisBuildDate, "molgenisBuildDate is null");

    // workaround for Eclipse bug: https://github.com/molgenis/molgenis/issues/2667
    this.molgenisBuildDate =
        molgenisBuildDate.equals("${maven.build.timestamp}")
            ? DateTimeFormatter.ofLocalizedDateTime(MEDIUM).format(now()) + " by Eclipse"
            : molgenisBuildDate;
    this.userPermissionEvaluator = requireNonNull(permissionEvaluator);
  }

  @RequestMapping
  public String forwardDefaultMenuDefaultPlugin(Model model) {
    Optional<MenuNode> optionalMenu = filterMenu();

    if (!optionalMenu.isPresent()) {
      LOG.warn("main menu does not contain any (accessible) items");
      return "forward:/login";
    }

    MenuNode menu = optionalMenu.get();
    String menuId = menu.getId();
    model.addAttribute(KEY_MENU_ID, menuId);

    Optional<MenuItem> optionalActiveItem = menu.firstItem();

    if (!optionalActiveItem.isPresent()) {
      LOG.warn("main menu does not contain any (accessible) items");
      return "forward:/login";
    }

    MenuItem activeItem = optionalActiveItem.get();
    String pluginId = activeItem.getId();

    String contextUri = URI + '/' + menuId + '/' + pluginId;
    model.addAttribute(KEY_CONTEXT_URL, contextUri);
    model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
    model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildDate);

    return getForwardPluginUri(activeItem.getId(), null, activeItem.getParams());
  }

  private Optional<MenuNode> filterMenu() {
    return menuReaderService
        .getMenu()
        .filter(
            item ->
                userPermissionEvaluator.hasPermission(
                    new PluginIdentity(item.getId()), PluginPermission.VIEW_PLUGIN));
  }

  @RequestMapping("/{menuId}")
  public String forwardMenuDefaultPlugin(@Valid @NotNull @PathVariable String menuId, Model model) {
    Optional<MenuNode> optionalMenu = filterMenu();

    if (!optionalMenu.isPresent()) {
      LOG.warn("main menu does not contain any (accessible) items");
      return "forward:/login";
    }
    Menu wholeMenu = (Menu) optionalMenu.get();
    MenuNode selectedMenu =
        wholeMenu
            .getItems()
            .stream()
            .filter(child -> child.getId().equals(menuId))
            .findFirst()
            .orElseThrow(
                () -> new RuntimeException("menu with id [" + menuId + "] does not exist"));
    model.addAttribute(KEY_MENU_ID, menuId);

    String pluginId = selectedMenu.firstItem().map(MenuItem::getId).orElse(VoidPluginController.ID);

    String contextUri = URI + '/' + menuId + '/' + pluginId;
    model.addAttribute(KEY_CONTEXT_URL, contextUri);
    model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
    model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildDate);
    return getForwardPluginUri(pluginId);
  }

  @RequestMapping("/{menuId}/{pluginId}/**")
  public String forwardMenuPlugin(
      HttpServletRequest request,
      @Valid @NotNull @PathVariable String menuId,
      @Valid @NotNull @PathVariable String pluginId,
      Model model) {
    String contextUri = URI + '/' + menuId + '/' + pluginId;
    String mappingUri =
        (String) (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
    String remainder = mappingUri.substring(contextUri.length());

    model.addAttribute(KEY_CONTEXT_URL, contextUri);
    model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
    model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildDate);
    model.addAttribute(KEY_MENU_ID, menuId);
    return getForwardPluginUri(pluginId, remainder);
  }

  private String getForwardPluginUri(String pluginId) {
    return getForwardPluginUri(pluginId, null);
  }

  private String getForwardPluginUri(String pluginId, @Nullable String pathRemainder) {
    return getForwardPluginUri(pluginId, pathRemainder, null);
  }

  /** package-private for testability */
  String getForwardPluginUri(
      String pluginId, @Nullable String pathRemainder, @Nullable String queryString) {
    // get plugin path with elevated permissions because the anonymous user can also request plugins
    Plugin plugin = runAsSystem(() -> dataService.findOneById(PLUGIN, pluginId, Plugin.class));
    if (plugin == null) {
      throw new UnknownPluginException(pluginId);
    }

    StringBuilder strBuilder = new StringBuilder("forward:");
    strBuilder.append(PluginController.PLUGIN_URI_PREFIX).append(plugin.getPath());
    // If you do not append the trailing slash the queryString will be appended by an unknown code
    // snippet.
    // The trailing slash is needed for clients to serve resources 'relative' to the URI-path.
    if (pluginId.startsWith("app")) {
      strBuilder.append("/");
    }
    if (pathRemainder != null && !pathRemainder.isEmpty()) {
      strBuilder.append("/").append(pathRemainder);
    }
    if (queryString != null && !queryString.isEmpty()) {
      strBuilder.append('?').append(queryString);
    }
    return strBuilder.toString();
  }

  /** Plugin without content */
  @Controller
  @RequestMapping(VoidPluginController.URI)
  public static class VoidPluginController extends PluginController {
    public static final String ID = "void";
    public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

    public VoidPluginController() {
      super(URI);
    }

    @GetMapping
    public String init() {
      return "view-void";
    }
  }
}
