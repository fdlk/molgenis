package org.molgenis.data.mapper.data.request;

import com.google.auto.value.AutoValue;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_RemoveTagRequest.class)
public abstract class RemoveTagRequest {
  @NotNull
  public abstract String getEntityTypeId();

  @NotNull
  public abstract String getAttributeName();

  @NotNull
  public abstract String getRelationIRI();

  @NotEmpty
  public abstract String getOntologyTermIRI();
}
