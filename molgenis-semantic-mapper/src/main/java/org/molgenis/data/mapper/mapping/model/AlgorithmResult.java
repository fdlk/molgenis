package org.molgenis.data.mapper.mapping.model;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;

/** Result of applying algorithm to one source entity row */
@AutoValue
public abstract class AlgorithmResult {
  @Nullable
  public abstract Object getValue();

  @Nullable
  public abstract Exception getException();

  public abstract Entity getSourceEntity();

  public boolean isSuccess() {
    return getException() == null;
  }

  public static AlgorithmResult createSuccess(Object object, Entity sourceEntity) {
    return new AutoValue_AlgorithmResult(object, null, sourceEntity);
  }

  public static AlgorithmResult createFailure(Exception e, Entity sourceEntity) {
    return new AutoValue_AlgorithmResult(null, e, sourceEntity);
  }
}
