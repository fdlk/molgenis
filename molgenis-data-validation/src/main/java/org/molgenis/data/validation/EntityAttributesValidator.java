package org.molgenis.data.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Streams.stream;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static org.molgenis.data.util.EntityUtils.isNullValue;

import com.google.common.collect.Streams;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.validation.ConstraintViolation;
import org.springframework.stereotype.Component;

/**
 * Attribute data type validator.
 *
 * <p>Does not check if xref,mref, categorical values are present. That happens in the
 * RepositoryValidationDecorator.
 */
@Component
public class EntityAttributesValidator {
  private final ExpressionValidator expressionValidator;
  private EmailValidator emailValidator;

  EntityAttributesValidator(ExpressionValidator expressionValidator) {
    this.expressionValidator = requireNonNull(expressionValidator);
  }

  public Set<ConstraintViolation> validate(Entity entity, EntityType meta) {
    Map<String, Boolean> expressionValues = computeExpressionValues(entity, meta);
    Set<ConstraintViolation> violations = checkNullableExpressions(entity, meta, expressionValues);
    violations.addAll(checkValidationExpressions(entity, meta));

    stream(meta.getAtomicAttributes())
        .filter(this::isValidationAttribute)
        .forEach(attr -> validateAttribute(entity, meta, attr).ifPresent(violations::add));

    if (entity.getIdValue() == null) {
      violations.add(createConstraintViolation(entity, meta.getIdAttribute(), meta, null));
    }
    return violations;
  }

  private boolean isValidationAttribute(Attribute attribute) {
    return !attribute.hasExpression() && !attribute.isMappedBy();
  }

  private Optional<ConstraintViolation> validateAttribute(
      Entity entity, EntityType entityType, Attribute attribute) {
    ConstraintViolation violation = null;

    AttributeType attrType = attribute.getDataType();
    switch (attrType) {
      case EMAIL:
        violation = checkEmail(entity, attribute, entityType);
        break;
      case BOOL:
        violation = checkBoolean(entity, attribute, entityType);
        break;
      case DATE:
        violation = checkDate(entity, attribute, entityType);
        break;
      case DATE_TIME:
        violation = checkDateTime(entity, attribute, entityType);
        break;
      case DECIMAL:
        violation = checkDecimal(entity, attribute, entityType);
        break;
      case HYPERLINK:
        violation = checkHyperlink(entity, attribute, entityType);
        break;
      case INT:
        violation = checkInt(entity, attribute, entityType);
        if ((violation == null) && (attribute.getRange() != null)) {
          violation = checkRange(entity, attribute, entityType);
        }
        break;
      case LONG:
        violation = checkLong(entity, attribute, entityType);
        if ((violation == null) && (attribute.getRange() != null)) {
          violation = checkRange(entity, attribute, entityType);
        }
        break;
      case ENUM:
        violation = checkEnum(entity, attribute, entityType);
        break;
      case HTML:
      case SCRIPT:
      case TEXT:
      case STRING:
        violation = checkText(entity, attribute, entityType);
        break;
      case CATEGORICAL:
      case FILE:
      case XREF:
        violation = checkXref(entity, attribute, entityType);
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        violation = checkMref(entity, attribute, entityType);
        break;
      case COMPOUND:
        // no op
        break;
      default:
        throw new UnexpectedEnumException(attrType);
    }
    return Optional.ofNullable(violation);
  }

  private ConstraintViolation checkMref(Entity entity, Attribute attr, EntityType entityType) {
    Iterable<Entity> refEntities;
    try {
      refEntities = entity.getEntities(attr.getName());
    } catch (Exception e) {
      return createConstraintViolation(
          entity, attr, entityType, "Not a valid entity, expected an entity list.");
    }
    if (refEntities == null) {
      return createConstraintViolation(
          entity, attr, entityType, "Not a valid entity, expected an entity list.");
    }
    for (Entity refEntity : refEntities) {
      if (refEntity == null) {
        return createConstraintViolation(
            entity, attr, entityType, "Not a valid entity, null is not allowed");
      }
      if (!refEntity.getEntityType().getId().equals(attr.getRefEntity().getId())) {
        return createConstraintViolation(entity, attr, entityType, "Not a valid entity type.");
      }
    }
    return null;
  }

  private ConstraintViolation checkXref(Entity entity, Attribute attr, EntityType entityType) {
    Entity refEntity;
    try {
      refEntity = entity.getEntity(attr.getName());
    } catch (Exception e) {
      return createConstraintViolation(entity, attr, entityType, "Not a valid entity.");
    }

    if (refEntity == null) {
      return null;
    }
    if (!refEntity.getEntityType().getId().equals(attr.getRefEntity().getId())) {
      return createConstraintViolation(entity, attr, entityType, "Not a valid entity type.");
    }
    return null;
  }

  private Map<String, Boolean> computeExpressionValues(Entity entity, EntityType entityType) {
    Set<String> expressions = new TreeSet<>();

    for (Attribute attribute : entityType.getAtomicAttributes()) {
      String nullableExpression = attribute.getNullableExpression();
      if (nullableExpression != null) {
        expressions.add(nullableExpression);
        String visibleExpression = attribute.getVisibleExpression();
        if (visibleExpression != null) {
          expressions.add(visibleExpression);
        }
      }
      String validationExpression = attribute.getValidationExpression();
      if (validationExpression != null) {
        expressions.add(validationExpression);
      }
    }

    if (expressions.isEmpty()) {
      return Collections.emptyMap();
    }

    List<String> expressionsList = newArrayList(expressions);
    List<Boolean> results = expressionValidator.resolveBooleanExpressions(expressionsList, entity);

    return mergeToMap(expressionsList, results);
  }

  private static <K, V> Map<K, V> mergeToMap(List<K> expressionsList, List<V> results) {
    Iterator<K> keys = expressionsList.iterator();
    Iterator<V> values = results.iterator();
    Map<K, V> result = newHashMap();
    while (keys.hasNext() || values.hasNext()) {
      result.put(keys.next(), values.next());
    }
    return result;
  }

  private Set<ConstraintViolation> checkNullableExpressions(
      Entity entity, EntityType entityType, Map<String, Boolean> expressionValues) {
    return Streams.stream(entityType.getAtomicAttributes())
        .filter(attr -> isNullValue(entity, attr))
        .filter(attr -> isNullableExpressionPresentAndNotTrue(expressionValues, attr))
        .filter(not(attr -> isVisibleExpressionPresentAndFalse(expressionValues, attr)))
        .map(
            attr ->
                createConstraintViolation(
                    entity,
                    attr,
                    entityType,
                    format("Offended nullable expression: %s", attr.getNullableExpression())))
        .collect(Collectors.toSet());
  }

  private static boolean isVisibleExpressionPresentAndFalse(
      Map<String, Boolean> expressionValues, Attribute attribute) {
    return Optional.ofNullable(attribute.getVisibleExpression())
        .map(expressionValues::get)
        .filter(FALSE::equals)
        .isPresent();
  }

  private static boolean isNullableExpressionPresentAndNotTrue(
      Map<String, Boolean> expressionValues, Attribute attribute) {
    return Optional.ofNullable(attribute.getNullableExpression())
        .filter(expression -> !TRUE.equals(expressionValues.get(expression)))
        .isPresent();
  }

  private Set<ConstraintViolation> checkValidationExpressions(Entity entity, EntityType meta) {
    List<String> validationExpressions = new ArrayList<>();
    List<Attribute> expressionAttributes = new ArrayList<>();

    for (Attribute attribute : meta.getAtomicAttributes()) {
      if (StringUtils.isNotBlank(attribute.getValidationExpression())) {
        expressionAttributes.add(attribute);
        validationExpressions.add(attribute.getValidationExpression());
      }
    }

    Set<ConstraintViolation> violations = new LinkedHashSet<>();

    if (!validationExpressions.isEmpty()) {
      List<Boolean> results =
          expressionValidator.resolveBooleanExpressions(validationExpressions, entity);
      for (int i = 0; i < results.size(); i++) {
        if (!TRUE.equals(results.get(i))) {
          violations.add(
              createConstraintViolation(
                  entity,
                  expressionAttributes.get(i),
                  meta,
                  format("Offended validation expression: %s", validationExpressions.get(i))));
        }
      }
    }

    return violations;
  }

  private ConstraintViolation checkEmail(
      Entity entity, Attribute attribute, EntityType entityType) {
    String email = entity.getString(attribute.getName());
    if (email == null) {
      return null;
    }

    if (emailValidator == null) {
      emailValidator = new EmailValidator();
    }

    if (!emailValidator.isValid(email, null)) {
      return createConstraintViolation(
          entity, attribute, entityType, "Not a valid e-mail address.");
    }

    if (email.length() > attribute.getMaxLength()) {
      return createConstraintViolation(entity, attribute, entityType);
    }

    return null;
  }

  private static ConstraintViolation checkBoolean(
      Entity entity, Attribute attribute, EntityType entityType) {
    try {
      entity.getBoolean(attribute.getName());
      return null;
    } catch (Exception e) {
      return createConstraintViolation(entity, attribute, entityType);
    }
  }

  private static ConstraintViolation checkDateTime(
      Entity entity, Attribute attribute, EntityType entityType) {
    try {
      entity.getInstant(attribute.getName());
      return null;
    } catch (Exception e) {
      return createConstraintViolation(entity, attribute, entityType);
    }
  }

  private static ConstraintViolation checkDate(
      Entity entity, Attribute attribute, EntityType entityType) {
    try {
      entity.getLocalDate(attribute.getName());
      return null;
    } catch (Exception e) {
      return createConstraintViolation(entity, attribute, entityType);
    }
  }

  private static ConstraintViolation checkDecimal(
      Entity entity, Attribute attribute, EntityType entityType) {
    try {
      entity.getDouble(attribute.getName());
      return null;
    } catch (Exception e) {
      return createConstraintViolation(entity, attribute, entityType);
    }
  }

  private ConstraintViolation checkHyperlink(
      Entity entity, Attribute attribute, EntityType entityType) {
    String link = entity.getString(attribute.getName());
    if (link == null) {
      return null;
    }

    try {
      new URI(link);
    } catch (URISyntaxException e) {
      return createConstraintViolation(entity, attribute, entityType, "Not a valid hyperlink.");
    }

    if (link.length() > attribute.getMaxLength()) {
      return createConstraintViolation(entity, attribute, entityType);
    }

    return null;
  }

  private static ConstraintViolation checkInt(
      Entity entity, Attribute attribute, EntityType entityType) {
    try {
      entity.getInt(attribute.getName());
      return null;
    } catch (Exception e) {
      return createConstraintViolation(entity, attribute, entityType);
    }
  }

  private static ConstraintViolation checkLong(
      Entity entity, Attribute attribute, EntityType entityType) {
    try {
      entity.getLong(attribute.getName());
      return null;
    } catch (Exception e) {
      return createConstraintViolation(entity, attribute, entityType);
    }
  }

  private static ConstraintViolation checkRange(
      Entity entity, Attribute attr, EntityType entityType) {
    Range range = attr.getRange();
    Long value;
    switch (attr.getDataType()) {
      case INT:
        Integer intValue = entity.getInt(attr.getName());
        value = intValue != null ? intValue.longValue() : null;
        break;
      case LONG:
        value = entity.getLong(attr.getName());
        break;
      default:
        throw new RuntimeException(
            format("Range not allowed for data type [%s]", attr.getDataType().toString()));
    }
    if ((value != null)
        && ((range.getMin() != null && value < range.getMin())
            || (range.getMax() != null && value > range.getMax()))) {
      return createConstraintViolation(entity, attr, entityType);
    }

    return null;
  }

  private static ConstraintViolation checkText(
      Entity entity, Attribute attribute, EntityType meta) {
    String text = entity.getString(attribute.getName());
    if (text == null) {
      return null;
    }

    if (text.length() > attribute.getMaxLength()) {
      return createConstraintViolation(entity, attribute, meta);
    }

    return null;
  }

  private ConstraintViolation checkEnum(Entity entity, Attribute attribute, EntityType entityType) {
    String value = entity.getString(attribute.getName());
    if (value != null) {
      List<String> enumOptions = attribute.getEnumOptions();

      if (!enumOptions.contains(value)) {
        return createConstraintViolation(
            entity, attribute, entityType, "Value must be one of " + enumOptions);
      }
    }

    return null;
  }

  private static ConstraintViolation createConstraintViolation(
      Entity entity, Attribute attribute, EntityType entityType) {
    String message =
        format(
            "Invalid %s value '%s' for attribute '%s' of entity '%s'.",
            attribute.getDataType().toString().toLowerCase(),
            entity.get(attribute.getName()),
            attribute.getLabel(),
            entityType.getId());

    Range range = attribute.getRange();
    if (range != null) {
      message += format("Value must be between %d and %d", range.getMin(), range.getMax());
    }

    Integer maxLength = attribute.getMaxLength();
    if (maxLength != null) {
      message += format("Value must be less than or equal to %d characters", maxLength);
    }

    return new ConstraintViolation(message);
  }

  private ConstraintViolation createConstraintViolation(
      Entity entity, Attribute attribute, EntityType entityType, @Nullable String message) {
    Object value = getDataValuesForType(entity, attribute);
    String dataValue = value != null ? value.toString() : null;
    String fullMessage =
        format(
            "Invalid [%s] value [%s] for attribute [%s] of entity [%s] with type [%s].",
            attribute.getDataType().toString().toLowerCase(),
            dataValue,
            attribute.getLabel(),
            entity.getLabelValue(),
            entityType.getId());
    if (message != null) {
      fullMessage += " " + message;
    }

    return new ConstraintViolation(fullMessage);
  }

  /** Package-private for testability. */
  Object getDataValuesForType(Entity entity, Attribute attribute) {
    String attributeName = attribute.getName();
    switch (attribute.getDataType()) {
      case DATE:
        return entity.getLocalDate(attributeName);
      case DATE_TIME:
        return entity.getInstant(attributeName);
      case BOOL:
        return entity.getBoolean(attributeName);
      case DECIMAL:
        return entity.getDouble(attributeName);
      case LONG:
        return entity.getLong(attributeName);
      case INT:
        return entity.getInt(attributeName);
      case HYPERLINK:
      case ENUM:
      case HTML:
      case TEXT:
      case SCRIPT:
      case EMAIL:
      case STRING:
        return entity.getString(attributeName);
      case CATEGORICAL:
      case XREF:
      case FILE:
        Entity refEntity = entity.getEntity(attributeName);
        if (refEntity != null) return refEntity.getIdValue();
        else return "";
      case CATEGORICAL_MREF:
      case MREF:
        List<String> mrefValues = newArrayList();
        for (Entity mrefEntity : entity.getEntities(attributeName)) {
          if (mrefEntity != null) {
            mrefValues.add(mrefEntity.getIdValue().toString());
          }
        }
        return mrefValues;
      default:
        return "";
    }
  }
}
