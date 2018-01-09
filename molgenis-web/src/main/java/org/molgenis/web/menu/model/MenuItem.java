package org.molgenis.web.menu.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MenuItem.class)
@SuppressWarnings("squid:S1610") // Autovalue class cannot be an interface
public abstract class MenuItem implements MenuNode
{
	@Nullable
	public abstract String getParams();

	@Override
	public Optional<MenuNode> filter(Predicate<MenuItem> predicate)
	{
		return Optional.of(this).filter(predicate).map(MenuNode.class::cast);
	}

	@Override
	public Optional<List<String>> getPath(String id)
	{
		return Optional.of(getId()).filter(id::equals).map(Collections::singletonList);
	}

	public static MenuItem create(String id, String label)
	{
		return new AutoValue_MenuItem(id, label, null);
	}
}
