package org.molgenis.data.discovery.service.impl;

import com.google.common.collect.Lists;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.BiobankAttributeDataType;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.biobank.BiobankUniverseMemberVector;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.model.matching.BiobankSampleCollectionSimilarity;
import org.molgenis.data.discovery.model.matching.IdentifiableTagGroup;
import org.molgenis.data.discovery.repo.BiobankUniverseRepository;
import org.molgenis.data.discovery.scoring.attributes.VectorSpaceModelAttributeSimilarity;
import org.molgenis.data.discovery.scoring.collections.VectorSpaceModelCollectionSimilarity;
import org.molgenis.data.discovery.service.BiobankUniverseService;
import org.molgenis.data.discovery.service.OntologyBasedExplainService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.BiobankAttributeDataType.toEnum;

public class BiobankUniverseServiceImpl implements BiobankUniverseService
{
	private static final Logger LOG = LoggerFactory.getLogger(BiobankUniverseServiceImpl.class);
	private final static int MAX_NUMBER_MATCHES = 50;

	private final IdGenerator idGenerator;
	private final BiobankUniverseRepository biobankUniverseRepository;
	private final OntologyService ontologyService;
	private final TagGroupGenerator tagGroupGenerator;
	private final OntologyBasedExplainService ontologyBasedExplainService;
	private final AttributeCandidateScoringImpl biobankUniverseScore;
	private final VectorSpaceModelCollectionSimilarity vectorSpaceModelCollectionSimilarity;

	@Autowired
	public BiobankUniverseServiceImpl(IdGenerator idGenerator, BiobankUniverseRepository biobankUniverseRepository,
			OntologyService ontologyService, TagGroupGenerator tagGroupGenerator,
			ExplainMappingService explainMappingService, OntologyBasedExplainService ontologyBasedExplainService,
			TermFrequencyService termFrequencyService)
	{
		this.idGenerator = requireNonNull(idGenerator);
		this.biobankUniverseRepository = biobankUniverseRepository;
		this.ontologyService = requireNonNull(ontologyService);
		this.tagGroupGenerator = requireNonNull(tagGroupGenerator);
		this.ontologyBasedExplainService = requireNonNull(ontologyBasedExplainService);
		this.biobankUniverseScore = new AttributeCandidateScoringImpl(ontologyService,
				new VectorSpaceModelAttributeSimilarity(termFrequencyService));
		this.vectorSpaceModelCollectionSimilarity = new VectorSpaceModelCollectionSimilarity(biobankUniverseRepository,
				ontologyService, idGenerator);
	}

	@RunAsSystem
	@Override
	public BiobankUniverse addBiobankUniverse(String universeName, List<String> semanticTypeNames, MolgenisUser owner)
	{
		List<SemanticType> semanticTypes = ontologyService.getSemanticTypesByNames(semanticTypeNames);

		BiobankUniverse biobankUniverse = BiobankUniverse
				.create(idGenerator.generateId(), universeName, emptyList(), owner, semanticTypes, emptyList());

		biobankUniverseRepository.addBiobankUniverse(biobankUniverse);

		return biobankUniverseRepository.getUniverse(biobankUniverse.getIdentifier());
	}

	@RunAsSystem
	@Override
	public void deleteBiobankUniverse(String identifier)
	{
		biobankUniverseRepository.removeBiobankUniverse(biobankUniverseRepository.getUniverse(identifier));
	}

	@RunAsSystem
	@Override
	public BiobankUniverse getBiobankUniverse(String identifier)
	{
		return biobankUniverseRepository.getUniverse(identifier);
	}

	@RunAsSystem
	@Override
	public void addBiobankUniverseMember(BiobankUniverse biobankUniverse,
			List<BiobankSampleCollection> biobankSampleCollections)
	{
		biobankUniverseRepository.addUniverseMembers(biobankUniverse, biobankSampleCollections);
	}

	@RunAsSystem
	@Override
	public List<BiobankSampleCollection> getAllBiobankSampleCollections()
	{
		return biobankUniverseRepository.getAllBiobankSampleCollections();
	}

	@RunAsSystem
	@Override
	public List<BiobankSampleCollection> getBiobankSampleCollections(List<String> biobankSampleCollectionNames)
	{
		return biobankSampleCollectionNames.stream().map(biobankUniverseRepository::getBiobankSampleCollection)
				.collect(toList());
	}

	@RunAsSystem
	@Override
	public BiobankSampleCollection getBiobankSampleCollection(String biobankSampleCollectionName)
	{
		return biobankUniverseRepository.getBiobankSampleCollection(biobankSampleCollectionName);
	}

	@RunAsSystem
	@Override
	public void removeBiobankSampleCollection(BiobankSampleCollection biobankSampleCollection)
	{
		biobankUniverseRepository.removeBiobankSampleCollection(biobankSampleCollection);
	}

	@Override
	public List<BiobankUniverse> getBiobankUniverses()
	{
		return biobankUniverseRepository.getAllUniverses();
	}

	@Override
	public List<BiobankSampleAttribute> getBiobankSampleAttributes(BiobankSampleCollection biobankSampleCollection)
	{
		return Lists.newArrayList(biobankUniverseRepository.getBiobankSampleAttributes(biobankSampleCollection));
	}

	@Override
	public int countBiobankSampleAttributes(BiobankSampleCollection biobankSampleCollection)
	{
		return biobankUniverseRepository.countBiobankSampleAttributes(biobankSampleCollection);
	}

	@Override
	public boolean isBiobankSampleCollectionTagged(BiobankSampleCollection biobankSampleCollection)
	{
		return biobankUniverseRepository.isBiobankSampleCollectionTagged(biobankSampleCollection);
	}

	@RunAsSystem
	@Override
	public void removeAllTagGroups(BiobankSampleCollection biobankSampleCollection)
	{
		Iterable<BiobankSampleAttribute> biobankSampleAttributes = biobankUniverseRepository
				.getBiobankSampleAttributes(biobankSampleCollection);

		biobankUniverseRepository.removeTagGroupsForAttributes(biobankSampleAttributes);
	}

	@RunAsSystem
	@Override
	public List<IdentifiableTagGroup> findTagGroupsForAttributes(BiobankSampleAttribute biobankSampleAttribute)
	{
		return tagGroupGenerator
				.generateTagGroups(biobankSampleAttribute.getLabel(), ontologyService.getAllOntologyIds()).stream()
				.map(this::tagGroupToIdentifiableTagGroup).collect(Collectors.toList());
	}

	@Override
	public Map<BiobankSampleCollection, List<AttributeMappingCandidate>> getAttributeCandidateMappings(
			BiobankUniverse biobankUniverse, BiobankSampleCollection target)
	{
		Iterable<AttributeMappingCandidate> attributeMappingCandidates = biobankUniverseRepository
				.getAttributeMappingCandidates(biobankUniverse, target);

		Map<BiobankSampleCollection, List<AttributeMappingCandidate>> attributeMappingCandidateTable = new LinkedHashMap<>();

		for (AttributeMappingCandidate attributeMappingCandidate : attributeMappingCandidates)
		{
			BiobankSampleCollection sourceBiobankSampleCollection = attributeMappingCandidate.getSource()
					.getCollection();

			if (!attributeMappingCandidateTable.containsKey(sourceBiobankSampleCollection))
			{
				attributeMappingCandidateTable.put(sourceBiobankSampleCollection, new ArrayList<>());
			}

			attributeMappingCandidateTable.get(sourceBiobankSampleCollection).add(attributeMappingCandidate);
		}

		return attributeMappingCandidateTable;
	}

	@Override
	public List<AttributeMappingCandidate> generateAttributeCandidateMappings(BiobankUniverse biobankUniverse,
			BiobankSampleAttribute target, SearchParam searchParam, List<OntologyBasedMatcher> ontologyBasedMatchers)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Started matching the target attribute: (" + target.getName() + ":" + target.getLabel() + ")");
		}

		List<AttributeMappingCandidate> allCandidates = new ArrayList<>();

		for (OntologyBasedMatcher ontologyBasedMatcher : ontologyBasedMatchers)
		{
			List<BiobankSampleAttribute> sourceAttributes = ontologyBasedMatcher.match(searchParam);

			List<AttributeMappingCandidate> collect = ontologyBasedExplainService
					.explain(biobankUniverse, searchParam, target, newArrayList(sourceAttributes), biobankUniverseScore)
					.stream().filter(candidate -> candidate.getExplanation().getNgramScore() > 0)
					.filter(candidate -> !candidate.getExplanation().getMatchedSourceWords().isEmpty()).sorted()
					.limit(MAX_NUMBER_MATCHES).collect(toList());

			allCandidates.addAll(collect);
		}

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Finished matching the target attribute: (" + target.getName() + ":" + target.getLabel() + ")");
		}

		return allCandidates;
	}

	@Override
	public List<AttributeMappingCandidate> getCandidateMappingsCandidates(Query<Entity> query)
	{
		return biobankUniverseRepository.getAttributeMappingCandidates(query);
	}

	@RunAsSystem
	@Transactional
	@Override
	public void importSampleCollections(String sampleName, Stream<Entity> biobankSampleAttributeEntityStream)
	{
		BiobankSampleCollection biobankSampleCollection = BiobankSampleCollection.create(sampleName);
		biobankUniverseRepository.addBiobankSampleCollection(biobankSampleCollection);

		Stream<BiobankSampleAttribute> biobankSampleAttributeStream = biobankSampleAttributeEntityStream
				.map(entity -> importedAttributEntityToBiobankSampleAttribute(biobankSampleCollection, entity))
				.filter(Objects::nonNull);

		biobankUniverseRepository.addBiobankSampleAttributes(biobankSampleAttributeStream);
	}

	@RunAsSystem
	@Override
	public void addKeyConcepts(BiobankUniverse universe, List<String> semanticTypeNames)
	{
		List<SemanticType> semanticTypes = ontologyService.getSemanticTypesByNames(semanticTypeNames);
		biobankUniverseRepository.addKeyConcepts(universe, semanticTypes);
	}

	@RunAsSystem
	@Override
	public void updateBiobankUniverseMemberVectors(BiobankUniverse biobankUniverse)
	{
		List<BiobankUniverseMemberVector> biobankUniverseMemberVectors = vectorSpaceModelCollectionSimilarity
				.createBiobankUniverseMemberVectors(biobankUniverse);

		biobankUniverseRepository.updateBiobankUniverseMemberVectors(biobankUniverse, biobankUniverseMemberVectors);
	}

	private BiobankSampleAttribute importedAttributEntityToBiobankSampleAttribute(BiobankSampleCollection collection,
			Entity entity)
	{
		String identifier = idGenerator.generateId();
		String name = entity.getString(BiobankSampleAttributeMetaData.NAME);
		String label = entity.getString(BiobankSampleAttributeMetaData.LABEL);
		BiobankAttributeDataType biobankAttributeDataType = toEnum(
				entity.getString(BiobankSampleAttributeMetaData.DATA_TYPE));
		String description = entity.getString(BiobankSampleAttributeMetaData.DESCRIPTION);

		return isNotBlank(name) ? BiobankSampleAttribute
				.create(identifier, name, label, description, biobankAttributeDataType, collection, emptyList()) : null;
	}

	private IdentifiableTagGroup tagGroupToIdentifiableTagGroup(TagGroup tagGroup)
	{
		String identifier = idGenerator.generateId();
		String matchedWords = tagGroup.getMatchedWords();
		float score = tagGroup.getScore();

		List<SemanticType> semanticTypes = tagGroup.getOntologyTerms().stream()
				.flatMap(ot -> ot.getSemanticTypes().stream()).collect(toList());

		return IdentifiableTagGroup.create(identifier, tagGroup.getOntologyTerms(), semanticTypes, matchedWords, score);
	}

	@Override
	public List<BiobankSampleCollectionSimilarity> getCollectionSimilarities(BiobankUniverse biobankUniverse)
	{
		BiobankUniverseMemberVector[] array = biobankUniverse.getVectors().stream()
				.toArray(BiobankUniverseMemberVector[]::new);

		List<BiobankSampleCollectionSimilarity> collectionSimilarities = new ArrayList<>();
		for (int i = 0; i < array.length; i++)
		{
			BiobankUniverseMemberVector target = array[i];
			for (int j = i + 1; j < array.length; j++)
			{
				BiobankUniverseMemberVector source = array[j];
				collectionSimilarities.add(vectorSpaceModelCollectionSimilarity.cosineValue(target, source));
			}
		}

		return collectionSimilarities;
	}
}