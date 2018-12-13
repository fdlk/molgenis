package org.molgenis.web.menu.model;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MenuTest {
  private Menu menu;

  @BeforeMethod
  public void setUp() {
    MenuItem p30 = MenuItem.create("p3_0", "lbl");
    MenuItem p31 = MenuItem.create("p3_1", "lbl");

    Menu p20 = Menu.create("p2_0", "lbl", ImmutableList.of(p30, p31));
    MenuItem p21 = MenuItem.create("p2_1", "lbl");

    MenuItem p10 = MenuItem.create("p1_0", "lbl");
    Menu p11 = Menu.create("p1_1", "lbl", ImmutableList.of(p20, p21));

    menu = Menu.create("root", "", ImmutableList.of(p10, p11));
  }

  @Test
  public void findMenuItemPath() {
    assertEquals(menu.getPath("p1_0"), Optional.of(asList("menu/root/p1_0".split("/"))));
    assertEquals(menu.getPath("p1_1"), Optional.of(asList("menu/root/p1_1".split("/"))));
    assertEquals(menu.getPath("p2_0"), Optional.of(asList("menu/p1_1/p2_0".split("/"))));
    assertEquals(menu.getPath("p2_1"), Optional.of(asList("menu/p1_1/p2_1".split("/"))));
    assertEquals(menu.getPath("p3_0"), Optional.of(asList("menu/p2_0/p3_0".split("/"))));
    assertEquals(menu.getPath("p3_1"), Optional.of(asList("menu/p2_0/p3_1".split("/"))));
    assertEquals(menu.getPath("non_existing"), Optional.empty());
  }
}
