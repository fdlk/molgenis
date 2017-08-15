package org.molgenis.auth;

import static org.molgenis.auth.GroupMetaData.ACTIVE;
import static org.molgenis.auth.GroupMetaData.ID;
import static org.molgenis.auth.GroupMetaData.NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class Group extends StaticEntity {
  public Group(Entity entity) {
    super(entity);
  }

  public Group(EntityType entityType) {
    super(entityType);
    setDefaultValues();
  }

  public Group(String id, EntityType entityType) {
    super(entityType);
    setId(id);
    setDefaultValues();
  }

  public boolean isActive() {
    Boolean active = getBoolean(ACTIVE);
    return active != null ? active : false;
  }

  public void setActive(boolean active) {
    set(ACTIVE, active);
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

  private void setDefaultValues() {
    setActive(true);
  }
}
