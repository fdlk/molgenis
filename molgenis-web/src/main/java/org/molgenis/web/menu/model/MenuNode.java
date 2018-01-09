package org.molgenis.web.menu.model;

import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface MenuNode
{
	@NotEmpty
	String getId();

	@NotEmpty
	String getLabel();

	Optional<MenuNode> filter(Predicate<MenuItem> predicate);

	Optional<List<String>> getPath(String id);
}
