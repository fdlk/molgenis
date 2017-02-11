package org.molgenis.graphql.schema;

import graphql.schema.*;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_RANDOM;

/**
 * Generates schema for dynamic entities.
 */
@Component
public class SchemaGenerator
{
	private static final Logger LOG = LoggerFactory.getLogger(SchemaGenerator.class);

	private final IdGenerator idGenerator;
	private final MetaDataService metaDataService;
	private final DataService dataService;

	@Autowired
	public SchemaGenerator(MetaDataService metaDataService, IdGenerator idGenerator, DataService dataService)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.idGenerator = requireNonNull(idGenerator);
		this.dataService = requireNonNull(dataService);
	}

	public GraphQLSchema createSchema()
	{
		final GraphQLObjectType.Builder builder = newObject().name("query");
		builder.field(fieldForEntityType(metaDataService.getEntityType("it_emx_datatypes_TypeTestRef")));
		builder.field(fieldForEntityType(metaDataService.getEntityType("it_emx_datatypes_TypeTest")));

		final GraphQLObjectType queryObject = builder.build();
		return GraphQLSchema.newSchema().query(queryObject).build();
	}

	public GraphQLFieldDefinition fieldForEntityType(EntityType entityType)
	{
		return newFieldDefinition().name(entityType.getFullyQualifiedName()).description(entityType.getDescription())
				.type(new GraphQLList(createObjectType(entityType))).dataFetcher(env ->
				{
					LOG.info("Fetching {} rows ...", entityType.getName());
					final List<Entity> result = dataService.findAll(entityType.getFullyQualifiedName())
							.collect(Collectors.toList());
					LOG.info("Found {}", result);
					return result;
				}).build();
	}

	public GraphQLObjectType createObjectType(EntityType entityType)
	{
		final GraphQLObjectType.Builder builder = newObject().name(entityType.getFullyQualifiedName())
				.description(entityType.getDescription());
		for (Attribute attribute : entityType.getAtomicAttributes())
		{
			builder.field(createFieldDefinition(attribute));
		}
		return builder.build();
	}

	public GraphQLFieldDefinition createFieldDefinition(Attribute attribute)
	{
		return newFieldDefinition().name(attribute.getName()).description(attribute.getDescription())
				.type(createOutputType(attribute)).dataFetcher(env ->
				{
					Object source = env.getSource();
					if (source == null) return null;
					LOG.info("Fetching {}...", attribute.getName());
					final Object result = ((Entity) source).get(attribute.getName());
					LOG.info("Found {}", result);
					return result;
				}).build();
	}

	public GraphQLOutputType createOutputType(Attribute attribute)
	{
		GraphQLOutputType result = null;
		switch (attribute.getDataType())
		{
			case STRING:
			case TEXT:
			case HTML:
			case SCRIPT:
			case EMAIL:
			case HYPERLINK:
				result = GraphQLString;
				break;
			case INT:
				result = GraphQLInt;
				break;
			case LONG:
				result = GraphQLLong;
				break;
			case BOOL:
				result = GraphQLBoolean;
				break;
			case DECIMAL:
				//TODO: is this correct?
				result = GraphQLFloat;
				break;
			case ENUM:
				result = createEnumType(attribute);
				break;
			case XREF:
			case CATEGORICAL:
			case FILE:
				result = createObjectType(attribute.getRefEntity());
				break;
			case MREF:
			case CATEGORICAL_MREF:
			case ONE_TO_MANY:
				result = new GraphQLList(createObjectType(attribute.getRefEntity()));
				break;
			case DATE:
				result = MolgenisGraphQLScalars.GraphQLDate;
				break;
			case DATE_TIME:
				result = MolgenisGraphQLScalars.GraphQLDateTime;
				break;
		}
		if (!attribute.isNillable())
		{
			result = new graphql.schema.GraphQLNonNull(result);
		}
		return result;
	}

	public GraphQLEnumType createEnumType(Attribute attribute)
	{
		GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
				.name(attribute.getName() + "_" + idGenerator.generateId(SHORT_RANDOM))
				.description(attribute.getDescription());
		for (String option : attribute.getEnumOptions())
		{
			builder.value(option);
		}
		return builder.build();
	}
}
