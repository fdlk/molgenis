package org.molgenis.data.discovery.service.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.model.matching.IdentifiableTagGroup;
import org.molgenis.data.discovery.model.matching.MatchingExplanation;
import org.molgenis.data.discovery.service.OntologyBasedExplainService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = OntologyBasedExplainServiceImplTest.Config.class)
public class OntologyBasedExplainServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	OntologyService ontologyService;

	@Autowired
	OntologyBasedExplainService ontologyBasedExplainServiceImpl;

	@Autowired
	IdGenerator idGenerator;

	@Autowired
	AttributeCandidateScoringImpl attributeCandidateScoringImpl;

	@Autowired
	MolgenisUserMetaData molgenisUserMetaData;

	@BeforeMethod
	public void setup()
	{
		when(idGenerator.generateId()).thenReturn("identifier");
	}

	@Test
	public void computeScoreForMatchedSource()
	{
		// Define the data
		SemanticType semanticType = SemanticType.create("1", "smoking", "smoking", true);

		OntologyTermImpl targetOntologyTerm = OntologyTermImpl
				.create("C0302836", "C0302836", "Cigar Smoker", StringUtils.EMPTY, Lists.newArrayList("Cigar Smoker"),
						emptyList(), emptyList(), asList(semanticType));

		OntologyTermImpl sourceOntologyTerm = OntologyTermImpl
				.create("C0302836", "C0302836", "Smoking tobacco", StringUtils.EMPTY,
						Lists.newArrayList("Smoking tobacco", "Smoked Tobacco"), emptyList(), emptyList(),
						asList(semanticType));

		IdentifiableTagGroup targetTagGroup = IdentifiableTagGroup
				.create("1", Arrays.asList(targetOntologyTerm), Collections.emptyList(), "cigar smoker", 0.7f);

		IdentifiableTagGroup sourceTagGroup = IdentifiableTagGroup
				.create("2", Arrays.asList(sourceOntologyTerm), Collections.emptyList(), "tobacco smoke", 0.3f);

		BiobankUniverse biobankUniverse = BiobankUniverse
				.create("1", "test universe", emptyList(), new MolgenisUser(molgenisUserMetaData), emptyList(),
						emptyList());

		BiobankSampleCollection collection = BiobankSampleCollection.create("test collection");

		BiobankSampleAttribute targetAttribute = BiobankSampleAttribute
				.create("1", "SMK_CIGAR_CURRENT", "Current Cigar Smoker", "", collection,
						Arrays.asList(targetTagGroup));

		BiobankSampleAttribute sourceAttribute1 = BiobankSampleAttribute.create("2", "SMK121",
				"How many hours a day you are exposed to the tobacco smoke of others? (Repeat) (1)", "", collection,
				Arrays.asList(sourceTagGroup));

		BiobankSampleAttribute sourceAttribute2 = BiobankSampleAttribute
				.create("3", "SMK231", "Current", "", collection, Collections.emptyList());

		BiobankSampleAttribute sourceAttribute3 = BiobankSampleAttribute
				.create("4", "SMK234", "Current cigar smoker", "", collection, Collections.emptyList());

		SearchParam searchParam = SearchParam.create(Collections.emptySet(), Collections.emptyList());

		Multimap<OntologyTermImpl, OntologyTermImpl> relatedOntologyTerms = LinkedHashMultimap.create();
		relatedOntologyTerms.put(targetOntologyTerm, sourceOntologyTerm);

		// Define the actions
		when(ontologyService.isDescendant(sourceOntologyTerm, targetOntologyTerm)).thenReturn(true);

		when(ontologyService.getOntologyTermSemanticRelatedness(sourceOntologyTerm, targetOntologyTerm))
				.thenReturn(0.5);

		when(ontologyService.related(targetOntologyTerm, sourceOntologyTerm, OntologyBasedMatcher.STOP_LEVEL))
				.thenReturn(true);

		when(ontologyService
				.areWithinDistance(targetOntologyTerm, sourceOntologyTerm, OntologyBasedMatcher.EXPANSION_LEVEL))
				.thenReturn(true);

		when(attributeCandidateScoringImpl
				.score(targetAttribute, sourceAttribute1, biobankUniverse, relatedOntologyTerms,
						searchParam.getMatchParam().isStrictMatch()))
				.thenReturn(Hit.create("cigar smoker tobacco smoking", 0.4f));

		when(attributeCandidateScoringImpl
				.score(targetAttribute, sourceAttribute2, biobankUniverse, relatedOntologyTerms,
						searchParam.getMatchParam().isStrictMatch())).thenReturn(Hit.create("current", 0.3f));

		when(attributeCandidateScoringImpl
				.score(targetAttribute, sourceAttribute3, biobankUniverse, relatedOntologyTerms,
						searchParam.getMatchParam().isStrictMatch()))
				.thenReturn(Hit.create("current cigar smoker", 1.0f));

		// Test
		List<AttributeMappingCandidate> attributeMappingCandidates = ontologyBasedExplainServiceImpl
				.explain(biobankUniverse, searchParam, targetAttribute,
						Arrays.asList(sourceAttribute1, sourceAttribute2, sourceAttribute3),
						attributeCandidateScoringImpl);

		AttributeMappingCandidate candidate1 = AttributeMappingCandidate
				.create("identifier", biobankUniverse, targetAttribute, sourceAttribute1, MatchingExplanation
						.create("identifier", Arrays.asList(sourceOntologyTerm), "cigar smoker tobacco smoking",
								"cigar smoker tobacco smoking", 0.4f));

		AttributeMappingCandidate candidate3 = AttributeMappingCandidate
				.create("identifier", biobankUniverse, targetAttribute, sourceAttribute3, MatchingExplanation
						.create("identifier", emptyList(), "Current Cigar Smoker", "current cigar smoker", 1.0f));

		Assert.assertEquals(attributeMappingCandidates, Arrays.asList(candidate3, candidate1));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		public OntologyService ontologyService()
		{
			return mock(OntologyService.class);
		}

		@Bean
		public AttributeCandidateScoringImpl attributeCandidateScoringImpl()
		{
			return mock(AttributeCandidateScoringImpl.class);
		}

		@Bean
		public MolgenisUserMetaData molgenisUserMetaData()
		{
			return mock(MolgenisUserMetaData.class);
		}

		@Bean
		public OntologyBasedExplainService ontologyBasedExplainService()
		{
			return new OntologyBasedExplainServiceImpl(idGenerator(), ontologyService());
		}
	}
}