package org.molgenis.core.ui.security;

import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class MolgenisAccessDecisionVoter implements AccessDecisionVoter<FilterInvocation> {
  private static final Pattern PATTERN_MENUID = Pattern.compile("/menu/([^/]+).*");
  private static final Pattern PATTERN_PLUGINID =
      Pattern.compile("(?:/plugin|/menu/[^/]+)/([^/^?]+).*");

  private final MenuReaderService menuReaderService;

  public MolgenisAccessDecisionVoter(MenuReaderService menuReaderService) {
    this.menuReaderService = Objects.requireNonNull(menuReaderService);
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return true;
  }

  @Override
  public int vote(
      Authentication authentication,
      FilterInvocation filterInvocation,
      Collection<ConfigAttribute> attributes) {
    String requestUrl = filterInvocation.getRequestUrl();

    Matcher pluginMatcher = PATTERN_PLUGINID.matcher(requestUrl);
    if (pluginMatcher.matches()) {
      String pluginId = pluginMatcher.group(1);
      return getMolgenisPermissionService().hasPermission(new PluginIdentity(pluginId), VIEW_PLUGIN)
          ? ACCESS_GRANTED
          : ACCESS_DENIED;
    }

    Matcher menuMatcher = PATTERN_MENUID.matcher(requestUrl);
    if (menuMatcher.matches()) {
      String menuId = menuMatcher.group(1);
      boolean found =
          menuReaderService
              .getMenu()
              .filter(
                  it ->
                      getMolgenisPermissionService()
                          .hasPermission(new PluginIdentity(it.getId()), VIEW_PLUGIN))
              .filter(it -> it.getPath(menuId).isPresent())
              .isPresent();
      return found ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    return ACCESS_DENIED;
  }

  /** Can't be autowired due to circular dependency resolving */
  private UserPermissionEvaluator getMolgenisPermissionService() {
    return ApplicationContextProvider.getApplicationContext()
        .getBean(UserPermissionEvaluator.class);
  }

  //  /** Can't be autowired due to circular dependency resolving */
  //  private MenuReaderService getMolgenisUi()
  //  {
  //    return ApplicationContextProvider.getApplicationContext().getBean(MenuReaderService.class);
  //  }
}
