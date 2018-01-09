package org.molgenis.web.menu.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.gson.AutoGson;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AutoGson(autoValueClass = AutoValue_Menu.class)
@AutoValue
@SuppressWarnings("squid:S1610") // Autovalue class cannot be an interface
public abstract class Menu implements MenuNode
{
	public abstract List<MenuNode> getItems();

	@Override
	public Optional<MenuNode> filter(Predicate<MenuItem> predicate)
	{
		List<MenuNode> items = getItems().stream()
										 .map(item -> item.filter(predicate))
										 .filter(Optional::isPresent)
										 .map(Optional::get)
										 .collect(Collectors.toList());
		return Optional.of(create(getId(), getLabel(), items))
					   .filter(menu -> !menu.getItems().isEmpty())
					   .map(MenuNode.class::cast);
	}

	@Override
	public Optional<List<String>> getPath(String id)
	{
		return filter(menuItem -> menuItem.getId().equals(id)).flatMap(
				child -> ((Menu) child).getItems().get(0).getPath(id)).map(this::prefixId);
	}

	private List<String> prefixId(List<String> path)
	{
		ImmutableList.Builder<String> result = ImmutableList.builder();
		result.add(getId());
		result.addAll(path);
		return result.build();
	}

	public static Menu create(String id, String label, List<MenuNode> items)
	{
		return new AutoValue_Menu(id, label, items);
	}
}
