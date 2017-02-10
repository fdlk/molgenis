package org.molgenis.graphql.plugin;

import com.bretpatterson.schemagen.graphql.GraphQLSchemaBuilder;
import com.google.common.collect.ImmutableList;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.molgenis.graphql.GsonTypeFactory;
import org.molgenis.graphql.Metadata;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.molgenis.graphql.plugin.GraphQLPluginController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class GraphQLPluginController extends MolgenisPluginController
{
	public static final String ID = "graphql";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final Logger LOG = LoggerFactory.getLogger(GraphQLPluginController.class);

	private final GraphQL graphQL;

	@Autowired
	public GraphQLPluginController(Metadata metadata, GsonTypeFactory gsonTypeFactory)
	{
		super(URI);
		GraphQLSchema schema = GraphQLSchemaBuilder.newBuilder().registerTypeFactory(gsonTypeFactory)
				.registerGraphQLControllerObjects(ImmutableList.of(metadata)).build();
		graphQL = new GraphQL(schema);
		LOG.info("constructe");
	}

	@RequestMapping(method = GET)
	public String serve()
	{
		return "view-graphql";
	}

	@RequestMapping(method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object executeOperation(@RequestBody Map body)
	{
		String query = (String) body.get("query");
		Map<String, Object> variables = (Map<String, Object>) body.get("variables");
		ExecutionResult executionResult = graphQL.execute(query, (Object) null, variables);
		Map<String, Object> result = new LinkedHashMap<>();
		if (executionResult.getErrors().size() > 0)
		{
			result.put("errors", executionResult.getErrors());
			LOG.error("Errors: {}", executionResult.getErrors());
		}
		result.put("data", executionResult.getData());
		return result;
	}

}
