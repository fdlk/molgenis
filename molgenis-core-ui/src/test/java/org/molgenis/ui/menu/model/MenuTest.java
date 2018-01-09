package org.molgenis.ui.menu.model;

import com.google.gson.Gson;
import org.molgenis.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { GsonConfig.class })
public class MenuTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private Gson gson;

	private Menu menu;

	@BeforeMethod
	public void beforeMethod()
	{
		String json = "{\"type\": \"menu\", \"id\": \"main\", \"label\": \"Home\", \"items\": [ {\"type\": \"plugin\", \"id\": \"home\", \"label\": \"Home\" },{\"type\": \"plugin\", \"id\": \"account\", \"label\": \"Account\" }]}";
		menu = (Menu) gson.fromJson(json, MenuNode.class);
	}

	@Test
	public void testFilterFalse()
	{
		assertEquals(menu.filter((node) -> false), Optional.empty());
	}

	@Test
	public void testFilterTrue()
	{
		assertEquals(menu.filter((node) -> true), Optional.of(menu));
	}

	@Test
	public void testFilterAccount()
	{
		assertEquals(menu.filter((node) -> "account".equals(node.getId())),
				Optional.of(Menu.create("main", "Home", singletonList(MenuItem.create("account", "Account")))));
	}

}