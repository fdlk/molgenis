package org.molgenis.graphql;

import com.bretpatterson.schemagen.graphql.ITypeFactory;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

@Component
public class GsonTypeFactory implements ITypeFactory
{
	private final Gson gson;

	@Autowired
	public GsonTypeFactory(Gson gson)
	{
		this.gson = requireNonNull(gson);
	}

	@Override
	public Object convertToType(Type type, Object arg)
	{
		return gson.fromJson(gson.toJson(arg), type);
	}
}
