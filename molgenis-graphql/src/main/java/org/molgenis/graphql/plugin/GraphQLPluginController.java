package org.molgenis.graphql.plugin;

import com.bretpatterson.schemagen.graphql.GraphQLSchemaBuilder;
import com.google.common.collect.ImmutableList;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.graphql.GsonTypeFactory;
import org.molgenis.graphql.HelloWorld;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.util.Objects.requireNonNull;
import static org.molgenis.graphql.plugin.GraphQLPluginController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class GraphQLPluginController extends MolgenisPluginController
{
	public static final String ID = "graphql";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final Logger LOG = LoggerFactory.getLogger(GraphQLPluginController.class);

	private final MetaDataService metaDataService;
	private GsonTypeFactory gsonTypeFactory;

	@Autowired
	public GraphQLPluginController(MetaDataService metaDataService, GsonTypeFactory gsonTypeFactory)
	{
		super(URI);
		this.metaDataService = requireNonNull(metaDataService);
		this.gsonTypeFactory = requireNonNull(gsonTypeFactory);
	}

	@RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public
	@ResponseBody
	Object query(@RequestParam String query)
	{
		LOG.info("query: '{}'", query);
		HelloWorld helloWorld = new HelloWorld();
		GraphQLSchema schema = GraphQLSchemaBuilder.newBuilder().registerTypeFactory(gsonTypeFactory)
				.registerGraphQLControllerObjects(ImmutableList.of(helloWorld)).build();

		// now lets execute a query against the schema
		ExecutionResult result = new GraphQL(schema).execute(query);
		if (result.getErrors().size() != 0)
		{
			return result.getErrors();
		}
		else
		{
			return result.getData();
		}
	}

}
