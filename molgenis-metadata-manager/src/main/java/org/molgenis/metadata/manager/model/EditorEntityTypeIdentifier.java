package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeIdentifier.class)
public abstract class EditorEntityTypeIdentifier {
  public abstract String getId();

  @Nullable
  public abstract String getLabel();

  public static EditorEntityTypeIdentifier create(String id, @Nullable String label) {
    return new AutoValue_EditorEntityTypeIdentifier(id, label);
  }
}
