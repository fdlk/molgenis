package org.molgenis.web.converter;

import com.google.gson.TypeAdapterFactory;
import org.molgenis.data.Entity;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.molgenis.web.menu.model.MenuNode;
import org.molgenis.web.support.EntitySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
public class GsonConfig {
  @Value("${environment:production}")
  private String environment;

  @Bean
  public GsonHttpMessageConverter gsonHttpMessageConverter() {
    return new MolgenisGsonHttpMessageConverter(gsonFactoryBean().getObject());
  }

  @Bean
  public TypeAdapterFactory menuTypeAdapterFactory() {
    RuntimeTypeAdapterFactory<MenuNode> menuRuntimeTypeAdapterFactory =
        RuntimeTypeAdapterFactory.of(MenuNode.class, "type");
    menuRuntimeTypeAdapterFactory.registerAutoValueSubtype(MenuItem.class, "plugin");
    menuRuntimeTypeAdapterFactory.registerAutoValueSubtype(Menu.class, "menu");
    return menuRuntimeTypeAdapterFactory;
  }

  @Bean
  public GsonFactoryBean gsonFactoryBean() {
    boolean prettyPrinting =
        environment != null && (environment.equals("development") || environment.equals("test"));

    GsonFactoryBean gsonFactoryBean = new GsonFactoryBean();
    gsonFactoryBean.registerTypeHierarchyAdapter(Entity.class, new EntitySerializer());
    gsonFactoryBean.setRegisterJavaTimeConverters(true);
    gsonFactoryBean.setDisableHtmlEscaping(true);
    gsonFactoryBean.setPrettyPrinting(prettyPrinting);
    gsonFactoryBean.setSerializeNulls(false);
    gsonFactoryBean.registerTypeAdapterFactory(menuTypeAdapterFactory());
    gsonFactoryBean.registerTypeAdapterFactory(new AutoValueTypeAdapterFactory());
    return gsonFactoryBean;
  }
}
