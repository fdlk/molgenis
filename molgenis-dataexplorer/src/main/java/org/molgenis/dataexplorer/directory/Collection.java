package org.molgenis.dataexplorer.directory;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Collection.class)
public abstract class Collection {
  public abstract String getCollectionId();

  @Nullable
  public abstract String getBiobankId();

  public static Collection createCollection(String collectionId, String biobankId) {
    return new AutoValue_Collection(collectionId, biobankId);
  }
}
