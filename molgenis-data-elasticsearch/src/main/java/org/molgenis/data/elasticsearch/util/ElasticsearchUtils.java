package org.molgenis.data.elasticsearch.util;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;
import static org.elasticsearch.client.Requests.refreshRequest;
import static org.molgenis.data.elasticsearch.request.SourceFilteringGenerator.toFetchFields;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.optimize.OptimizeResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Facade in front of the ElasticSearch client.
 */
public class ElasticsearchUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchUtils.class);
	private final Client client;
	private final SearchRequestGenerator generator = new SearchRequestGenerator();

	public ElasticsearchUtils(Client client)
	{
		this.client = client;
	}

	public void deleteIndex(String index)
	{
		client.admin().indices().prepareDelete(index).execute().actionGet();
	}

	public boolean indexExists(String index)
	{
		return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
	}

	// Wait until elasticsearch is ready
	public void waitForYellowStatus()
	{
		client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

	private void refreshIndex(String index)
	{
		client.admin().indices().refresh(refreshRequest(index)).actionGet();
	}

	public void waitForCompletion(BulkProcessor bulkProcessor)
	{
		try
		{
			boolean isCompleted = bulkProcessor.awaitClose(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			if (!isCompleted)
			{
				throw new MolgenisDataException("Failed to complete bulk request within the given time");
			}
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ImmutableOpenMap<String, MappingMetaData> getMappings(String indexName)
	{
		LOG.trace("Retrieving Elasticsearch mappings ...");
		GetMappingsResponse mappingsResponse = client.admin().indices().prepareGetMappings(indexName).get();
		LOG.debug("Retrieved Elasticsearch mappings");
		return mappingsResponse.getMappings().get(indexName);
	}

	public void putMapping(String index, XContentBuilder jsonBuilder, String entityName) throws IOException
	{
		if (LOG.isTraceEnabled()) LOG.trace("Creating Elasticsearch mapping [{}] ...", jsonBuilder.string());

		PutMappingResponse response = client.admin().indices().preparePutMapping(index)
				.setType(sanitizeMapperType(entityName)).setSource(jsonBuilder).get();

		if (!response.isAcknowledged())
		{
			throw new ElasticsearchException(
					"Creation of mapping for documentType [" + entityName + "] failed. Response=" + response);
		}
		if (LOG.isDebugEnabled()) LOG.debug("Created Elasticsearch mapping [{}]", jsonBuilder.string());
	}

	public void refresh(String index)
	{
		LOG.trace("Refreshing Elasticsearch index [{}] ...", index);
		refreshIndex(index);
		LOG.debug("Refreshed Elasticsearch index [{}]", index);
	}

	public long getCount(Query<Entity> q, EntityMetaData entityMetaData, String type, String indexName)
	{
		if (q != null)
		{
			LOG.trace("Counting Elasticsearch [{}] docs using query [{}] ...", type, q);
		}
		else
		{
			LOG.trace("Counting Elasticsearch [{}] docs", type);
		}
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
		generator.buildSearchRequest(searchRequestBuilder, type, SearchType.COUNT, q, null, null, null, entityMetaData);
		SearchResponse searchResponse = searchRequestBuilder.get();
		if (searchResponse.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Search failed. Returned headers:" + searchResponse.getHeaders());
		}
		long count = searchResponse.getHits().totalHits();
		long ms = searchResponse.getTookInMillis();
		if (q != null)
		{
			LOG.debug("Counted {} Elasticsearch [{}] docs using query [{}] in {}ms", count, type, q, ms);
		}
		else
		{
			LOG.debug("Counted {} Elasticsearch [{}] docs in {}ms", count, type, ms);
		}
		return count;
	}

	public void optimizeIndex(String indexName)
	{
		LOG.trace("Optimizing Elasticsearch index [{}] ...", indexName);
		// setMaxNumSegments(1) fully optimizes the index
		OptimizeResponse response = client.admin().indices().prepareOptimize(indexName).setMaxNumSegments(1).get();
		if (response.getFailedShards() > 0)
		{
			throw new ElasticsearchException("Optimize failed. Returned headers:" + response.getHeaders());
		}
		LOG.debug("Optimized Elasticsearch index [{}]", indexName);
	}

	/**
	 * Deletes a document from an index.
	 *
	 * @param index the name of the index
	 * @param id    the ID of the document
	 * @param type  tye type of the document
	 */
	public void deleteById(String index, String id, String type)
	{
		LOG.trace("Deleting Elasticsearch '{}' doc with id [{}] ...", type, id);
		GetResponse response = client.prepareGet(index, type, id).get();
		LOG.debug("Retrieved document type [{}] with id [{}] in index [{}]", type, id, index);
		if (response.isExists())
		{
			client.prepareDelete(index, type, id).get();
		}
		LOG.debug("Deleted Elasticsearch '{}' doc with id [{}]", type, id);
	}

	/**
	 * Checks if a type exists in an index.
	 *
	 * @param type      the name of the type
	 * @param indexName the name of the index
	 * @return boolean indicating if the type exists in the index
	 */
	public boolean isTypeExists(String type, String indexName)
	{
		LOG.trace("Check whether type [{}] exists in index [{}]...", type, indexName);
		TypesExistsResponse typesExistsResponse = client.admin().indices().prepareTypesExists(indexName).setTypes(type)
				.get();
		boolean typeExists = typesExistsResponse.isExists();
		LOG.trace("Checked whether type [{}] exists in index [{}]", type, indexName);
		return typeExists;
	}

	/**
	 * Tries to delete the mapping for a type in an index.
	 *
	 * @param type      name of the type
	 * @param indexName name of the index
	 * @return boolean indicating success of the deletion
	 */
	public boolean deleteMapping(String type, String indexName)
	{
		DeleteMappingResponse deleteMappingResponse = client.admin().indices().prepareDeleteMapping(indexName)
				.setType(type).get();
		return deleteMappingResponse.isAcknowledged();
	}

	/**
	 * Deletes all documents of a type in an index.
	 *
	 * @param type      tye name of the type of the documents
	 * @param indexName the name of the index
	 * @return boolean indicating success of the deletion
	 */
	public boolean deleteAllDocumentsOfType(String type, String indexName)
	{
		LOG.trace("Deleting all Elasticsearch '{}' docs ...", type);
		DeleteByQueryResponse deleteByQueryResponse = client.prepareDeleteByQuery(indexName)
				.setQuery(new TermQueryBuilder("_type", type)).get();

		if (deleteByQueryResponse != null)
		{
			IndexDeleteByQueryResponse idbqr = deleteByQueryResponse.getIndex(indexName);
			if (idbqr != null && idbqr.getFailedShards() > 0)
			{
				return false;
			}
		}
		LOG.debug("Deleted all Elasticsearch '{}' docs.", type);
		return true;
	}

	public Optional<Map<String, Object>> getDocument(String type, String id, String indexName)
	{
		LOG.trace("Retrieving Elasticsearch [{}] doc with id [{}] ...", type, id);
		GetResponse response = client.prepareGet(indexName, type, id).get();
		LOG.debug("Retrieved Elasticsearch [{}] doc with id [{}]", type, id);
		return getSourceFromResponse(response);
	}

	public Optional<Map<String, Object>> getDocument(String type, String id, String[] fetchFields, String indexName)
	{
		LOG.trace("Retrieving Elasticsearch [{}] doc with id [{}] and fetchFields {} ...", type, id,
				asList(fetchFields));
		GetResponse response = client.prepareGet(indexName, type, id).setFetchSource(fetchFields, null).get();
		LOG.debug("Retrieved Elasticsearch [{}] doc with id [{}] and fetchFields {}", type, id, asList(fetchFields));
		return getSourceFromResponse(response);
	}

	private static Optional<Map<String, Object>> getSourceFromResponse(GetResponse response)
	{
		return response.isExists() ? Optional.of(response.getSource()) : Optional.empty();
	}

	public void flushIndex(String indexName)
	{
		LOG.trace("Flushing Elasticsearch index [{}] ...", indexName);
		client.admin().indices().prepareFlush(indexName).get();
		LOG.debug("Flushed Elasticsearch index [{}]", indexName);
	}

	public SearchResponse search(SearchType searchType, SearchRequest request, EntityMetaData entityMetaData,
			String documentType, String indexName)
	{
		SearchRequestBuilder builder = client.prepareSearch(indexName);
		generator
				.buildSearchRequest(builder, documentType, searchType, request.getQuery(), request.getAggregateField1(),
						request.getAggregateField2(), request.getAggregateFieldDistinct(), entityMetaData);
		LOG.trace("*** REQUEST\n{}", builder);
		SearchResponse response = builder.get();
		LOG.trace("*** RESPONSE\n{}", response);
		return response;
	}

	public Stream<Map<String, Object>> getDocuments(String type, Stream<Object> entityIds, String indexName)
	{
		LOG.trace("Retrieving Elasticsearch [{}] docs with ids [{}] ...", type, entityIds);
		MultiGetRequestBuilder request = client.prepareMultiGet();
		entityIds.forEach(id -> {
			MultiGetRequest.Item item = new MultiGetRequest.Item(indexName, type, toElasticsearchId(id));
			request.add(item);
		});
		if (request.request().getItems().isEmpty())
		{
			return Stream.empty();
		}
		MultiGetResponse response = request.get();
		LOG.debug("Retrieved Elasticsearch [{}] docs with ids [{}]", type, entityIds);

		return combineResponses(response);
	}

	public Stream<Map<String, Object>> getDocuments(String type, @Nonnull String[] includes, Stream<Object> entityIds,
			String indexName)
	{
		LOG.trace("Retrieving Elasticsearch [{}] docs with ids [{}] and includes [{}] ...", type, entityIds,
				asList(includes));
		MultiGetRequestBuilder request = client.prepareMultiGet();

		entityIds.forEach(id -> {
			MultiGetRequest.Item item = new MultiGetRequest.Item(indexName, type, toElasticsearchId(id))
					.fetchSourceContext(new FetchSourceContext(includes));
			request.add(item);
		});
		if (request.request().getItems().isEmpty())
		{
			return Stream.empty();
		}
		MultiGetResponse response = request.get();
		LOG.debug("Retrieved Elasticsearch [{}] docs with ids [{}] and fetch [{}]", type, entityIds, asList(includes));

		return combineResponses(response);
	}

	private Stream<Map<String, Object>> combineResponses(MultiGetResponse response)
	{
		return stream(response.spliterator(), false).flatMap(itemResponse -> {
			if (itemResponse.isFailed())
			{
				throw new ElasticsearchException("Search failed. Returned headers:" + itemResponse.getFailure());
			}
			GetResponse getResponse = itemResponse.getResponse();
			if (getResponse.isExists())
			{
				Map<String, Object> source = getResponse.getSource();
				return Stream.of(source);
			}
			else
			{
				return Stream.empty();
			}
		});
	}
}
