package org.molgenis.data.discovery.filters;

import org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.BiobankAttributeDataType;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;

import java.util.EnumMap;
import java.util.EnumSet;

import static org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.BiobankAttributeDataType.*;

/**
 *
 */
public class DataTypePostFilter implements PostFilter
{
	private static EnumMap<BiobankAttributeDataType, EnumSet<BiobankAttributeDataType>> DATA_TYPE_DISALLOWED;

	static
	{
		DATA_TYPE_DISALLOWED = new EnumMap<>(BiobankAttributeDataType.class);
		DATA_TYPE_DISALLOWED.put(STRING, EnumSet.of(DATE, INT, DECIMAL));
		DATA_TYPE_DISALLOWED.put(DATE, EnumSet.of(STRING));
		DATA_TYPE_DISALLOWED.put(INT, EnumSet.of(STRING));
		DATA_TYPE_DISALLOWED.put(DECIMAL, EnumSet.of(STRING));
		DATA_TYPE_DISALLOWED.put(CATEGORICAL, EnumSet.of(DATE, INT, DECIMAL));
	}

	@Override
	public boolean filter(AttributeMappingCandidate attributeMappingCandidate, SearchParam searchParam,
			BiobankUniverse biobankUniverse)
	{
		BiobankAttributeDataType targetDataType = attributeMappingCandidate.getTarget().getBiobankAttributeDataType();

		BiobankAttributeDataType sourceDataType = attributeMappingCandidate.getSource().getBiobankAttributeDataType();

		return !DATA_TYPE_DISALLOWED.get(targetDataType).contains(sourceDataType);
	}
}
