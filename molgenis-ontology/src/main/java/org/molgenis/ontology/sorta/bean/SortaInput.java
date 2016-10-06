package org.molgenis.ontology.sorta.bean;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.*;
import static org.molgenis.ontology.sorta.service.impl.SortaServiceImpl.*;

public class SortaInput
{
	private final Entity inputEntity;

	public SortaInput(Entity inputEntity)
	{
		this.inputEntity = requireNonNull(inputEntity);
	}

	public List<String> getLexicalMatchAttributes()
	{
		return stream(inputEntity.getAttributeNames().spliterator(), false).filter(this::isAttrNameValidForLexicalMatch)
				.collect(toList());
	}

	public String getValue(String attributeName)
	{
		String string = inputEntity.getString(attributeName);
		return isNotBlank(string) ? string : StringUtils.EMPTY;
	}

	public List<String> getAnnotationMatchAttributes()
	{
		return stream(inputEntity.getAttributeNames().spliterator(), false)
				.filter(this::isAttrNameValidForAnnotationMatch).collect(toList());
	}

	private boolean isAttrNameValidForLexicalMatch(String attr)
	{
		return !attr.equalsIgnoreCase(DEFAULT_MATCHING_IDENTIFIER) && (
				equalsIgnoreCase(attr, DEFAULT_MATCHING_NAME_FIELD) || containsIgnoreCase(attr,
						DEFAULT_MATCHING_SYNONYM_PREFIX_FIELD));
	}

	private boolean isAttrNameValidForAnnotationMatch(String attr)
	{
		return !attr.equalsIgnoreCase(DEFAULT_MATCHING_IDENTIFIER) && !equalsIgnoreCase(attr,
				DEFAULT_MATCHING_NAME_FIELD) && !containsIgnoreCase(attr, DEFAULT_MATCHING_SYNONYM_PREFIX_FIELD);
	}
}