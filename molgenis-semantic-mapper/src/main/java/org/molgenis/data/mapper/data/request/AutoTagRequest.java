package org.molgenis.data.mapper.data.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AutoTagRequest.class)
public abstract class AutoTagRequest {
  @NotBlank
  public abstract String getEntityTypeId();

  @NotEmpty
  public abstract List<String> getOntologyIds();
}
