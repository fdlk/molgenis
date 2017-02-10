package org.molgenis.graphql.plugin;

import com.bretpatterson.schemagen.graphql.GraphQLSchemaBuilder;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.apache.commons.io.IOUtils;
import org.molgenis.graphql.GsonTypeFactory;
import org.molgenis.graphql.HelloWorld;
import org.molgenis.graphql.Metadata;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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

	private final Metadata metadata;
	private final GraphQL graphQL;
	private final Gson gson;

	@Autowired
	public GraphQLPluginController(Metadata metadata, GsonTypeFactory gsonTypeFactory, Gson gson)
	{
		super(URI);
		this.metadata = requireNonNull(metadata);
		this.gson = requireNonNull(gson);
		HelloWorld helloWorld = new HelloWorld();
		GraphQLSchema schema = GraphQLSchemaBuilder.newBuilder().registerTypeFactory(gsonTypeFactory)
				.registerGraphQLControllerObjects(ImmutableList.of(helloWorld)).build();
		graphQL = new GraphQL(schema);
		LOG.info("constructd");
	}

	@RequestMapping(method = GET)
	public String serve()
	{
		return "view-graphql";
	}

	@RequestMapping(method = POST, produces = APPLICATION_JSON_VALUE)
	public
	@ResponseBody
	Object query(HttpServletRequest request) throws IOException
	{
		String queryJson = IOUtils.toString(request.getInputStream());
		Map gsonMap = gson.fromJson(queryJson, Map.class);
		String query = gsonMap.get("query").toString();
		LOG.info("query: '{}'", query);

		// now lets execute a query against the schema
		ExecutionResult result = graphQL.execute(query);
		if (result.getErrors().size() != 0)
		{
			LOG.error("Failed to execute query. Errors: {}", result.getErrors());
			return result.getErrors();
		}
		else
		{
			LOG.info("Executed query. Data: {}", result.getData());
			return result.getData();
		}
	}

}
