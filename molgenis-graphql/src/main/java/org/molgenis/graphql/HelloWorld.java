package org.molgenis.graphql;

import com.bretpatterson.schemagen.graphql.annotations.GraphQLController;
import com.bretpatterson.schemagen.graphql.annotations.GraphQLQuery;

@GraphQLController
public class HelloWorld
{
	@GraphQLQuery(name = "helloWorld")
	public String helloWorld()
	{
		return "Hello World!";
	}

}
