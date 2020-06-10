package org.molgenis.data.elasticsearch.client;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NGRAM;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NOT_ANALYZED;
import static org.molgenis.data.elasticsearch.FieldConstants.NGRAM_ANALYZER;
import static org.molgenis.data.elasticsearch.client.SettingsContentBuilder.CI_NORMALIZER;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.util.UnexpectedEnumException;

/** Creates Elasticsearch client content for mappings. */
class MappingContentBuilder {

  /**
   * Protect against Lucene’s term byte-length limit of 32766 bytes 32766/4=8191 since UTF-8
   * characters may occupy at most 4 bytes
   */
  private static final int IGNORE_ABOVE_VALUE = 8191;

  public static final String KEYWORD_TYPE = "keyword";
  public static final String FIELDS = "fields";
  public static final String TYPE = "type";
  public static final String INDEX = "index";

  private final XContentType xContentType;

  MappingContentBuilder() {
    this(XContentType.JSON);
  }

  MappingContentBuilder(XContentType xContentType) {
    this.xContentType = requireNonNull(xContentType);
  }

  XContentBuilder createMapping(Mapping mapping) {
    try (XContentBuilder contentBuilder = XContentFactory.contentBuilder(xContentType)) {
      createMapping(mapping, contentBuilder);
      return contentBuilder;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void createMapping(Mapping mapping, XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject();
    contentBuilder.startObject("_source").field("enabled", false).endObject();
    createFieldMappings(mapping.getFieldMappings(), contentBuilder);
    contentBuilder.endObject();
  }

  private void createFieldMappings(List<FieldMapping> fieldMappings, XContentBuilder contentBuilder)
      throws IOException {
    contentBuilder.startObject("properties");
    fieldMappings.forEach(
        fieldMapping -> {
          try {
            createFieldMapping(fieldMapping, contentBuilder);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
    contentBuilder.endObject();
  }

  private void createFieldMapping(FieldMapping fieldMapping, XContentBuilder contentBuilder)
      throws IOException {
    contentBuilder.startObject(fieldMapping.getName());
    switch (fieldMapping.getType()) {
      case BOOLEAN:
        createFieldMapping("boolean", contentBuilder);
        break;
      case DATE:
        createFieldMappingDate("date", contentBuilder);
        break;
      case DATE_TIME:
        createFieldMappingDate("date_time_no_millis", contentBuilder);
        break;
      case DOUBLE:
        createFieldMapping("double", contentBuilder);
        break;
      case INTEGER:
        createFieldMappingInteger(contentBuilder);
        break;
      case LONG:
        createFieldMapping("long", contentBuilder);
        break;
      case NESTED:
        createFieldMappingNested(fieldMapping.getNestedFieldMappings(), contentBuilder);
        break;
      case TEXT:
        createFieldMappingText(contentBuilder, fieldMapping.isNeedsNgram());
        break;
      case KEYWORD:
        createFieldMappingKeyword(fieldMapping, contentBuilder);
        break;
      default:
        throw new UnexpectedEnumException(fieldMapping.getType());
    }
    contentBuilder.endObject();
  }

  private void createFieldMapping(String type, XContentBuilder contentBuilder) throws IOException {
    contentBuilder.field(TYPE, type);
  }

  private void createFieldMappingDate(String dateFormat, XContentBuilder contentBuilder)
      throws IOException {
    contentBuilder.field(TYPE, "date").field("format", dateFormat);
    // not-analyzed field for aggregation
    // note: the norms settings defaults to false for not_analyzed fields
    contentBuilder
        .startObject(FIELDS)
        .startObject(FIELD_NOT_ANALYZED)
        .field(TYPE, KEYWORD_TYPE)
        .field(INDEX, true)
        .endObject()
        .endObject();
  }

  private void createFieldMappingInteger(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.field(TYPE, "integer");
    // Fix sorting by using disk-based "fielddata" instead of in-memory "fielddata"
    contentBuilder.field("doc_values", true);
  }

  private void createFieldMappingNested(
      List<FieldMapping> nestedFieldMappings, XContentBuilder contentBuilder) throws IOException {
    contentBuilder.field(TYPE, "nested");
    createFieldMappings(nestedFieldMappings, contentBuilder);
  }

  private void createFieldMappingKeyword(FieldMapping fieldMapping, XContentBuilder contentBuilder)
      throws IOException {
    contentBuilder.field(TYPE, KEYWORD_TYPE);
    if (!fieldMapping.isCaseSensitive()) {
      contentBuilder.field("normalizer", CI_NORMALIZER);
    }
    XContentBuilder fieldsObject =
        contentBuilder
            .startObject(FIELDS)
            .startObject(FIELD_NOT_ANALYZED)
            .field(TYPE, KEYWORD_TYPE)
            .field(INDEX, true)
            .endObject();
    fieldsObject.endObject();
  }

  private void createFieldMappingText(XContentBuilder contentBuilder, boolean needsNgram)
      throws IOException {
    // enable/disable norms based on given value
    contentBuilder.field(TYPE, "text");
    contentBuilder.field("norms", true);
    // not-analyzed field for sorting and wildcard queries
    // note: the norms settings defaults to false for not_analyzed fields
    XContentBuilder fieldsObject =
        contentBuilder
            .startObject(FIELDS)
            .startObject(FIELD_NOT_ANALYZED)
            .field(TYPE, KEYWORD_TYPE)
            .field(INDEX, true)
            .field("ignore_above", IGNORE_ABOVE_VALUE)
            .endObject();
    if (needsNgram) {
      contentBuilder
          .startObject(FIELD_NGRAM)
          .field("analyzer", NGRAM_ANALYZER)
          .field(TYPE, "text")
          .field(INDEX, true)
          .endObject();
    }
    fieldsObject.endObject();
  }
}
