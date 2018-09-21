package org.molgenis.core.ui.security;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.molgenis.web.UiMenuItemType.MENU;
import static org.molgenis.web.UiMenuItemType.PLUGIN;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisUiMenuPermissionDecoratorTest extends AbstractMockitoTest {
  private MolgenisUiMenuPermissionDecorator molgenisUiMenuPermissionDecorator;
  @Mock private UiMenu uiMenu;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    molgenisUiMenuPermissionDecorator =
        new MolgenisUiMenuPermissionDecorator(uiMenu, userPermissionEvaluator);
  }

  @Test
  public void testGetActiveItem() {
    UiMenuItem menuItem = when(mock(UiMenuItem.class).getType()).thenReturn(MENU).getMock();
    UiMenuItem notPermittedPluginItem =
        when(mock(UiMenuItem.class).getType()).thenReturn(PLUGIN).getMock();
    when(notPermittedPluginItem.getId()).thenReturn("pluginNotPermitted");
    UiMenuItem permittedPluginItem =
        when(mock(UiMenuItem.class).getType()).thenReturn(PLUGIN).getMock();
    when(permittedPluginItem.getId()).thenReturn("pluginPermitted");
    doReturn(false)
        .when(userPermissionEvaluator)
        .hasPermission(new PluginIdentity("pluginNotPermitted"), VIEW_PLUGIN);
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new PluginIdentity("pluginPermitted"), VIEW_PLUGIN);
    when(uiMenu.getItems())
        .thenReturn(asList(menuItem, notPermittedPluginItem, permittedPluginItem));

    assertEquals(molgenisUiMenuPermissionDecorator.getActiveItem(), permittedPluginItem);
  }
}
