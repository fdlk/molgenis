package org.molgenis.graphql.plugin;

import com.google.gson.Gson;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.molgenis.graphql.schema.SchemaGenerator;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
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

	private final SchemaGenerator schemaGenerator;
	private final Gson gson;

	@Autowired
	public GraphQLPluginController(SchemaGenerator schemaGenerator, Gson gson)
	{
		super(URI);
		this.schemaGenerator = requireNonNull(schemaGenerator);
		this.gson = requireNonNull(gson);
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
		LOG.info("query: {}", gson.toJson(body));
		//TODO: cache result
		GraphQLSchema schema = schemaGenerator.createSchema();
		GraphQL graphQL = new GraphQL(schema);

		String query = (String) body.get("query");
		Map<String, Object> variables = (Map<String, Object>) body.get("variables");
		if (variables == null)
		{
			variables = new HashMap<>();
		}
		ExecutionResult executionResult = graphQL.execute(query, (Object) null, variables);
		Map<String, Object> result = new LinkedHashMap<>();
		if (executionResult.getErrors().size() > 0)
		{
			result.put("errors", executionResult.getErrors());
			LOG.error("Errors: {}", executionResult.getErrors());
		}
		result.put("data", executionResult.getData());
		LOG.info("result: {}", gson.toJson(result));
		return result;
	}

}
