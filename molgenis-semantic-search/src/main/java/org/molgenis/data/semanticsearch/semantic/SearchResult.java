package org.molgenis.data.semanticsearch.semantic;

public interface SearchResult<T> {
  T getItem();

  int getRelevance();
}
