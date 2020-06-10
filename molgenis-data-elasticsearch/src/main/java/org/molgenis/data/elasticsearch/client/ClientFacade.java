package org.molgenis.data.elasticsearch.client;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.action.DocWriteRequest.OpType.INDEX;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.molgenis.data.elasticsearch.ElasticsearchService.MAX_BATCH_SIZE;

import com.google.common.base.Stopwatch;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.client.model.SearchHits;
import org.molgenis.data.elasticsearch.generator.model.Document;
import org.molgenis.data.elasticsearch.generator.model.DocumentAction;
import org.molgenis.data.elasticsearch.generator.model.Index;
import org.molgenis.data.elasticsearch.generator.model.IndexSettings;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.Sort;
import org.molgenis.data.index.exception.IndexAlreadyExistsException;
import org.molgenis.data.index.exception.IndexException;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.molgenis.util.UnexpectedEnumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elasticsearch client facade:
 *
 * <ul>
 *   <li>Provides simplified interface to Elasticsearch client
 *   <li>Reduces Elasticsearch client dependencies
 *   <li>Translates Elasticsearch client exceptions to MOLGENIS indexing exceptions
 *   <li>Logs requests and responses
 * </ul>
 */
public class ClientFacade implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(ClientFacade.class);

  private final RestHighLevelClient client;
  private final SettingsContentBuilder settingsBuilder;
  private final MappingContentBuilder mappingSourceBuilder;
  private final SortContentBuilder sortContentBuilder;

  public ClientFacade(RestHighLevelClient client) {
    this.client = requireNonNull(client);
    this.settingsBuilder = new SettingsContentBuilder();
    this.mappingSourceBuilder = new MappingContentBuilder();
    this.sortContentBuilder = new SortContentBuilder();
  }

  public void createIndex(Index index, IndexSettings indexSettings, Stream<Mapping> mappingStream) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Creating index '{}' ...", index.getName());
    }

    CreateIndexRequest createIndexRequest = createIndexRequest(index, indexSettings, mappingStream);

    CreateIndexResponse createIndexResponse;
    try {
      createIndexResponse = client.indices().create(createIndexRequest, DEFAULT);
    } catch (ResourceAlreadyExistsException e) {
      LOG.debug("", e);
      throw new IndexAlreadyExistsException(index.getName());
    } catch (ElasticsearchException | IOException e) {
      LOG.error("", e);
      throw new IndexException(format("Error creating index '%s'.", index.getName()));
    }

    // 'acknowledged' indicates whether the index was successfully created in the cluster before the
    // request timeout
    if (!createIndexResponse.isAcknowledged()) {
      LOG.warn("Index '{}' creation possibly failed (acknowledged=false)", index.getName());
    }
    // 'shards_acknowledged' indicates whether the requisite number of shard copies were started for
    // each shard in the index before timing out
    if (!createIndexResponse.isShardsAcknowledged()) {
      LOG.warn("Index '{}' creation possibly failed (shards_acknowledged=false)", index.getName());
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Created index '{}'.", index.getName());
    }
  }

  private CreateIndexRequest createIndexRequest(
      Index index, IndexSettings indexSettings, Stream<Mapping> mappingStream) {
    var createIndexRequest = new CreateIndexRequest(index.getName());
    createIndexRequest.settings(settingsBuilder.createSettings(indexSettings));
    mappingStream.map(mappingSourceBuilder::createMapping).forEach(createIndexRequest::mapping);
    return createIndexRequest;
  }

  public boolean indexesExist(Index index) {
    return indexesExist(singletonList(index));
  }

  private boolean indexesExist(List<Index> indexes) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Determining index(es) '{}' existence ...", toString(indexes));
    }

    String[] indexNames = toIndexNames(indexes);
    GetIndexRequest indicesExistsRequest = new GetIndexRequest(indexNames);

    boolean exists;
    try {
      exists = client.indices().exists(indicesExistsRequest, DEFAULT);
    } catch (ElasticsearchException | IOException e) {
      LOG.error("", e);
      throw new IndexException(
          format("Error determining index(es) '%s' existence.", toString(indexes)));
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Determined index(es) '{}' existence: {}.", toString(indexes), exists);
    }
    return exists;
  }

  public void deleteIndex(Index index) {
    deleteIndexes(singletonList(index));
  }

  private void deleteIndexes(List<Index> indexes) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Deleting index(es) '{}' ...", toString(indexes));
    }

    String[] indexNames = toIndexNames(indexes);
    DeleteIndexRequest request = new DeleteIndexRequest(indexNames);

    AcknowledgedResponse deleteIndexResponse;
    try {
      deleteIndexResponse = client.indices().delete(request, DEFAULT);
    } catch (ElasticsearchException e) {
      LOG.debug("", e);
      if (e.status().getStatus() == 404) {
        throw new UnknownIndexException(toString(indexes));
      }
      throw new IndexException(format("Error deleting index(es) '%s'.", toString(indexes)));
    } catch (IOException e) {
      LOG.error("", e);
      throw new IndexException(format("Error deleting index(es) '%s'.", toString(indexes)));
    }

    if (!deleteIndexResponse.isAcknowledged()) {
      throw new IndexException(format("Error deleting index(es) '%s'.", toString(indexes)));
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Deleted index(es) '{}'.", toString(indexes));
    }
  }

  public void refreshIndexes() {
    refreshIndexes(singletonList(Index.create("_all")));
  }

  private void refreshIndexes(List<Index> indexes) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Refreshing index(es) '{}' ...", toString(indexes));
    }

    String[] indexNames = toIndexNames(indexes);
    RefreshRequest refreshRequest = new RefreshRequest(indexNames);

    RefreshResponse refreshResponse;
    try {
      refreshResponse = client.indices().refresh(refreshRequest, DEFAULT);
    } catch (ElasticsearchException e) {
      LOG.debug("", e);
      if (e.status().getStatus() == 404) {
        throw new UnknownIndexException(toIndexNames(indexes));
      }
      throw new IndexException(format("Error refreshing index(es) '%s'.", toString(indexes)));
    } catch (IOException e) {
      LOG.error("", e);
      throw new IndexException(format("Error refreshing index(es) '%s'.", toString(indexes)));
    }

    if (refreshResponse.getFailedShards() > 0) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            stream(refreshResponse.getShardFailures())
                .map(ShardOperationFailedException::toString)
                .collect(joining("\n")));
      }
      throw new IndexException(format("Error refreshing index(es) '%s'.", toString(indexes)));
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Refreshed index(es) '{}'", toString(indexes));
    }
  }

  public long getCount(Index index) {
    return getCount(singletonList(index));
  }

  private long getCount(List<Index> indexes) {
    return getCount(null, indexes);
  }

  public long getCount(QueryBuilder query, Index index) {
    return getCount(query, singletonList(index));
  }

  private long getCount(QueryBuilder query, List<Index> indexes) {
    if (LOG.isTraceEnabled()) {
      if (query != null) {
        LOG.trace("Counting docs in index(es) '{}' with query '{}' ...", toString(indexes), query);
      } else {
        LOG.trace("Counting docs in index(es) '{}' ...", toString(indexes));
      }
    }

    CountRequest countRequest = new CountRequest(toIndexNames(indexes));
    if (query != null) {
      countRequest.query(query);
    }

    CountResponse countResponse;
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      countResponse = client.count(countRequest, DEFAULT);
    } catch (ElasticsearchStatusException e) {
      LOG.error("", e);
      if (e.status().getStatus() == 404) {
        throw new UnknownIndexException(toIndexNames(indexes));
      } else {
        throw new IndexException(
            format("Error counting docs in index(es) '%s'.", toString(indexes)));
      }
    } catch (ElasticsearchException | IOException e) {
      LOG.error("", e);
      throw new IndexException(format("Error counting docs in index(es) '%s'.", toString(indexes)));
    }
    stopwatch.stop();
    if (countResponse.getFailedShards() > 0) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            stream(countResponse.getShardFailures())
                .map(ShardSearchFailure::toString)
                .collect(joining("\n")));
      }
      throw new IndexException(format("Error counting docs in index(es) '%s'.", toString(indexes)));
    }
    if (TRUE.equals(countResponse.isTerminatedEarly())) {
      throw new IndexException(
          format("Timeout while counting docs in index(es) '%s'.", toString(indexes)));
    }

    long totalHits = countResponse.getCount();
    if (LOG.isDebugEnabled()) {
      if (query != null) {
        LOG.debug(
            "Counted {} docs in index(es) '{}' with query '{}' in {}ms.",
            totalHits,
            toString(indexes),
            query,
            stopwatch.elapsed(MILLISECONDS));
      } else {
        LOG.debug(
            "Counted {} docs in index(es) '{}' in {}ms.",
            totalHits,
            toString(indexes),
            stopwatch.elapsed(MILLISECONDS));
      }
    }
    return totalHits;
  }

  public SearchHits search(QueryBuilder query, int from, int size, List<Index> indexes) {
    return search(query, from, size, null, indexes);
  }

  public SearchHits search(QueryBuilder query, int from, int size, Sort sort, Index index) {
    return search(query, from, size, sort, singletonList(index));
  }

  private SearchHits search(
      QueryBuilder query, int from, int size, Sort sort, List<Index> indexes) {
    if (size > MAX_BATCH_SIZE) {
      throw new MolgenisQueryException(
          String.format(
              "Batch size of %s exceeds the maximum batch size of %s for search queries",
              size, MAX_BATCH_SIZE));
    }

    if (LOG.isTraceEnabled()) {
      if (sort != null) {
        LOG.trace(
            "Searching docs [{}-{}] in index(es) '{}' with query '{}' sorted by '{}' ...",
            from,
            from + size,
            toString(indexes),
            query,
            sort);
      } else {
        LOG.trace(
            "Searching docs [{}-{}] in index(es) '{}' with query '{}' ...",
            from,
            from + size,
            toString(indexes),
            query);
      }
    }

    SearchRequest searchRequest = createSearchRequest(query, from, size, sort, null, indexes);

    SearchResponse searchResponse;
    try {
      searchResponse = client.search(searchRequest, DEFAULT);
    } catch (ElasticsearchException e) {
      LOG.error("", e);
      if (e.status().getStatus() == 404) {
        throw new UnknownIndexException(toIndexNames(indexes));
      }
      throw new IndexException(
          format(
              "Error searching docs in index(es) '%s' with query '%s'.", toString(indexes), query));
    } catch (IOException e) {
      LOG.error("", e);
      throw new IndexException(
          format(
              "Error searching docs in index(es) '%s' with query '%s'.", toString(indexes), query));
    }
    if (searchResponse.getFailedShards() > 0) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            stream(searchResponse.getShardFailures())
                .map(ShardSearchFailure::toString)
                .collect(joining("\n")));
      }
      throw new IndexException(
          format(
              "Error searching docs in index(es) '%s' with query '%s'.", toString(indexes), query));
    }
    if (searchResponse.isTimedOut()) {
      throw new IndexException(
          format(
              "Timeout searching counting docs in index(es) '%s'  with query '%s'.",
              toString(indexes), query));
    }

    if (LOG.isDebugEnabled()) {
      if (sort != null) {
        LOG.debug(
            "Searched {} docs in index(es) '{}' with query '{}' sorted by '{}' in {}ms.",
            searchResponse.getHits().getTotalHits(),
            toString(indexes),
            query,
            sort,
            searchResponse.getTook().millis());
      } else {
        LOG.debug(
            "Searched {} docs in index(es) '{}' with query '{}' in {}ms.",
            searchResponse.getHits().getTotalHits(),
            toString(indexes),
            query,
            searchResponse.getTook().millis());
      }
    }
    return createSearchResponse(searchResponse);
  }

  private SearchRequest createSearchRequest(
      QueryBuilder query,
      Integer from,
      Integer size,
      Sort sort,
      List<AggregationBuilder> aggregations,
      List<Index> indexes) {
    String[] indexNames = toIndexNames(indexes);
    SearchRequest searchRequest = new SearchRequest(indexNames);
    SearchSourceBuilder searchSourceBuilder =
        createSearchSourceBuilder(query, from, size, sort, aggregations);
    searchRequest.source(searchSourceBuilder);
    return searchRequest;
  }

  private SearchSourceBuilder createSearchSourceBuilder(
      QueryBuilder query,
      Integer from,
      Integer size,
      Sort sort,
      List<AggregationBuilder> aggregations) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    if (query != null) {
      searchSourceBuilder.query(query);
    }
    if (from != null) {
      searchSourceBuilder.from(from);
    }
    if (size != null) {
      searchSourceBuilder.size(size);
    }
    if (sort != null) {
      List<SortBuilder> sorts = sortContentBuilder.createSorts(sort);
      sorts.forEach(searchSourceBuilder::sort);
    }
    if (aggregations != null) {
      aggregations.forEach(searchSourceBuilder::aggregation);
    }
    return searchSourceBuilder;
  }

  private SearchHits createSearchResponse(SearchResponse searchResponse) {
    org.elasticsearch.search.SearchHits searchHits = searchResponse.getHits();
    List<SearchHit> searchHitList =
        stream(searchHits.getHits())
            .map(hit -> SearchHit.create(hit.getId(), hit.getIndex()))
            .collect(toList());
    return SearchHits.create(searchHits.getTotalHits().value, searchHitList);
  }

  public Aggregations aggregate(
      List<AggregationBuilder> aggregations, QueryBuilder query, Index index) {
    return aggregate(aggregations, query, singletonList(index));
  }

  private Aggregations aggregate(
      List<AggregationBuilder> aggregations, QueryBuilder query, List<Index> indexes) {
    if (LOG.isTraceEnabled()) {
      if (query != null) {
        LOG.trace(
            "Aggregating docs in index(es) '{}' with aggregations '{}' and query '{}' ...",
            toString(indexes),
            aggregations,
            query);
      } else {
        LOG.trace(
            "Aggregating docs in index(es) '{}' with aggregations '{}' ...",
            toString(indexes),
            aggregations);
      }
    }

    SearchRequest searchRequest = createSearchRequest(query, null, 0, null, aggregations, indexes);
    SearchResponse searchResponse;
    try {
      searchResponse = client.search(searchRequest, DEFAULT);
    } catch (ElasticsearchException e) {
      LOG.error("", e);
      if (e.status().getStatus() == 404) {
        throw new UnknownIndexException(toIndexNames(indexes));
      }
      throw new IndexException(
          format("Error aggregating docs in index(es) '%s'.", toString(indexes)));
    } catch (IOException e) {
      LOG.error("", e);
      throw new IndexException(
          format("Error aggregating docs in index(es) '%s'.", toString(indexes)));
    }
    if (searchResponse.getFailedShards() > 0) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            stream(searchResponse.getShardFailures())
                .map(ShardSearchFailure::toString)
                .collect(joining("\n")));
      }
      throw new IndexException(
          format("Error aggregating docs in index(es) '%s'.", toString(indexes)));
    }
    if (searchResponse.isTimedOut()) {
      throw new IndexException(
          format("Timeout aggregating docs in index(es) '%s'.", toString(indexes)));
    }

    if (LOG.isDebugEnabled()) {
      if (query != null) {
        LOG.debug(
            "Aggregated docs in index(es) '{}' with aggregations '{}' and query '{}' in {}ms.",
            toString(indexes),
            aggregations,
            query,
            searchResponse.getTook().millis());
      } else {
        LOG.debug(
            "Aggregated docs in index(es) '{}' with aggregations '{}' in {}ms.",
            toString(indexes),
            aggregations,
            searchResponse.getTook().millis());
      }
    }
    return searchResponse.getAggregations();
  }

  public Explanation explain(SearchHit searchHit, QueryBuilder query) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(
          "Explaining doc with id '{}' in index '{}' for query '{}' ...",
          searchHit.getId(),
          searchHit.getIndex(),
          query);
    }

    String indexName = searchHit.getIndex();
    ExplainRequest explainRequest = new ExplainRequest(indexName, searchHit.getId()).query(query);
    ExplainResponse explainResponse;
    try {
      explainResponse = client.explain(explainRequest, DEFAULT);
    } catch (ElasticsearchException | IOException e) {
      LOG.error("", e);
      throw new IndexException(
          format(
              "Error explaining doc with id '%s' in index '%s' for query '%s'.",
              searchHit.getId(), searchHit.getIndex(), query));
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Explained doc with id '{}' in index '{}' for query '{}'.",
          searchHit.getId(),
          searchHit.getIndex(),
          query);
    }
    return explainResponse.getExplanation();
  }

  public void index(Index index, Document document) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Indexing doc with id '{}' in index '{}' ...", document.getId(), index.getName());
    }

    String indexName = index.getName();
    String documentId = document.getId();
    XContentBuilder source = document.getContent();
    IndexRequest indexRequest = new IndexRequest(indexName).id(documentId).source(source);

    IndexResponse indexResponse;
    try {
      indexResponse = client.index(indexRequest, DEFAULT);
    } catch (ElasticsearchException e) {
      LOG.error("", e);
      if (e.status().getStatus() == 404) {
        throw new UnknownIndexException(index.getName());
      }
      throw new IndexException(
          format("Error indexing doc with id '%s' in index '%s'.", documentId, indexName));
    } catch (IOException e) {
      LOG.debug("", e);
      throw new IndexException(
          format("Error indexing doc with id '%s' in index '%s'.", documentId, indexName));
    }

    // TODO: Is it good enough if at least one shard succeeds? Shouldn't we at least log something
    // if failures > 0?
    if (indexResponse.getShardInfo().getSuccessful() == 0) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            Arrays.stream(indexResponse.getShardInfo().getFailures())
                // FIXME: logs Object.toString()
                .map(ReplicationResponse.ShardInfo.Failure::toString)
                .collect(joining("\n")));
      }
      throw new IndexException(
          format("Error indexing doc with id '%s' in index '%s'.", documentId, indexName));
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Indexed doc with id '{}' in index '{}'.", documentId, indexName);
    }
  }

  public void deleteById(Index index, Document document) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Deleting doc with id '{}' in index '{}' ...", document.getId(), index.getName());
    }

    String indexName = index.getName();
    String documentId = document.getId();
    DeleteRequest deleteRequest = new DeleteRequest(indexName).id(documentId);

    DeleteResponse deleteResponse;
    try {
      deleteResponse = client.delete(deleteRequest, DEFAULT);
    } catch (ElasticsearchException e) {
      LOG.error("", e);
      if (e.status().getStatus() == 404) {
        throw new UnknownIndexException(index.getName());
      }
      throw new IndexException(
          format("Error deleting doc with id '%s' in index '%s'.", documentId, indexName));
    } catch (IOException e) {
      LOG.debug("", e);
      throw new IndexException(
          format("Error deleting doc with id '%s' in index '%s'.", documentId, indexName));
    }

    // TODO: Check why not check shardinfo?

    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Deleted doc with id '{}' in index '{}' and status '{}'",
          documentId,
          indexName,
          deleteResponse.getResult());
    }
  }

  public void processDocumentActions(Stream<DocumentAction> documentActions) {
    LOG.trace("Processing document actions ...");
    BulkRequest bulkRequest = new BulkRequest();
    documentActions.map(this::toDocWriteRequest).forEach(bulkRequest::add);
    try {
      client.bulk(bulkRequest, DEFAULT);
    } catch (IOException | ElasticsearchException ex) {
      throw new MolgenisDataException("Failed to complete bulk request within the given time");
    }
  }

  private DocWriteRequest toDocWriteRequest(DocumentAction documentAction) {
    String indexName = documentAction.getIndex().getName();
    String documentId = documentAction.getDocument().getId();

    DocWriteRequest docWriteRequest;
    switch (documentAction.getOperation()) {
      case INDEX:
        XContentBuilder source = documentAction.getDocument().getContent();
        if (source == null) {
          throw new IndexException(
              format("Document action is missing document source '%s'", documentAction));
        }
        docWriteRequest =
            Requests.indexRequest(indexName).id(documentId).source(source).opType(INDEX);
        break;
      case DELETE:
        docWriteRequest = Requests.deleteRequest(indexName).id(documentId);
        break;
      default:
        throw new UnexpectedEnumException(documentAction.getOperation());
    }
    return docWriteRequest;
  }

  private String[] toIndexNames(List<Index> indexes) {
    return indexes.stream().map(Index::getName).toArray(String[]::new);
  }

  private String toString(List<Index> indexes) {
    return indexes.stream().map(Index::getName).collect(joining(", "));
  }

  @Override
  public void close() {
    try {
      client.close();
    } catch (ElasticsearchException | IOException e) {
      LOG.error("Error closing Elasticsearch client", e);
    }
  }
}
