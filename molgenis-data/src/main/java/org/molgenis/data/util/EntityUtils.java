package org.molgenis.data.util;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.util.MolgenisDateFormat.FAILED_TO_PARSE_ATTRIBUTE_AS_DATETIME_MESSAGE;
import static org.molgenis.data.util.MolgenisDateFormat.FAILED_TO_PARSE_ATTRIBUTE_AS_DATE_MESSAGE;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.data.util.MolgenisDateFormat.parseLocalDate;

import com.google.common.collect.Iterables;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.util.ListEscapeUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.util.i18n.LanguageService;

public class EntityUtils {
  private EntityUtils() {}

  /**
   * Convert a string value to a typed value based on a non-entity-referencing attribute data type.
   *
   * @param valueStr string value
   * @param attr non-entity-referencing attribute
   * @return typed value
   * @throws MolgenisDataException if attribute references another entity
   */
  public static Object getTypedValue(String valueStr, Attribute attr) {
    // Reference types cannot be processed because we lack an entityManager in this route.
    if (EntityTypeUtils.isReferenceType(attr)) {
      throw new MolgenisDataException(
          "getTypedValue(String, AttributeMetadata) can't be used for attributes referencing entities");
    }
    return getTypedValue(valueStr, attr, null);
  }

  /**
   * Convert a string value to a typed value based on the attribute data type.
   *
   * @param valueStr string value
   * @param attr attribute
   * @param entityManager entity manager used to convert referenced entity values
   * @return typed value
   */
  public static Object getTypedValue(String valueStr, Attribute attr, EntityManager entityManager) {
    if (valueStr == null) return null;
    switch (attr.getDataType()) {
      case BOOL:
        return Boolean.valueOf(valueStr);
      case CATEGORICAL:
      case FILE:
      case XREF:
        EntityType xrefEntity = attr.getRefEntity();
        Object xrefIdValue = getTypedValue(valueStr, xrefEntity.getIdAttribute(), entityManager);
        return entityManager.getReference(xrefEntity, xrefIdValue);
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        EntityType mrefEntity = attr.getRefEntity();
        List<String> mrefIdStrValues = ListEscapeUtils.toList(valueStr);
        return mrefIdStrValues.stream()
            .map(
                mrefIdStrValue ->
                    getTypedValue(mrefIdStrValue, mrefEntity.getIdAttribute(), entityManager))
            .map(mrefIdValue -> entityManager.getReference(mrefEntity, mrefIdValue))
            .collect(toList());
      case COMPOUND:
        throw new IllegalArgumentException("Compound attribute has no value");
      case DATE:
        try {
          return parseLocalDate(valueStr);
        } catch (DateTimeParseException e) {
          throw new MolgenisDataException(
              format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATE_MESSAGE, attr.getName(), valueStr), e);
        }
      case DATE_TIME:
        try {
          return parseInstant(valueStr);
        } catch (DateTimeParseException e) {
          throw new MolgenisDataException(
              format(FAILED_TO_PARSE_ATTRIBUTE_AS_DATETIME_MESSAGE, attr.getName(), valueStr), e);
        }
      case DECIMAL:
        return Double.valueOf(valueStr);
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        return valueStr;
      case INT:
        return Integer.valueOf(valueStr);
      case LONG:
        return Long.valueOf(valueStr);
      default:
        throw new UnexpectedEnumException(attr.getDataType());
    }
  }

  /** Returns true if entity metadata equals another entity metadata. TODO docs */
  public static boolean equals(EntityType entityType, EntityType otherEntityType) {
    if (entityType == null && otherEntityType != null) return false;
    if (entityType != null && otherEntityType == null) return false;
    if (!(entityType != null && entityType.getId().equals(otherEntityType.getId()))) return false;
    if (!Objects.equals(entityType.getLabel(), otherEntityType.getLabel())) return false;
    if (!LanguageService.getLanguageCodes()
        .allMatch(
            languageCode ->
                Objects.equals(
                    entityType.getLabel(languageCode), otherEntityType.getLabel(languageCode)))) {
      return false;
    }
    if (!Objects.equals(entityType.getDescription(), otherEntityType.getDescription()))
      return false;
    if (!LanguageService.getLanguageCodes()
        .allMatch(
            languageCode ->
                Objects.equals(
                    entityType.getDescription(languageCode),
                    otherEntityType.getDescription(languageCode)))) {
      return false;
    }
    if (entityType.isAbstract() != otherEntityType.isAbstract()) return false;

    // NB This is at such a low level that we do not know the default backend
    // so we don't check if the other one is the default if the backend is null.
    String backend = entityType.getBackend();
    String otherBackend = otherEntityType.getBackend();
    if ((backend == null && otherBackend != null)
        || (backend != null && otherBackend == null)
        || (backend != null && !backend.equals(otherBackend))) {
      return false;
    }

    // compare package identifiers
    Package pack = entityType.getPackage();
    Package otherPackage = otherEntityType.getPackage();
    if (pack == null && otherPackage != null) return false;
    if (pack != null && otherPackage == null) return false;
    if (pack != null && !pack.getIdValue().equals(otherPackage.getIdValue())) {
      return false;
    }

    // compare id attribute identifier (identifier might be null if id attribute hasn't been
    // persisted yet)
    Attribute ownIdAttribute = entityType.getOwnIdAttribute();
    Attribute otherOwnIdAttribute = otherEntityType.getOwnIdAttribute();
    if (ownIdAttribute == null && otherOwnIdAttribute != null) return false;
    if (ownIdAttribute != null && otherOwnIdAttribute == null) return false;
    if (ownIdAttribute != null
        && !Objects.equals(ownIdAttribute.getIdentifier(), otherOwnIdAttribute.getIdentifier()))
      return false;

    // compare label attribute identifier (identifier might be null if id attribute hasn't been
    // persisted yet)
    Attribute ownLabelAttribute = entityType.getOwnLabelAttribute();
    Attribute otherOwnLabelAttribute = otherEntityType.getOwnLabelAttribute();
    if (ownLabelAttribute == null && otherOwnLabelAttribute != null) return false;
    if (ownLabelAttribute != null && otherOwnLabelAttribute == null) return false;
    if (ownLabelAttribute != null
        && !Objects.equals(
            ownLabelAttribute.getIdentifier(), otherOwnLabelAttribute.getIdentifier()))
      return false;

    // compare lookup attribute identifiers
    List<Attribute> lookupAttrs = newArrayList(entityType.getOwnLookupAttributes());
    List<Attribute> otherLookupAttrs = newArrayList(otherEntityType.getOwnLookupAttributes());
    if (lookupAttrs.size() != otherLookupAttrs.size()) return false;
    for (int i = 0; i < lookupAttrs.size(); ++i) {
      // identifier might be null if id attribute hasn't been persisted yet
      if (!Objects.equals(
          lookupAttrs.get(i).getIdentifier(), otherLookupAttrs.get(i).getIdentifier())) {
        return false;
      }
    }

    // compare extends entity identifier
    EntityType extendsEntityType = entityType.getExtends();
    EntityType otherExtendsEntityType = otherEntityType.getExtends();
    if (extendsEntityType == null && otherExtendsEntityType != null) return false;
    if (extendsEntityType != null && otherExtendsEntityType == null) return false;
    if (extendsEntityType != null
        && !extendsEntityType.getId().equals(otherExtendsEntityType.getId())) return false;

    // compare attributes
    if (!equals(entityType.getOwnAllAttributes(), otherEntityType.getOwnAllAttributes()))
      return false;

    // compare tag identifiers
    List<Tag> tags = newArrayList(entityType.getTags());
    List<Tag> otherTags = newArrayList(otherEntityType.getTags());
    if (tags.size() != otherTags.size()) return false;
    for (int i = 0; i < tags.size(); ++i) {
      if (!tags.get(i).getId().equals(otherTags.get(i).getId())) return false;
    }

    return entityType.getIndexingDepth() == otherEntityType.getIndexingDepth();
  }

  /** Returns true if an Iterable equals another Iterable. */
  public static boolean equals(Iterable<Attribute> attrsIt, Iterable<Attribute> otherAttrsIt) {
    List<Attribute> attrs = newArrayList(attrsIt);
    List<Attribute> otherAttrs = newArrayList(otherAttrsIt);

    if (attrs.size() != otherAttrs.size()) return false;
    for (int i = 0; i < attrs.size(); ++i) {
      if (!equals(attrs.get(i), otherAttrs.get(i))) return false;
    }
    return true;
  }

  /** Returns true if an Iterable equals another Iterable. */
  public static boolean equalsEntities(
      Iterable<Entity> entityIterable, Iterable<Entity> otherEntityIterable) {
    List<Entity> attrs = newArrayList(entityIterable);
    List<Entity> otherAttrs = newArrayList(otherEntityIterable);

    if (attrs.size() != otherAttrs.size()) return false;
    for (int i = 0; i < attrs.size(); ++i) {
      if (!equals(attrs.get(i), otherAttrs.get(i))) return false;
    }
    return true;
  }

  /** Returns true if a Tag equals another Tag. */
  public static boolean equals(Tag tag, Tag otherTag) {
    if (!Objects.equals(tag.getId(), otherTag.getId())) return false;
    if (!Objects.equals(tag.getObjectIri(), otherTag.getObjectIri())) return false;
    if (!Objects.equals(tag.getLabel(), otherTag.getLabel())) return false;
    if (!Objects.equals(tag.getRelationIri(), otherTag.getRelationIri())) return false;
    if (!Objects.equals(tag.getRelationLabel(), otherTag.getRelationLabel())) return false;
    return Objects.equals(tag.getCodeSystem(), otherTag.getCodeSystem());
  }

  /** Returns true if an attribute equals another attribute. */
  public static boolean equals(Attribute attr, Attribute otherAttr) {
    return equals(attr, otherAttr, true);
  }

  /**
   * Returns true if an attribute equals another attribute. Skips the identifier if checkIdentifier
   * is set to false
   *
   * <p>Other attribute identifiers can be null when importing and this attribute has not been
   * persisted to the db yet
   *
   * @param checkIdentifier skips checking attribute identifier, parent attribute identifier and
   *     attribute entity identifier
   */
  public static boolean equals(Attribute attr, Attribute otherAttr, boolean checkIdentifier) {
    if (attr == null || otherAttr == null) {
      return (attr == null && otherAttr == null);
    }

    if (checkIdentifier && !Objects.equals(attr.getIdentifier(), otherAttr.getIdentifier())) {
      return false;
    }
    if (!Objects.equals(attr.getName(), otherAttr.getName())) {
      return false;
    }

    EntityType entity = attr.getEntity();
    EntityType otherEntity = otherAttr.getEntity();
    if (checkIdentifier) {
      if (entity == null && otherEntity != null) {
        return false;
      }
      if (entity != null && otherEntity == null) {
        return false;
      }
      if (entity != null && !entity.getId().equals(otherEntity.getId())) {
        return false;
      }
    }
    if (!Objects.equals(attr.getSequenceNumber(), otherAttr.getSequenceNumber())) {
      return false;
    }
    if (!Objects.equals(attr.getLabel(), otherAttr.getLabel())) {
      return false;
    }
    if (!LanguageService.getLanguageCodes()
        .allMatch(
            languageCode ->
                Objects.equals(attr.getLabel(languageCode), otherAttr.getLabel(languageCode)))) {
      return false;
    }
    if (!Objects.equals(attr.getDescription(), otherAttr.getDescription())) {
      return false;
    }
    if (!LanguageService.getLanguageCodes()
        .allMatch(
            languageCode ->
                Objects.equals(
                    attr.getDescription(languageCode), otherAttr.getDescription(languageCode)))) {
      return false;
    }
    if (!Objects.equals(attr.getDataType(), otherAttr.getDataType())) {
      return false;
    }
    if (!Objects.equals(attr.isIdAttribute(), otherAttr.isIdAttribute())) {
      return false;
    }
    if (!Objects.equals(attr.isLabelAttribute(), otherAttr.isLabelAttribute())) {
      return false;
    }
    if (!Objects.equals(attr.getLookupAttributeIndex(), otherAttr.getLookupAttributeIndex())) {
      return false;
    }

    // recursively compare attribute parent
    if (!EntityUtils.equals(attr.getParent(), otherAttr.getParent(), checkIdentifier)) {
      return false;
    }

    // compare entity identifier
    if (attr.hasRefEntity() && !otherAttr.hasRefEntity()) {
      return false;
    }
    if (!attr.hasRefEntity() && otherAttr.hasRefEntity()) {
      return false;
    }
    if (attr.hasRefEntity()
        && !attr.getRefEntity().getId().equals(otherAttr.getRefEntity().getId())) {
      return false;
    }
    if (!Objects.equals(attr.getCascadeDelete(), otherAttr.getCascadeDelete())) {
      return false;
    }
    if (!EntityUtils.equals(attr.getMappedBy(), otherAttr.getMappedBy(), checkIdentifier)) {
      return false;
    }
    if (!Objects.equals(attr.getOrderBy(), otherAttr.getOrderBy())) {
      return false;
    }
    if (!Objects.equals(attr.getExpression(), otherAttr.getExpression())) {
      return false;
    }
    if (!Objects.equals(attr.isNillable(), otherAttr.isNillable())) {
      return false;
    }
    if (!Objects.equals(attr.isAuto(), otherAttr.isAuto())) {
      return false;
    }
    if (!Objects.equals(attr.isVisible(), otherAttr.isVisible())) {
      return false;
    }
    if (!Objects.equals(attr.isAggregatable(), otherAttr.isAggregatable())) {
      return false;
    }
    if (!Objects.equals(attr.getEnumOptions(), otherAttr.getEnumOptions())) {
      return false;
    }
    if (!Objects.equals(attr.getRangeMin(), otherAttr.getRangeMin())) {
      return false;
    }
    if (!Objects.equals(attr.getRangeMax(), otherAttr.getRangeMax())) {
      return false;
    }
    if (!Objects.equals(attr.isReadOnly(), otherAttr.isReadOnly())) {
      return false;
    }
    if (!Objects.equals(attr.isUnique(), otherAttr.isUnique())) {
      return false;
    }
    if (!Objects.equals(attr.getNullableExpression(), otherAttr.getNullableExpression())) {
      return false;
    }
    if (!Objects.equals(attr.getVisibleExpression(), otherAttr.getVisibleExpression())) {
      return false;
    }
    if (!Objects.equals(attr.getValidationExpression(), otherAttr.getValidationExpression())) {
      return false;
    }
    if (!Objects.equals(attr.getDefaultValue(), otherAttr.getDefaultValue())) {
      return false;
    }

    // compare tag identifiers
    List<Tag> tags = newArrayList(attr.getTags());
    List<Tag> otherTags = newArrayList(otherAttr.getTags());
    if (tags.size() != otherTags.size()) {
      return false;
    }
    for (int i = 0; i < tags.size(); ++i) {
      if (!Objects.equals(tags.get(i).getId(), otherTags.get(i).getId())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if entity equals another entity. For referenced entities compares the referenced
   * entity ids.
   *
   * @return true if entity equals another entity
   */
  public static boolean equals(Entity entity, Entity otherEntity) {
    if (entity == null && otherEntity != null) return false;
    if (entity != null && otherEntity == null) return false;
    if (entity == null) return true;
    if (!entity.getEntityType().getId().equals(otherEntity.getEntityType().getId())) return false;

    for (Attribute attr : entity.getEntityType().getAtomicAttributes()) {
      String attrName = attr.getName();
      switch (attr.getDataType()) {
        case BOOL:
          if (!Objects.equals(entity.getBoolean(attrName), otherEntity.getBoolean(attrName)))
            return false;
          break;
        case CATEGORICAL:
        case FILE:
        case XREF:
          Entity xrefValue = entity.getEntity(attrName);
          Entity otherXrefValue = otherEntity.getEntity(attrName);
          if (xrefValue == null && otherXrefValue != null) return false;
          if (xrefValue != null && otherXrefValue == null) return false;
          if (xrefValue != null
              && otherXrefValue != null
              && !xrefValue.getIdValue().equals(otherXrefValue.getIdValue())) return false;
          break;
        case CATEGORICAL_MREF:
        case ONE_TO_MANY:
        case MREF:
          List<Entity> entities = newArrayList(entity.getEntities(attrName));
          List<Entity> otherEntities = newArrayList(otherEntity.getEntities(attrName));
          if (entities.size() != otherEntities.size()) return false;
          for (int i = 0; i < entities.size(); ++i) {
            Entity mrefValue = entities.get(i);
            Entity otherMrefValue = otherEntities.get(i);
            if (mrefValue == null && otherMrefValue != null) return false;
            if (mrefValue != null && otherMrefValue == null) return false;
            if (mrefValue != null
                && otherMrefValue != null
                && !mrefValue.getIdValue().equals(otherMrefValue.getIdValue())) return false;
          }
          break;
        case COMPOUND:
          throw new RuntimeException(format("Invalid data type [%s]", attr.getDataType()));
        case DATE:
          if (!Objects.equals(entity.getLocalDate(attrName), otherEntity.getLocalDate(attrName)))
            return false;
          break;
        case DATE_TIME:
          if (!Objects.equals(entity.getInstant(attrName), otherEntity.getInstant(attrName)))
            return false;
          break;
        case DECIMAL:
          if (!Objects.equals(entity.getDouble(attrName), otherEntity.getDouble(attrName)))
            return false;
          break;
        case EMAIL:
        case ENUM:
        case HTML:
        case HYPERLINK:
        case SCRIPT:
        case STRING:
        case TEXT:
          if (!Objects.equals(entity.getString(attrName), otherEntity.getString(attrName)))
            return false;
          break;
        case INT:
          if (!Objects.equals(entity.getInt(attrName), otherEntity.getInt(attrName))) return false;
          break;
        case LONG:
          if (!Objects.equals(entity.getLong(attrName), otherEntity.getLong(attrName)))
            return false;
          break;
        default:
          throw new UnexpectedEnumException(attr.getDataType());
      }
    }
    return true;
  }

  public static int hashCode(Entity entity) {
    int h = 0;
    for (Attribute attr : entity.getEntityType().getAtomicAttributes()) {
      int hValue = 0;
      String attrName = attr.getName();
      switch (attr.getDataType()) {
        case BOOL:
          hValue = Objects.hashCode(entity.getBoolean(attrName));
          break;
        case CATEGORICAL:
        case FILE:
        case XREF:
          Entity xrefValue = entity.getEntity(attrName);
          Object xrefIdValue = xrefValue != null ? xrefValue.getIdValue() : null;
          hValue = Objects.hashCode(xrefIdValue);
          break;
        case CATEGORICAL_MREF:
        case ONE_TO_MANY:
        case MREF:
          for (Entity mrefValue : entity.getEntities(attrName)) {
            Object mrefIdValue = mrefValue != null ? mrefValue.getIdValue() : null;
            hValue += Objects.hashCode(mrefIdValue);
          }
          break;
        case COMPOUND:
          throw new RuntimeException(format("Invalid data type [%s]", attr.getDataType()));
        case DATE:
          hValue = Objects.hashCode(entity.getLocalDate(attrName));
          break;
        case DATE_TIME:
          hValue = Objects.hashCode(entity.getInstant(attrName));
          break;
        case DECIMAL:
          hValue = Objects.hashCode(entity.getDouble(attrName));
          break;
        case EMAIL:
        case ENUM:
        case HTML:
        case HYPERLINK:
        case SCRIPT:
        case STRING:
        case TEXT:
          hValue = Objects.hashCode(entity.getString(attrName));
          break;
        case INT:
          hValue = Objects.hashCode(entity.getInt(attrName));
          break;
        case LONG:
          hValue = Objects.hashCode(entity.getLong(attrName));
          break;
        default:
          throw new UnexpectedEnumException(attr.getDataType());
      }
      h += Objects.hashCode(attrName) ^ hValue;
    }

    int result = entity.getEntityType().getId().hashCode();
    return 31 * result + h;
  }

  /**
   * Returns whether an entity attribute value is <tt>null</tt> or empty for attributes referencing
   * multiple entities.
   */
  public static boolean isNullValue(Entity entity, Attribute attribute) {
    boolean isNullValue;
    String attributeName = attribute.getName();
    AttributeType attributeType = attribute.getDataType();
    switch (attributeType) {
      case BOOL:
        isNullValue = entity.getBoolean(attributeName) == null;
        break;
      case CATEGORICAL:
      case FILE:
      case XREF:
        isNullValue = entity.getEntity(attributeName) == null;
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        Iterable<Entity> refEntities = entity.getEntities(attributeName);
        isNullValue = Iterables.isEmpty(refEntities);
        break;
      case COMPOUND:
        throw new RuntimeException(format("Invalid data type [%s]", attribute.getDataType()));
      case DATE:
        isNullValue = entity.getLocalDate(attributeName) == null;
        break;
      case DATE_TIME:
        isNullValue = entity.getInstant(attributeName) == null;
        break;
      case DECIMAL:
        isNullValue = entity.getDouble(attributeName) == null;
        break;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        isNullValue = entity.getString(attributeName) == null;
        break;
      case INT:
        isNullValue = entity.getInt(attributeName) == null;
        break;
      case LONG:
        isNullValue = entity.getLong(attributeName) == null;
        break;
      default:
        throw new UnexpectedEnumException(attributeType);
    }
    return isNullValue;
  }

  /** Returns whether two entities have the same identifier and same entity type identifier. */
  public static boolean isSame(Entity thisEntity, Entity thatEntity) {
    return thisEntity == thatEntity
        || thisEntity.getIdValue().equals(thatEntity.getIdValue())
            && thisEntity.getEntityType().getId().equals(thatEntity.getEntityType().getId());
  }
}
