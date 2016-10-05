package org.molgenis.data.discovery.service;

import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.service.impl.AttributeCandidateScoringImpl;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;

import java.util.List;

public interface OntologyBasedExplainService
{
	/**
	 * Explain how the a list of {@link BiobankSampleAttribute}s get matched based on the given {@link SearchParam}
	 *
	 * @param biobankUniverse
	 * @param searchParam
	 * @param targetAttribute
	 * @param sourceAttributes
	 * @param attributeCandidateScoring
	 * @return
	 */
	List<AttributeMappingCandidate> explain(BiobankUniverse biobankUniverse, SearchParam searchParam,
			BiobankSampleAttribute targetAttribute, List<BiobankSampleAttribute> sourceAttributes,
			AttributeCandidateScoringImpl attributeCandidateScoring);
}
