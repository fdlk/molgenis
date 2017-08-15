package org.molgenis.data.mapper.data.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetOntologyTermRequest.class)
public abstract class GetOntologyTermRequest {
  @NotBlank
  public abstract String getSearchTerm();

  @NotEmpty
  public abstract List<String> getOntologyIds();
}
