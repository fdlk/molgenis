package org.molgenis.data.discovery.filters;

import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;

public interface PostFilter
{
	boolean filter(AttributeMappingCandidate attributeMappingCandidate, SearchParam searchParam,
			BiobankUniverse biobankUniverse);
}
