package org.molgenis.data.discovery.service.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.IdentifiableTagGroup;
import org.molgenis.data.discovery.scoring.attributes.AttributeSimilarity;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.ontology.core.service.OntologyService;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class AttributeCandidateScoringImplTest
{
	OntologyService ontologyService;

	AttributeCandidateScoringImpl attributeCandidateScoringImpl;

	AttributeSimilarity attributeSimilarity;

	@BeforeTest
	public void beforeTest()
	{
		ontologyService = mock(OntologyService.class);
		attributeSimilarity = mock(AttributeSimilarity.class);
		attributeCandidateScoringImpl = new AttributeCandidateScoringImpl(ontologyService, attributeSimilarity);
	}

	@Test
	public void testScore()
	{
		BiobankUniverse biobankUniverse = BiobankUniverse
				.create("1", "test", emptyList(), new MolgenisUser(mock(MolgenisUserMetaData.class)), emptyList(),
						emptyList());

		BiobankSampleCollection biobankSampleCollection = BiobankSampleCollection.create("test-collection");

		OntologyTermImpl vegetables = OntologyTermImpl.create("1", "iri1", "Vegetables", asList("Vegetables"));

		OntologyTermImpl beans = OntologyTermImpl.create("2", "iri2", "Beans", asList("Beans"));

		OntologyTermImpl consumption = OntologyTermImpl.create("3", "iri3", "Consumption", asList("Consumption"));

		IdentifiableTagGroup tagGroup1 = IdentifiableTagGroup
				.create("1", Arrays.asList(consumption, vegetables), emptyList(), "consumption vegetables", 1.0f);

		IdentifiableTagGroup tagGroup2 = IdentifiableTagGroup
				.create("2", Arrays.asList(consumption, beans), emptyList(), "consumption beans", 1.0f);

		BiobankSampleAttribute targetAttribute = BiobankSampleAttribute
				.create("1", "targetAttribute", "Consumption of vegetables", StringUtils.EMPTY, biobankSampleCollection,
						Arrays.asList(tagGroup1));

		BiobankSampleAttribute sourceAttribute = BiobankSampleAttribute
				.create("2", "sourceAttribute", "Consumption of beans", StringUtils.EMPTY, biobankSampleCollection,
						Arrays.asList(tagGroup2));

		Multimap<OntologyTermImpl, OntologyTermImpl> relatedOntologyTerms = LinkedHashMultimap.create();
		relatedOntologyTerms.put(consumption, consumption);
		relatedOntologyTerms.put(vegetables, beans);

		when(ontologyService.getOntologyTermSemanticRelatedness(consumption, consumption)).thenReturn(1.0d);

		when(ontologyService.getOntologyTermSemanticRelatedness(vegetables, beans)).thenReturn(0.8d);

		when(ontologyService.isDescendant(consumption, consumption)).thenReturn(true);

		when(ontologyService.isDescendant(beans, vegetables)).thenReturn(true);

		when(attributeSimilarity.score("consumption of vegetables", "of consumption vegetables", false))
				.thenReturn(1.0f);

		Hit<String> score = attributeCandidateScoringImpl
				.score(targetAttribute, sourceAttribute, biobankUniverse, relatedOntologyTerms, false);

		assertEquals(score, Hit.create("consumption vegetables beans", 0.80480003f));
	}
}
