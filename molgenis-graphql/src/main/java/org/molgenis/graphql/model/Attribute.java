package org.molgenis.graphql.model;

import org.molgenis.data.meta.AttributeType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Statically typed wrapper class for schema generation.
 * Without any confusing extra methods and with lazy evaluation.
 */
public class Attribute
{
	private final org.molgenis.data.meta.model.Attribute attribute;

	public Attribute(org.molgenis.data.meta.model.Attribute attribute)
	{
		this.attribute = attribute;
	}

	public AttributeType getType()
	{
		return attribute.getDataType();
	}

	public String getName()
	{
		return attribute.getName();
	}

	public String getLabel()
	{
		return attribute.getLabel();
	}

	@Nullable
	public String getDescription()
	{
		return attribute.getDescription();
	}

	public List<Attribute> getChildren()
	{
		return StreamSupport.stream(attribute.getChildren().spliterator(), false).map(Attribute::new)
				.collect(Collectors.toList());
	}

	public List<String> getEnumOptions()
	{
		return attribute.getEnumOptions();
	}

	//
	//	@Nullable
	//	public abstract Long getMaxLength();
	//
	public EntityType getRefEntity()
	{
		if (attribute.getRefEntity() == null)
		{
			return null;
		}
		return new EntityType(attribute.getRefEntity());
	}

	public boolean isMappedBy()
	{
		return attribute.isMappedBy();
	}

	public Attribute getMappedBy()
	{
		return new Attribute(attribute.getMappedBy());
	}

	public boolean isAuto()
	{
		return attribute.isAuto();
	}

	public boolean isNillable()
	{
		return attribute.isNillable();
	}
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
}

