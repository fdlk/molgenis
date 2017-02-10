package org.molgenis.graphql.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

/**
 * Attribute
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_Attribute.class)
public abstract class Attribute
{
//	public abstract FieldType getFieldType();

	public abstract String getName();

	public abstract String getLabel();

	@Nullable
	public abstract String getDescription();

//	public abstract List<Attribute> getAttributes();
//
//	public abstract List<String> getEnumOptions();
//
//	@Nullable
//	public abstract Long getMaxLength();
//
//	public abstract EntityType getRefEntity();
//
//	public abstract String getMappedBy();
//
//	public abstract boolean isAuto();
//
//	public abstract boolean isNillable();
//
//	public abstract boolean isReadOnly();
//
//	public abstract String getDefaultValue();
//
//	public abstract boolean isLabelAttribute();
//
//	public abstract boolean isUnique();
//
//	public abstract boolean isVisible();
//
//	public abstract boolean isLookupAttribute();
//
//	public abstract boolean isAggregatable();
//
//	public abstract Range getRange();
//
//	public abstract String getExpression();
//
//	public abstract String getVisibleExpression();
//
//	public abstract String getValidationExpression();

	public static Builder builder()
	{
		return new AutoValue_Attribute.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
//		public abstract Builder setFieldType(FieldType fieldType);

		public abstract Builder setName(String name);

		public abstract Builder setLabel(String label);

		public abstract Builder setDescription(String description);

//		public abstract Builder setAttributes(List<Attribute> attributes);
//
//		public abstract Builder setEnumOptions(List<String> enumOptions);
//
//		public abstract Builder setMaxLength(Long maxLength);
//
//		public abstract Builder setRefEntity(EntityType refEntity);
//
//		public abstract Builder setMappedBy(String mappedBy);
//
//		public abstract Builder setAuto(boolean isAuto);
//
//		public abstract Builder setNillable(boolean nillable);
//
//		public abstract Builder setReadOnly(boolean readOnly);
//
//		public abstract Builder setDefaultValue(String defaultValue);
//
//		public abstract Builder setLabelAttribute(boolean labelAttribute);
//
//		public abstract Builder setUnique(boolean unique);
//
//		public abstract Builder setVisible(boolean visible);
//
//		public abstract Builder setLookupAttribute(boolean isLookupAttribute);
//
//		public abstract Builder setAggregatable(boolean aggregatable);
//
//		public abstract Builder setRange(Range range);
//
//		public abstract Builder setExpression(String expression);
//
//		public abstract Builder setVisibleExpression(String visibleExpression);
//
//		public abstract Builder setValidationExpression(String validationExpression);

		public abstract Attribute build();
	}

}

