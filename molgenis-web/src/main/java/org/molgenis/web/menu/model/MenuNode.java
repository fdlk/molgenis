package org.molgenis.web.menu.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.validation.constraints.NotEmpty;

public interface MenuNode {
  @NotEmpty
  String getId();

  @NotEmpty
  String getLabel();

  Optional<MenuNode> filter(Predicate<MenuItem> predicate);

  Optional<List<String>> getPath(String id);

  Optional<MenuItem> firstItem();
}
