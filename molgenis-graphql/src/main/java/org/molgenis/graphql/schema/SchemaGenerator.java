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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Maps.newHashMap;
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
	private final Map<String, GraphQLOutputType> objectTypeMap = newHashMap();

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
		builder.field(fieldForEntityType(metaDataService.getEntityType("sys_md_Package")));
		builder.field(fieldForEntityType(metaDataService.getEntityType("sys_md_EntityType")));
		final GraphQLObjectType queryObject = builder.build();
		return GraphQLSchema.newSchema().query(queryObject).build();
	}

	public GraphQLFieldDefinition fieldForEntityType(EntityType entityType)
	{
		return newFieldDefinition().name(entityType.getFullyQualifiedName()).description(entityType.getDescription())
				.type(new GraphQLList(new GraphQLNonNull(createGraphQLType(entityType)))).dataFetcher(env ->
				{
					LOG.info("Fetching {} rows ...", entityType.getName());
					final List<Entity> result = dataService.findAll(entityType.getFullyQualifiedName())
							.collect(Collectors.toList());
					LOG.info("Found {}", result);
					return result;
				}).build();
	}

	public GraphQLOutputType createGraphQLType(EntityType entityType)
	{
		final String fullyQualifiedName = entityType.getFullyQualifiedName();
		if (objectTypeMap.containsKey(fullyQualifiedName))
		{
			return objectTypeMap.get(fullyQualifiedName);
		}
		//recursion is interesting...
		objectTypeMap.put(fullyQualifiedName, new GraphQLTypeReference(fullyQualifiedName));
		final GraphQLObjectType.Builder builder = newObject().name(fullyQualifiedName)
				.description(entityType.getDescription());
		for (Attribute attribute : entityType.getAtomicAttributes())
		{
			builder.field(createFieldDefinition(entityType, attribute));
		}
		final GraphQLObjectType result = builder.build();
		objectTypeMap.put(fullyQualifiedName, result);
		return result;
	}

	public GraphQLFieldDefinition createFieldDefinition(EntityType entityType, Attribute attribute)
	{
		return newFieldDefinition().name(attribute.getName()).description(attribute.getDescription())
				.type(createOutputType(entityType.getFullyQualifiedName(), attribute)).dataFetcher(env ->
				{
					Object source = env.getSource();
					if (source == null) return null;
					LOG.info("Fetching {}...", attribute.getName());
					Object result = ((Entity) source).get(attribute.getName());
					if (result instanceof Iterable)
					{
						result = StreamSupport.stream(((Iterable) result).spliterator(), false);
					}
					if (result instanceof Stream)
					{
						result = ((Stream) result).collect(Collectors.toList());
					}
					LOG.info("Found {}", result);
					return result;
				}).build();
	}

	public GraphQLOutputType createOutputType(String fullyQualifiedName, Attribute attribute)
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
				result = GraphQLFloat;
				break;
			case ENUM:
				result = createEnumType(attribute);
				break;
			case XREF:
			case CATEGORICAL:
			case FILE:
				result = createRefType(attribute, fullyQualifiedName);
				break;
			case MREF:
			case CATEGORICAL_MREF:
			case ONE_TO_MANY:
				result = new GraphQLList(new GraphQLNonNull(createRefType(attribute, fullyQualifiedName)));
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

	public GraphQLOutputType createRefType(Attribute attribute, String fullyQualifiedName)
	{
		final EntityType refEntity = attribute.getRefEntity();
		if (refEntity.getFullyQualifiedName().equals(fullyQualifiedName))
		{
			return new GraphQLTypeReference(fullyQualifiedName);
		}
		else
		{
			return createGraphQLType(refEntity);
		}
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
