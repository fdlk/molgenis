package org.molgenis.apps.model;

import static org.molgenis.apps.model.AppMetaData.DESCRIPTION;
import static org.molgenis.apps.model.AppMetaData.ICON_HREF;
import static org.molgenis.apps.model.AppMetaData.ID;
import static org.molgenis.apps.model.AppMetaData.IS_ACTIVE;
import static org.molgenis.apps.model.AppMetaData.LANDING_PAGE_HTML_TEMPLATE;
import static org.molgenis.apps.model.AppMetaData.NAME;
import static org.molgenis.apps.model.AppMetaData.RESOURCE_ZIP;
import static org.molgenis.apps.model.AppMetaData.USE_FREEMARKER_TEMPLATE;

import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.data.system.core.FreemarkerTemplate;
import org.molgenis.file.model.FileMeta;

public class App extends StaticEntity {
  public App(Entity entity) {
    super(entity);
  }

  public App(EntityType entityType) {
    super(entityType);
  }

  public App(String name, EntityType entityType) {
    super(entityType);
    setName(name);
  }

  public String getId() {
    return getString(ID);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getName() {
    return getString(NAME);
  }

  public void setName(String name) {
    set(NAME, name);
  }

  @Nullable
  public String getDescription() {
    return getString(DESCRIPTION);
  }

  public void setDescription(String description) {
    set(DESCRIPTION, description);
  }

  @Nullable
  public FileMeta getSourceFiles() {
    return getEntity(RESOURCE_ZIP, FileMeta.class);
  }

  public void setSourceFiles(String sourcesDirectory) {
    set(RESOURCE_ZIP, sourcesDirectory);
  }

  public boolean isActive() {
    return getBoolean(IS_ACTIVE);
  }

  public void setActive(boolean isActive) {
    set(IS_ACTIVE, isActive);
  }

  @Nullable
  public String getIconHref() {
    return getString(ICON_HREF);
  }

  public void setIconHref(String iconHref) {
    set(ICON_HREF, iconHref);
  }

  public Boolean getUseFreemarkerTemplate() {
    return getBoolean(USE_FREEMARKER_TEMPLATE);
  }

  public void setUseFreemarkerTemplate(boolean useFreemarkerTemplate) {
    set(USE_FREEMARKER_TEMPLATE, useFreemarkerTemplate);
  }

  @Nullable
  public FreemarkerTemplate getHtmlTemplate() {
    return getEntity(LANDING_PAGE_HTML_TEMPLATE, FreemarkerTemplate.class);
  }

  public void setHtmlTemplate(FreemarkerTemplate htmlTemplate) {
    set(LANDING_PAGE_HTML_TEMPLATE, htmlTemplate);
  }
}
