package org.molgenis.util;

import com.google.gson.TypeAdapterFactory;
import org.molgenis.data.Entity;
import org.molgenis.gson.AutoValueTypeAdapterFactory;
import org.molgenis.gson.RuntimeTypeAdapterFactory;
import org.molgenis.ui.menu.model.Menu;
import org.molgenis.ui.menu.model.MenuItem;
import org.molgenis.ui.menu.model.MenuNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig
{
	@Value("${environment:production}")
	private String environment;

	@Bean
	public GsonHttpMessageConverter gsonHttpMessageConverter() throws Exception
	{
		return new GsonHttpMessageConverter(gsonFactoryBean().getObject());
	}

	@Bean
	public TypeAdapterFactory menuTypeAdapterFactory()
	{
		RuntimeTypeAdapterFactory<MenuNode> menuRuntimeTypeAdapterFactory = RuntimeTypeAdapterFactory.of(MenuNode.class,
				"type");
		menuRuntimeTypeAdapterFactory.registerSubtype(MenuItem.class, "plugin");
		menuRuntimeTypeAdapterFactory.registerSubtype(Menu.class, "menu");
		return menuRuntimeTypeAdapterFactory;
	}

	@Bean
	public GsonFactoryBean gsonFactoryBean()
	{
		boolean prettyPrinting =
				environment != null && (environment.equals("development") || environment.equals("test"));

		GsonFactoryBean gsonFactoryBean = new GsonFactoryBean();
		gsonFactoryBean.registerTypeHierarchyAdapter(Entity.class, new EntitySerializer());
		gsonFactoryBean.setRegisterJavaTimeConverters(true);
		gsonFactoryBean.setDisableHtmlEscaping(true);
		gsonFactoryBean.setPrettyPrinting(prettyPrinting);
		gsonFactoryBean.setSerializeNulls(false);
		gsonFactoryBean.registerTypeAdapterFactory(new AutoValueTypeAdapterFactory());
		gsonFactoryBean.registerTypeAdapterFactory(menuTypeAdapterFactory());
		return gsonFactoryBean;
	}
}