package org.molgenis.data.discovery.service;

import java.util.List;

import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.service.impl.AttributeCandidateScoringImpl;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;

public interface OntologyBasedExplainService
{
	public abstract List<AttributeMappingCandidate> explain(BiobankUniverse biobankUniverse, SearchParam searchParam,
			BiobankSampleAttribute targetAttribute, List<BiobankSampleAttribute> sourceAttributes,
			AttributeCandidateScoringImpl attributeCandidateScoring);
}
