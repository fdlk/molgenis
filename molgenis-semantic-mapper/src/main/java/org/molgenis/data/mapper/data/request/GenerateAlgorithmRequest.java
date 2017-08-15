package org.molgenis.data.mapper.data.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GenerateAlgorithmRequest.class)
public abstract class GenerateAlgorithmRequest {
  @NotNull
  public abstract String getTargetEntityTypeId();

  @NotNull
  public abstract String getTargetAttributeName();

  @NotNull
  public abstract String getSourceEntityTypeId();

  @NotEmpty
  public abstract List<String> getSourceAttributes();
}
