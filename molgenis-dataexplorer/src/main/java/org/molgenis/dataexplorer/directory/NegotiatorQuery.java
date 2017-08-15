package org.molgenis.dataexplorer.directory;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorQuery.class)
public abstract class NegotiatorQuery {
  public abstract String getURL();

  public abstract List<Collection> getCollections();

  public abstract String getHumanReadable();

  @Nullable
  public abstract String getnToken();

  public static NegotiatorQuery createQuery(
      String url, List<Collection> collections, String humanReadable, String nToken) {
    return new AutoValue_NegotiatorQuery(url, collections, humanReadable, nToken);
  }
}
