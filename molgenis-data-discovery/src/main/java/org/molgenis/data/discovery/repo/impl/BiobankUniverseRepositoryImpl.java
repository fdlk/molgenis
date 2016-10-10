package org.molgenis.data.discovery.repo.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.*;
import org.molgenis.data.discovery.job.BiobankUniverseJobExecutionMetaData;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleCollectionMetaData;
import org.molgenis.data.discovery.meta.biobank.BiobankUniverseMetaData;
import org.molgenis.data.discovery.meta.matching.AttributeMappingCandidateMetaData;
import org.molgenis.data.discovery.meta.matching.AttributeMappingDecisionMetaData;
import org.molgenis.data.discovery.meta.matching.AttributeMappingDecisionMetaData.DecisionOptions;
import org.molgenis.data.discovery.meta.matching.MatchingExplanationMetaData;
import org.molgenis.data.discovery.meta.matching.TagGroupMetaData;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.biobank.BiobankUniverseMemberVector;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.model.matching.AttributeMappingDecision;
import org.molgenis.data.discovery.model.matching.IdentifiableTagGroup;
import org.molgenis.data.discovery.model.matching.MatchingExplanation;
import org.molgenis.data.discovery.repo.BiobankUniverseRepository;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermEntity;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.SemanticTypeMetaData;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.security.user.UserAccountService;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.discovery.job.BiobankUniverseJobExecutionMetaData.BIOBANK_UNIVERSE_JOB_EXECUTION;
import static org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.BIOBANK_SAMPLE_ATTRIBUTE;
import static org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.TAG_GROUPS;
import static org.molgenis.data.discovery.meta.biobank.BiobankSampleCollectionMetaData.BIOBANK_SAMPLE_COLLECTION;
import static org.molgenis.data.discovery.meta.biobank.BiobankUniverseMetaData.BIOBANK_UNIVERSE;
import static org.molgenis.data.discovery.meta.matching.AttributeMappingCandidateMetaData.*;
import static org.molgenis.data.discovery.meta.matching.AttributeMappingDecisionMetaData.ATTRIBUTE_MAPPING_DECISION;
import static org.molgenis.data.discovery.meta.matching.MatchingExplanationMetaData.MATCHING_EXPLANATION;
import static org.molgenis.data.discovery.meta.matching.TagGroupMetaData.TAG_GROUP;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.data.support.QueryImpl.IN;

public class BiobankUniverseRepositoryImpl implements BiobankUniverseRepository
{
	private final DataService dataService;
	private final MolgenisUserService molgenisUserService;
	private final UserAccountService userAcountService;
	private final EntityManager entityManager;
	private final BiobankUniverseMetaData biobankUniverseMetaData;
	private final BiobankSampleCollectionMetaData biobankSampleCollectionMetaData;
	private final BiobankSampleAttributeMetaData biobankSampleAttributeMetaData;
	private final AttributeMappingCandidateMetaData attributeMappingCandidateMetaData;
	private final MatchingExplanationMetaData matchingExplanationMetaData;
	private final AttributeMappingDecisionMetaData attributeMappingDecisionMetaData;
	private final TagGroupMetaData tagGroupMetaData;
	private final OntologyTermMetaData ontologyTermMetaData;
	private final SemanticTypeMetaData semanticTypeMetaData;

	public BiobankUniverseRepositoryImpl(DataService dataService, MolgenisUserService molgenisUserService,
			UserAccountService userAcountService, EntityManager entityManager,
			BiobankUniverseMetaData biobankUniverseMetaData,
			BiobankSampleCollectionMetaData biobankSampleCollectionMetaData,
			BiobankSampleAttributeMetaData biobankSampleAttributeMetaData,
			MatchingExplanationMetaData matchingExplanationMetaData,
			AttributeMappingCandidateMetaData attributeMappingCandidateMetaData,
			AttributeMappingDecisionMetaData attributeMappingDecisionMetaData, TagGroupMetaData tagGroupMetaData,
			OntologyTermMetaData ontologyTermMetaData, SemanticTypeMetaData semanticTypeMetaData)
	{
		this.dataService = requireNonNull(dataService);
		this.molgenisUserService = requireNonNull(molgenisUserService);
		this.userAcountService = requireNonNull(userAcountService);
		this.entityManager = requireNonNull(entityManager);
		this.biobankUniverseMetaData = requireNonNull(biobankUniverseMetaData);
		this.biobankSampleCollectionMetaData = requireNonNull(biobankSampleCollectionMetaData);
		this.biobankSampleAttributeMetaData = requireNonNull(biobankSampleAttributeMetaData);
		this.attributeMappingCandidateMetaData = requireNonNull(attributeMappingCandidateMetaData);
		this.attributeMappingDecisionMetaData = requireNonNull(attributeMappingDecisionMetaData);
		this.matchingExplanationMetaData = requireNonNull(matchingExplanationMetaData);
		this.tagGroupMetaData = requireNonNull(tagGroupMetaData);
		this.ontologyTermMetaData = requireNonNull(ontologyTermMetaData);
		this.semanticTypeMetaData = requireNonNull(semanticTypeMetaData);
	}

	@Override
	public void addKeyConcepts(BiobankUniverse biobankUniverse, List<SemanticType> semanticTypes)
	{
		List<SemanticType> keyConcepts = Lists.newArrayList(biobankUniverse.getKeyConcepts());
		List<SemanticType> newKeyConcepts = semanticTypes.stream().filter(type -> !keyConcepts.contains(type))
				.collect(toList());

		if (newKeyConcepts.size() > 0)
		{
			keyConcepts.addAll(newKeyConcepts);
			BiobankUniverse newBiobankUniverse = BiobankUniverse
					.create(biobankUniverse.getIdentifier(), biobankUniverse.getName(), biobankUniverse.getMembers(),
							biobankUniverse.getOwner(), keyConcepts, biobankUniverse.getVectors());
			dataService.update(BIOBANK_UNIVERSE, biobankUniverseToEntity(newBiobankUniverse));
		}
	}

	@Override
	public List<BiobankUniverse> getAllUniverses()
	{
		List<BiobankUniverse> universes = dataService.findAll(BIOBANK_UNIVERSE).map(this::entityToBiobankUniverse)
				.collect(toList());
		return universes;
	}

	@Override
	public BiobankUniverse getUniverse(String identifier)
	{
		Fetch fetch = new Fetch();
		biobankUniverseMetaData.getAtomicAttributes().forEach(attr -> fetch.field(attr.getName()));
		Entity findOne = dataService
				.findOne(BIOBANK_UNIVERSE, QueryImpl.EQ(BiobankUniverseMetaData.IDENTIFIER, identifier).fetch(fetch));
		return findOne == null ? null : entityToBiobankUniverse(findOne);
	}

	@Override
	public void addBiobankUniverse(BiobankUniverse biobankUniverse)
	{
		Entity entity = biobankUniverseToEntity(biobankUniverse);
		dataService.add(BIOBANK_UNIVERSE, entity);
	}

	@Override
	public void removeBiobankUniverse(BiobankUniverse biobankUniverse)
	{
		List<String> attributeIdentifiers = biobankUniverse.getMembers().stream()
				.flatMap(member -> getBiobankSampleAttributeIdentifiers(member).stream()).collect(toList());

		if (!attributeIdentifiers.isEmpty())
		{
			Fetch fetchOntologyTerm = new Fetch();
			ontologyTermMetaData.getAtomicAttributes().forEach(field -> fetchOntologyTerm.field(field.getName()));

			Fetch fetchSemanticType = new Fetch();
			semanticTypeMetaData.getAtomicAttributes().forEach(field -> fetchSemanticType.field(field.getName()));

			Fetch fetchTagGroupFields = new Fetch();
			tagGroupMetaData.getAtomicAttributes().forEach(attribute -> fetchTagGroupFields.field(attribute.getName()));
			fetchTagGroupFields.field(TagGroupMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);
			fetchTagGroupFields.field(TagGroupMetaData.SEMANTIC_TYPES, fetchSemanticType);

			Fetch fetchBiobankSampleAttribute = new Fetch();
			biobankSampleAttributeMetaData.getAtomicAttributes()
					.forEach(attribute -> fetchBiobankSampleAttribute.field(attribute.getName()));
			fetchBiobankSampleAttribute.field(BiobankSampleAttributeMetaData.COLLECTION);
			fetchBiobankSampleAttribute.field(BiobankSampleAttributeMetaData.TAG_GROUPS, fetchTagGroupFields);

			Fetch fetchExplanation = new Fetch();
			matchingExplanationMetaData.getAtomicAttributes()
					.forEach(attribute -> fetchExplanation.field(attribute.getName()));
			fetchExplanation.field(MatchingExplanationMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);

			Fetch attributeMappingCandidateFetch = new Fetch();
			attributeMappingCandidateMetaData.getAtomicAttributes()
					.forEach(attribute -> attributeMappingCandidateFetch.field(attribute.getName()));
			attributeMappingCandidateFetch.field(AttributeMappingCandidateMetaData.TARGET, fetchBiobankSampleAttribute);
			attributeMappingCandidateFetch.field(AttributeMappingCandidateMetaData.SOURCE, fetchBiobankSampleAttribute);
			attributeMappingCandidateFetch.field(AttributeMappingCandidateMetaData.EXPLANATION, fetchExplanation);
			attributeMappingCandidateFetch.field(AttributeMappingCandidateMetaData.DECISIONS);

			List<QueryRule> innerQueryRules = newArrayList(new QueryRule(TARGET, IN, attributeIdentifiers),
					new QueryRule(OR), new QueryRule(SOURCE, IN, attributeIdentifiers));

			List<QueryRule> nestedQueryRules = newArrayList(
					new QueryRule(AttributeMappingCandidateMetaData.BIOBANK_UNIVERSE, EQUALS,
							biobankUniverse.getIdentifier()), new QueryRule(AND), new QueryRule(innerQueryRules));

			List<Entity> attributeMappingCandidateEntities = dataService.findAll(ATTRIBUTE_MAPPING_CANDIDATE,
					new QueryImpl<Entity>(nestedQueryRules).fetch(attributeMappingCandidateFetch)).collect(toList());

			// Remove attributeMappingCandidates, explanations and decisions
			removeAttributeMappingCandidates(attributeMappingCandidateEntities);
		}

		// Remove the BiobankUniverseJobExecutions in which the universe is involved
		Stream<Entity> biobankUniverseJobEntityStream = dataService.findAll(BIOBANK_UNIVERSE_JOB_EXECUTION,
				QueryImpl.EQ(BiobankUniverseJobExecutionMetaData.UNIVERSE, biobankUniverse.getIdentifier()));

		dataService.delete(BIOBANK_UNIVERSE_JOB_EXECUTION, biobankUniverseJobEntityStream);

		// Remove the BiobankUniverse itself
		dataService.delete(BIOBANK_UNIVERSE, biobankUniverseToEntity(biobankUniverse));
	}

	@Override
	public void addUniverseMembers(BiobankUniverse biobankUniverse, List<BiobankSampleCollection> members)
	{
		List<BiobankSampleCollection> allMembers = Stream
				.concat(biobankUniverse.getMembers().stream(), members.stream()).distinct().collect(toList());

		List<BiobankUniverseMemberVector> allVectors = concat(
				members.stream().filter(member -> !biobankUniverse.getMembers().contains(member))
						.map(member -> BiobankUniverseMemberVector.create(member, new double[0])),
				biobankUniverse.getVectors().stream()).collect(toList());

		Entity biobankUniverseToEntity = biobankUniverseToEntity(BiobankUniverse
				.create(biobankUniverse.getIdentifier(), biobankUniverse.getName(), allMembers,
						biobankUniverse.getOwner(), biobankUniverse.getKeyConcepts(), allVectors));

		dataService.update(BIOBANK_UNIVERSE, biobankUniverseToEntity);
	}

	@Override
	public void updateBiobankUniverseMemberVectors(BiobankUniverse biobankUniverse,
			List<BiobankUniverseMemberVector> biobankUniverseMemberVectors)
	{
		BiobankUniverse updatedBiobankUniverse = BiobankUniverse
				.create(biobankUniverse.getIdentifier(), biobankUniverse.getName(), biobankUniverse.getMembers(),
						biobankUniverse.getOwner(), biobankUniverse.getKeyConcepts(), biobankUniverseMemberVectors);

		dataService.update(BIOBANK_UNIVERSE, biobankUniverseToEntity(updatedBiobankUniverse));
	}

	@Override
	public void removeUniverseMembers(BiobankUniverse biobankUniverse, List<BiobankSampleCollection> members)
	{
		List<BiobankSampleCollection> remainingMembers = biobankUniverse.getMembers().stream()
				.filter(member -> !members.contains(member)).collect(toList());

		List<BiobankUniverseMemberVector> remainingVectors = biobankUniverse.getVectors().stream()
				.filter(vector -> !members.contains(vector.getBiobankSampleCollection())).collect(toList());

		Entity biobankUniverseToEntity = biobankUniverseToEntity(BiobankUniverse
				.create(biobankUniverse.getIdentifier(), biobankUniverse.getName(), remainingMembers,
						biobankUniverse.getOwner(), biobankUniverse.getKeyConcepts(), remainingVectors));

		dataService.update(BIOBANK_UNIVERSE, biobankUniverseToEntity);
	}

	@Override
	public void addBiobankSampleCollection(BiobankSampleCollection biobankSampleCollection)
	{
		dataService.add(BIOBANK_SAMPLE_COLLECTION, biobankSampleCollectionToEntity(biobankSampleCollection));
	}

	@Override
	public void removeBiobankSampleCollection(BiobankSampleCollection biobankSampleCollection)
	{
		Entity biobankSampleCollectionToEntity = biobankSampleCollectionToEntity(biobankSampleCollection);

		// Remove bioibankSampleAttribute, attributeMappingCandidates, Explanations and Decisions
		removeBiobankSampleAttributes(getBiobankSampleAttributes(biobankSampleCollection));

		// Remove the biobankSampleColleciton membership from all BiobankUniverses
		dataService.findAll(BIOBANK_UNIVERSE,
				QueryImpl.EQ(BiobankUniverseMetaData.MEMBERS, biobankSampleCollectionToEntity))
				.map(this::entityToBiobankUniverse)
				.forEach(universe -> removeUniverseMembers(universe, Arrays.asList(biobankSampleCollection)));

		// Remove the biobankSampleCollection itself
		dataService.delete(BIOBANK_SAMPLE_COLLECTION, biobankSampleCollectionToEntity);
	}

	@Override
	public List<BiobankSampleCollection> getAllBiobankSampleCollections()
	{
		return dataService.findAll(BIOBANK_SAMPLE_COLLECTION).map(this::entityToBiobankSampleCollection)
				.collect(toList());
	}

	@Override
	public BiobankSampleCollection getBiobankSampleCollection(String name)
	{
		Entity entity = dataService.findOneById(BIOBANK_SAMPLE_COLLECTION, name);
		return entity == null ? null : entityToBiobankSampleCollection(entity);
	}

	@Override
	public boolean isBiobankSampleCollectionTagged(BiobankSampleCollection biobankSampleCollection)
	{
		Fetch fetchOntologyTerm = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(field -> fetchOntologyTerm.field(field.getName()));

		Fetch fetchSemanticType = new Fetch();
		semanticTypeMetaData.getAtomicAttributes().forEach(field -> fetchSemanticType.field(field.getName()));

		Fetch fetchTagGroupFields = new Fetch();
		tagGroupMetaData.getAtomicAttributes().forEach(attribute -> fetchTagGroupFields.field(attribute.getName()));
		fetchTagGroupFields.field(TagGroupMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);
		fetchTagGroupFields.field(TagGroupMetaData.SEMANTIC_TYPES, fetchSemanticType);

		Fetch fetch = new Fetch();
		biobankSampleAttributeMetaData.getAtomicAttributes().forEach(attribute -> fetch.field(attribute.getName()));
		fetch.field(BiobankSampleAttributeMetaData.COLLECTION);
		fetch.field(BiobankSampleAttributeMetaData.TAG_GROUPS, fetchTagGroupFields);

		// Check if the first 100 biobankSampleAttributes have been tagged
		boolean anyMatch = dataService.findAll(BIOBANK_SAMPLE_ATTRIBUTE,
				EQ(BiobankSampleAttributeMetaData.COLLECTION, biobankSampleCollection.getName()).pageSize(100)
						.fetch(fetch)).anyMatch(entity -> Iterables.size((Iterable<?>) entity.get(TAG_GROUPS)) != 0);

		return anyMatch;
	}

	@Override
	public List<BiobankSampleAttribute> getBiobankSampleAttributes(BiobankSampleCollection biobankSampleCollection)
	{
		Fetch fetchOntologyTerm = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(field -> fetchOntologyTerm.field(field.getName()));

		Fetch fetchSemanticType = new Fetch();
		semanticTypeMetaData.getAtomicAttributes().forEach(field -> fetchSemanticType.field(field.getName()));

		Fetch fetchTagGroupFields = new Fetch();
		tagGroupMetaData.getAtomicAttributes().forEach(attribute -> fetchTagGroupFields.field(attribute.getName()));
		fetchTagGroupFields.field(TagGroupMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);
		fetchTagGroupFields.field(TagGroupMetaData.SEMANTIC_TYPES, fetchSemanticType);

		Fetch fetch = new Fetch();
		biobankSampleAttributeMetaData.getAtomicAttributes().forEach(attribute -> fetch.field(attribute.getName()));
		fetch.field(BiobankSampleAttributeMetaData.COLLECTION);
		fetch.field(BiobankSampleAttributeMetaData.TAG_GROUPS, fetchTagGroupFields);

		List<BiobankSampleAttribute> biobankSampleAttributes = dataService.findAll(BIOBANK_SAMPLE_ATTRIBUTE,
				EQ(BiobankSampleAttributeMetaData.COLLECTION, biobankSampleCollection.getName()).fetch(fetch))
				.map(this::entityToBiobankSampleAttribute).collect(toList());

		return biobankSampleAttributes;
	}

	@Override
	public int countBiobankSampleAttributes(BiobankSampleCollection biobankSampleCollection)
	{
		int count = (int) dataService.count(BIOBANK_SAMPLE_ATTRIBUTE,
				QueryImpl.EQ(BiobankSampleAttributeMetaData.COLLECTION, biobankSampleCollection.getName()));
		return count;
	}

	@Override
	public List<String> getBiobankSampleAttributeIdentifiers(BiobankSampleCollection biobankSampleCollection)
	{
		List<String> biobankSampleAttributeIdentifiers = dataService.findAll(BIOBANK_SAMPLE_ATTRIBUTE,
				EQ(BiobankSampleAttributeMetaData.COLLECTION, biobankSampleCollection.getName()))
				.map(entity -> entity.getIdValue().toString()).collect(toList());

		return biobankSampleAttributeIdentifiers;
	}

	@Override
	public void addBiobankSampleAttributes(Stream<BiobankSampleAttribute> biobankSampleAttributeStream)
	{
		Stream<Entity> biobankSampleAttributeEntityStream = biobankSampleAttributeStream
				.map(this::biobankSampleAttributeToEntity);

		dataService.add(BIOBANK_SAMPLE_ATTRIBUTE, biobankSampleAttributeEntityStream);
	}

	@Override
	public void removeBiobankSampleAttributes(List<BiobankSampleAttribute> biobankSampleAttributes)
	{
		if (!biobankSampleAttributes.isEmpty())
		{
			// Remove all associated candidate matches
			removeAttributeMappingCandidates(getAttributeMappingCandidateEntities(biobankSampleAttributes));

			// Remove all associated tag groups
			removeTagGroupsForAttributes(biobankSampleAttributes);

			Stream<Entity> biobankSampleAttributeEntityStream = stream(biobankSampleAttributes.spliterator(), false)
					.map(this::biobankSampleAttributeToEntity);

			dataService.delete(BIOBANK_SAMPLE_ATTRIBUTE, biobankSampleAttributeEntityStream);
		}
	}

	@Override
	public Stream<BiobankSampleAttribute> queryBiobankSampleAttribute(Query<Entity> query)
	{
		Fetch fetchOntologyTerm = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(field -> fetchOntologyTerm.field(field.getName()));

		Fetch fetchSemanticType = new Fetch();
		semanticTypeMetaData.getAtomicAttributes().forEach(field -> fetchSemanticType.field(field.getName()));

		Fetch fetchTagGroupFields = new Fetch();
		tagGroupMetaData.getAtomicAttributes().forEach(attribute -> fetchTagGroupFields.field(attribute.getName()));
		fetchTagGroupFields.field(TagGroupMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);
		fetchTagGroupFields.field(TagGroupMetaData.SEMANTIC_TYPES, fetchSemanticType);

		Fetch fetch = new Fetch();
		biobankSampleAttributeMetaData.getAtomicAttributes()
				.forEach(attributeMetaData -> fetch.field(attributeMetaData.getName()));
		fetch.field(BiobankSampleAttributeMetaData.COLLECTION);
		fetch.field(BiobankSampleAttributeMetaData.TAG_GROUPS, fetchTagGroupFields);

		return dataService.findAll(BIOBANK_SAMPLE_ATTRIBUTE, query.fetch(fetch))
				.map(this::entityToBiobankSampleAttribute);
	}

	@Override
	public void addTagGroupsForAttributes(Iterable<BiobankSampleAttribute> biobankSampleAttributes)
	{
		Stream<Entity> tagGroupEntityStream = stream(biobankSampleAttributes.spliterator(), false)
				.flatMap(attribute -> attribute.getTagGroups().stream()).map(this::identifiableTagGroupToEntity);
		dataService.add(TagGroupMetaData.TAG_GROUP, tagGroupEntityStream);

		Stream<Entity> attributeEntityStream = stream(biobankSampleAttributes.spliterator(), false)
				.map(this::biobankSampleAttributeToEntity);

		dataService.update(BIOBANK_SAMPLE_ATTRIBUTE, attributeEntityStream);
	}

	@Override
	public void removeTagGroupsForAttributes(Iterable<BiobankSampleAttribute> biobankSampleAttributes)
	{
		Stream<Entity> identifiableTagGroupEntityStream = stream(biobankSampleAttributes.spliterator(), false)
				.flatMap(biobankSampleAttribute -> biobankSampleAttribute.getTagGroups().stream())
				.map(this::identifiableTagGroupToEntity);

		Stream<Entity> biobankSampleAttributeEntityStream = stream(biobankSampleAttributes.spliterator(), false)
				.map(biobankSampleAttribute -> BiobankSampleAttribute
						.create(biobankSampleAttribute.getIdentifier(), biobankSampleAttribute.getName(),
								biobankSampleAttribute.getLabel(), biobankSampleAttribute.getDescription(),
								biobankSampleAttribute.getCollection(), emptyList()))
				.map(this::biobankSampleAttributeToEntity);

		// Remove the TagGroup references from BiobankSampleAttributes
		dataService.update(BIOBANK_SAMPLE_ATTRIBUTE, biobankSampleAttributeEntityStream);

		// Remove the TagGroups
		dataService.delete(TAG_GROUP, identifiableTagGroupEntityStream);
	}

	@Override
	public void addAttributeMappingCandidates(List<AttributeMappingCandidate> biobankSampleAttributes)
	{
		Stream<Entity> explanationStream = biobankSampleAttributes.stream()
				.map(AttributeMappingCandidate::getExplanation).map(this::mappingExplanationToEntity);
		dataService.add(MATCHING_EXPLANATION, explanationStream);

		Stream<Entity> attributeMappingCandidateStream = biobankSampleAttributes.stream()
				.map(this::attributeMappingCandidateToEntity);
		dataService.add(ATTRIBUTE_MAPPING_CANDIDATE, attributeMappingCandidateStream);
	}

	@Override
	public Iterable<AttributeMappingCandidate> getAttributeMappingCandidates(BiobankUniverse biobankUniverse)
	{
		return getAttributeMappingCandidates(biobankUniverse, null);
	}

	@Override
	public Iterable<AttributeMappingCandidate> getAttributeMappingCandidates(BiobankUniverse biobankUniverse,
			BiobankSampleCollection target)
	{
		Fetch fetchOntologyTerm = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(field -> fetchOntologyTerm.field(field.getName()));

		Fetch fetchSemanticType = new Fetch();
		semanticTypeMetaData.getAtomicAttributes().forEach(field -> fetchSemanticType.field(field.getName()));

		Fetch fetchTagGroupFields = new Fetch();
		tagGroupMetaData.getAtomicAttributes().forEach(attribute -> fetchTagGroupFields.field(attribute.getName()));
		fetchTagGroupFields.field(TagGroupMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);
		fetchTagGroupFields.field(TagGroupMetaData.SEMANTIC_TYPES, fetchSemanticType);

		Fetch fetchBiobankSampleAttribute = new Fetch();
		biobankSampleAttributeMetaData.getAtomicAttributes()
				.forEach(attribute -> fetchBiobankSampleAttribute.field(attribute.getName()));
		fetchBiobankSampleAttribute.field(BiobankSampleAttributeMetaData.COLLECTION);
		fetchBiobankSampleAttribute.field(BiobankSampleAttributeMetaData.TAG_GROUPS, fetchTagGroupFields);

		Fetch fetchExplanation = new Fetch();
		matchingExplanationMetaData.getAtomicAttributes()
				.forEach(attribute -> fetchExplanation.field(attribute.getName()));
		fetchExplanation.field(MatchingExplanationMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);

		Fetch fetch = new Fetch();
		attributeMappingCandidateMetaData.getAtomicAttributes().forEach(attribute -> fetch.field(attribute.getName()));
		fetch.field(AttributeMappingCandidateMetaData.TARGET, fetchBiobankSampleAttribute);
		fetch.field(AttributeMappingCandidateMetaData.SOURCE, fetchBiobankSampleAttribute);
		fetch.field(AttributeMappingCandidateMetaData.EXPLANATION, fetchExplanation);
		fetch.field(AttributeMappingCandidateMetaData.DECISIONS);

		List<QueryRule> nestedQueryRules = Lists
				.newArrayList(new QueryRule(BIOBANK_UNIVERSE, EQUALS, biobankUniverse.getIdentifier()));

		if (Objects.nonNull(target))
		{
			List<String> attributeIdentifiers = getBiobankSampleAttributeIdentifiers(target);

			nestedQueryRules.addAll(Arrays.asList(new QueryRule(AND), new QueryRule(TARGET, IN, attributeIdentifiers)));
		}

		List<AttributeMappingCandidate> attributeMappingCandidates = dataService
				.findAll(ATTRIBUTE_MAPPING_CANDIDATE, new QueryImpl<Entity>(nestedQueryRules).fetch(fetch))
				.map(this::entityToAttributeMappingCandidate).collect(toList());

		return attributeMappingCandidates;
	}

	@Override
	public List<AttributeMappingCandidate> getAttributeMappingCandidates(
			List<BiobankSampleAttribute> biobankSampleAttributes)
	{
		return StreamSupport.stream(getAttributeMappingCandidateEntities(biobankSampleAttributes).spliterator(), false)
				.map(this::entityToAttributeMappingCandidate).collect(toList());
	}

	@Override
	public List<AttributeMappingCandidate> getAttributeMappingCandidates(Query<Entity> query)
	{
		Fetch fetchOntologyTerm = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(field -> fetchOntologyTerm.field(field.getName()));

		Fetch fetchSemanticType = new Fetch();
		semanticTypeMetaData.getAtomicAttributes().forEach(field -> fetchSemanticType.field(field.getName()));

		Fetch fetchTagGroupFields = new Fetch();
		tagGroupMetaData.getAtomicAttributes().forEach(attribute -> fetchTagGroupFields.field(attribute.getName()));
		fetchTagGroupFields.field(TagGroupMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);
		fetchTagGroupFields.field(TagGroupMetaData.SEMANTIC_TYPES, fetchSemanticType);

		Fetch fetchBiobankSampleAttribute = new Fetch();
		biobankSampleAttributeMetaData.getAtomicAttributes()
				.forEach(attribute -> fetchBiobankSampleAttribute.field(attribute.getName()));
		fetchBiobankSampleAttribute.field(BiobankSampleAttributeMetaData.COLLECTION);
		fetchBiobankSampleAttribute.field(BiobankSampleAttributeMetaData.TAG_GROUPS, fetchTagGroupFields);

		Fetch fetchExplanation = new Fetch();
		matchingExplanationMetaData.getAtomicAttributes()
				.forEach(attribute -> fetchExplanation.field(attribute.getName()));
		fetchExplanation.field(MatchingExplanationMetaData.ONTOLOGY_TERMS, fetchOntologyTerm);

		Fetch fetch = new Fetch();
		attributeMappingCandidateMetaData.getAtomicAttributes().forEach(attribute -> fetch.field(attribute.getName()));
		fetch.field(AttributeMappingCandidateMetaData.TARGET, fetchBiobankSampleAttribute);
		fetch.field(AttributeMappingCandidateMetaData.SOURCE, fetchBiobankSampleAttribute);
		fetch.field(AttributeMappingCandidateMetaData.EXPLANATION, fetchExplanation);
		fetch.field(AttributeMappingCandidateMetaData.DECISIONS);

		return dataService.findAll(ATTRIBUTE_MAPPING_CANDIDATE, query.fetch(fetch))
				.map(this::entityToAttributeMappingCandidate).collect(toList());
	}

	private List<Entity> getAttributeMappingCandidateEntities(Iterable<BiobankSampleAttribute> biobankSampleAttributes)
	{
		List<String> attributeIdentifiers = stream(biobankSampleAttributes.spliterator(), false)
				.map(BiobankSampleAttribute::getIdentifier).collect(toList());

		Fetch fetch = new Fetch();
		attributeMappingCandidateMetaData.getAtomicAttributes().forEach(attr -> fetch.field(attr.getName()));

		List<Entity> attributeMappingCandidateEntities = dataService.findAll(ATTRIBUTE_MAPPING_CANDIDATE,
				IN(TARGET, attributeIdentifiers).or().in(SOURCE, attributeIdentifiers).fetch(fetch)).collect(toList());

		return attributeMappingCandidateEntities;
	}

	@Override
	public void removeAttributeMappingCandidates(List<Entity> attributeMappingCandidateEntities)
	{
		Stream<Entity> mappingExplanationStream = attributeMappingCandidateEntities.stream()
				.map(entity -> entity.getEntity(AttributeMappingCandidateMetaData.EXPLANATION));

		Stream<Entity> attributeMappingDecisionStream = attributeMappingCandidateEntities.stream().flatMap(
				entity -> StreamSupport
						.stream(entity.getEntities(AttributeMappingCandidateMetaData.DECISIONS).spliterator(), false));

		dataService.delete(ATTRIBUTE_MAPPING_CANDIDATE, attributeMappingCandidateEntities.stream());
		dataService.delete(MATCHING_EXPLANATION, mappingExplanationStream);
		dataService.delete(ATTRIBUTE_MAPPING_DECISION, attributeMappingDecisionStream);
	}

	private Entity biobankUniverseToEntity(BiobankUniverse biobankUniverse)
	{
		Iterable<Entity> semanticTypeEntities = entityManager.getReferences(semanticTypeMetaData,
				biobankUniverse.getKeyConcepts().stream().map(SemanticType::getIdentifier).collect(toList()));

		Iterable<Entity> biobankSampleCollectionEntities = entityManager.getReferences(biobankSampleCollectionMetaData,
				biobankUniverse.getMembers().stream().map(BiobankSampleCollection::getName).collect(toList()));

		Entity entity = new DynamicEntity(biobankUniverseMetaData);
		entity.set(BiobankUniverseMetaData.IDENTIFIER, biobankUniverse.getIdentifier());
		entity.set(BiobankUniverseMetaData.NAME, biobankUniverse.getName());
		entity.set(BiobankUniverseMetaData.MEMBERS, biobankSampleCollectionEntities);
		entity.set(BiobankUniverseMetaData.OWNER, biobankUniverse.getOwner());
		entity.set(BiobankUniverseMetaData.KEY_CONCEPTS, semanticTypeEntities);
		entity.set(BiobankUniverseMetaData.VECTORS, vectorsToJsonString(biobankUniverse.getVectors()));

		return entity;
	}

	private String vectorsToJsonString(List<BiobankUniverseMemberVector> vectors)
	{
		Map<String, String> collect = vectors.stream().collect(Collectors
				.toMap(vector -> vector.getBiobankSampleCollection().getName(),
						vector -> Arrays.toString(vector.getPoint())));

		return new Gson().toJson(collect);
	}

	private List<BiobankUniverseMemberVector> jsonStringToVectors(String json)
	{
		Map<String, String> fromJson = new Gson().fromJson(json, new TypeToken<Map<String, String>>()
		{
		}.getType());

		List<BiobankUniverseMemberVector> vectors = new ArrayList<>();

		for (Entry<String, String> entry : fromJson.entrySet())
		{
			BiobankSampleCollection biobankSampleCollection = getBiobankSampleCollection(entry.getKey());
			String vectorString = entry.getValue();
			String[] split = vectorString.replaceAll("[\\[\\]]", StringUtils.EMPTY).split(", ");
			double[] vector = Stream.of(split).filter(StringUtils::isNotBlank).mapToDouble(Double::valueOf).toArray();
			vectors.add(BiobankUniverseMemberVector.create(biobankSampleCollection, vector));
		}

		return vectors;
	}

	private BiobankUniverse entityToBiobankUniverse(Entity entity)
	{
		String identifier = entity.getString(BiobankUniverseMetaData.IDENTIFIER);
		String name = entity.getString(BiobankUniverseMetaData.NAME);
		MolgenisUser owner = molgenisUserService
				.getUser(entity.getEntity(BiobankUniverseMetaData.OWNER).getString(MolgenisUserMetaData.USERNAME));

		List<BiobankSampleCollection> members = new ArrayList<>();
		Iterable<Entity> memberIterable = entity.getEntities(BiobankUniverseMetaData.MEMBERS);
		if (memberIterable != null)
		{
			List<BiobankSampleCollection> collect = StreamSupport.stream(memberIterable.spliterator(), false)
					.map(this::entityToBiobankSampleCollection).collect(toList());
			members.addAll(collect);
		}

		List<SemanticType> keyConcepts = new ArrayList<>();
		Iterable<Entity> keyConceptIterable = entity.getEntities(BiobankUniverseMetaData.KEY_CONCEPTS);
		if (keyConceptIterable != null)
		{
			List<SemanticType> collect = StreamSupport.stream(keyConceptIterable.spliterator(), false)
					.map(OntologyTermRepository::entityToSemanticType).collect(toList());
			keyConcepts.addAll(collect);
		}

		List<BiobankUniverseMemberVector> vectors = new ArrayList<>();
		String vectorJson = entity.getString(BiobankUniverseMetaData.VECTORS);
		if (StringUtils.isNotBlank(vectorJson))
		{
			vectors.addAll(jsonStringToVectors(vectorJson));
		}

		return BiobankUniverse.create(identifier, name, members, owner, keyConcepts, vectors);
	}

	private BiobankSampleCollection entityToBiobankSampleCollection(Entity entity)
	{
		String name = entity.getString(BiobankSampleCollectionMetaData.NAME);
		return BiobankSampleCollection.create(name);
	}

	private Entity biobankSampleCollectionToEntity(BiobankSampleCollection biobankSampleCollection)
	{
		Entity entity = new DynamicEntity(biobankSampleCollectionMetaData);
		entity.set(BiobankSampleCollectionMetaData.NAME, biobankSampleCollection.getName());
		return entity;
	}

	private BiobankSampleAttribute entityToBiobankSampleAttribute(Entity entity)
	{
		String identifier = entity.getString(BiobankSampleAttributeMetaData.IDENTIFIER);
		String name = entity.getString(BiobankSampleAttributeMetaData.NAME);
		String label = entity.getString(BiobankSampleAttributeMetaData.LABEL);
		String description = entity.getString(BiobankSampleAttributeMetaData.DESCRIPTION);

		BiobankSampleCollection biobankSampleCollection = entityToBiobankSampleCollection(
				entity.getEntity(BiobankSampleAttributeMetaData.COLLECTION));

		Iterable<Entity> entities = entity.getEntities(BiobankSampleAttributeMetaData.TAG_GROUPS);
		List<IdentifiableTagGroup> tagGroups = StreamSupport.stream(entities.spliterator(), false)
				.map(this::entityToIdentifiableTagGroup).collect(toList());

		return BiobankSampleAttribute.create(identifier, name, label, description, biobankSampleCollection, tagGroups);
	}

	private Entity biobankSampleAttributeToEntity(BiobankSampleAttribute biobankSampleAttribute)
	{
		Iterable<Entity> tagGroupEntities = entityManager.getReferences(tagGroupMetaData,
				biobankSampleAttribute.getTagGroups().stream().map(IdentifiableTagGroup::getIdentifier)
						.collect(toList()));

		Entity biobankSampleCollectionEntity = entityManager
				.getReference(biobankSampleCollectionMetaData, biobankSampleAttribute.getCollection().getName());

		Entity entity = new DynamicEntity(biobankSampleAttributeMetaData);
		entity.set(BiobankSampleAttributeMetaData.IDENTIFIER, biobankSampleAttribute.getIdentifier());
		entity.set(BiobankSampleAttributeMetaData.NAME, biobankSampleAttribute.getName());
		entity.set(BiobankSampleAttributeMetaData.LABEL, biobankSampleAttribute.getLabel());
		entity.set(BiobankSampleAttributeMetaData.DESCRIPTION, biobankSampleAttribute.getDescription());
		entity.set(BiobankSampleAttributeMetaData.COLLECTION, biobankSampleCollectionEntity);
		entity.set(BiobankSampleAttributeMetaData.TAG_GROUPS, tagGroupEntities);

		return entity;
	}

	private Entity identifiableTagGroupToEntity(IdentifiableTagGroup tagGroup)
	{
		Iterable<Entity> ontologyTermEntities = entityManager.getReferences(ontologyTermMetaData,
				tagGroup.getOntologyTerms().stream().map(OntologyTerm::getId).distinct().collect(toList()));

		Iterable<Entity> semanticTypeEntities = entityManager.getReferences(semanticTypeMetaData,
				tagGroup.getSemanticTypes().stream().map(SemanticType::getIdentifier).distinct().collect(toList()));

		Entity entity = new DynamicEntity(tagGroupMetaData);
		entity.set(TagGroupMetaData.IDENTIFIER, tagGroup.getIdentifier());
		entity.set(TagGroupMetaData.ONTOLOGY_TERMS, ontologyTermEntities);
		entity.set(TagGroupMetaData.SEMANTIC_TYPES, semanticTypeEntities);
		entity.set(TagGroupMetaData.MATCHED_WORDS, tagGroup.getMatchedWords());
		entity.set(TagGroupMetaData.NGRAM_SCORE, (double) tagGroup.getScore());
		return entity;
	}

	private IdentifiableTagGroup entityToIdentifiableTagGroup(Entity entity)
	{
		String identifier = entity.getString(TagGroupMetaData.IDENTIFIER);
		String matchedWords = entity.getString(TagGroupMetaData.MATCHED_WORDS);
		Double ngramScore = entity.getDouble(TagGroupMetaData.NGRAM_SCORE);

		List<OntologyTerm> ontologyTerms = stream(entity.getEntities(TagGroupMetaData.ONTOLOGY_TERMS).spliterator(),
				false).map(OntologyTermEntity::new).map(OntologyTermRepository::toOntologyTerm).collect(toList());

		List<SemanticType> semanticTypes = stream(entity.getEntities(TagGroupMetaData.SEMANTIC_TYPES).spliterator(),
				false).map(OntologyTermRepository::entityToSemanticType).collect(toList());

		return IdentifiableTagGroup
				.create(identifier, ontologyTerms, semanticTypes, matchedWords, ngramScore.floatValue());
	}

	private AttributeMappingCandidate entityToAttributeMappingCandidate(Entity entity)
	{
		String identifier = entity.getString(AttributeMappingCandidateMetaData.IDENTIFIER);
		BiobankUniverse biobankUniverse = entityToBiobankUniverse(
				entity.getEntity(AttributeMappingCandidateMetaData.BIOBANK_UNIVERSE));
		BiobankSampleAttribute target = entityToBiobankSampleAttribute(
				entity.getEntity(AttributeMappingCandidateMetaData.TARGET));
		BiobankSampleAttribute source = entityToBiobankSampleAttribute(
				entity.getEntity(AttributeMappingCandidateMetaData.SOURCE));
		MatchingExplanation explanation = entityToMappingExplanation(
				entity.getEntity(AttributeMappingCandidateMetaData.EXPLANATION));

		List<AttributeMappingDecision> decisions = StreamSupport
				.stream(entity.getEntities(AttributeMappingCandidateMetaData.DECISIONS).spliterator(), false)
				.map(this::entityToAttributeMappingDecision)
				.filter(decistion -> decistion.getOwner().equals(userAcountService.getCurrentUser().getUsername()))
				.collect(toList());

		return AttributeMappingCandidate.create(identifier, biobankUniverse, target, source, explanation, decisions);
	}

	private Entity attributeMappingCandidateToEntity(AttributeMappingCandidate attributeMappingCandidate)
	{
		String identifier = attributeMappingCandidate.getIdentifier();
		BiobankUniverse biobankUniverse = attributeMappingCandidate.getBiobankUniverse();
		BiobankSampleAttribute target = attributeMappingCandidate.getTarget();
		BiobankSampleAttribute source = attributeMappingCandidate.getSource();
		MatchingExplanation explanation = attributeMappingCandidate.getExplanation();

		Entity biobankUniverseEntity = entityManager
				.getReference(biobankUniverseMetaData, biobankUniverse.getIdentifier());

		Entity targetEntity = entityManager.getReference(biobankSampleAttributeMetaData, target.getIdentifier());

		Entity sourceEntity = entityManager.getReference(biobankSampleAttributeMetaData, source.getIdentifier());

		Entity matchingExplanationEntity = entityManager
				.getReference(matchingExplanationMetaData, explanation.getIdentifier());

		Iterable<Entity> decisionEntities = entityManager.getReferences(attributeMappingDecisionMetaData,
				attributeMappingCandidate.getDecisions().stream().map(AttributeMappingDecision::getIdentifier)
						.collect(toList()));

		Entity entity = new DynamicEntity(attributeMappingCandidateMetaData);
		entity.set(AttributeMappingCandidateMetaData.IDENTIFIER, identifier);
		entity.set(AttributeMappingCandidateMetaData.BIOBANK_UNIVERSE, biobankUniverseEntity);
		entity.set(AttributeMappingCandidateMetaData.TARGET, targetEntity);
		entity.set(AttributeMappingCandidateMetaData.SOURCE, sourceEntity);
		entity.set(AttributeMappingCandidateMetaData.EXPLANATION, matchingExplanationEntity);
		entity.set(AttributeMappingCandidateMetaData.DECISIONS, decisionEntities);

		return entity;
	}

	private Entity mappingExplanationToEntity(MatchingExplanation mappingExplanation)
	{
		String identifier = mappingExplanation.getIdentifier();
		String queryString = mappingExplanation.getQueryString();
		String matchedWords = mappingExplanation.getMatchedWords();
		double ngramScore = mappingExplanation.getNgramScore();

		Iterable<Entity> ontologyTermEntities = entityManager.getReferences(ontologyTermMetaData,
				mappingExplanation.getOntologyTerms().stream().map(OntologyTerm::getId).collect(toList()));

		Entity entity = new DynamicEntity(matchingExplanationMetaData);
		entity.set(MatchingExplanationMetaData.IDENTIFIER, identifier);
		entity.set(MatchingExplanationMetaData.MATCHED_QUERY_STRING, queryString);
		entity.set(MatchingExplanationMetaData.MATCHED_WORDS, matchedWords);
		entity.set(MatchingExplanationMetaData.ONTOLOGY_TERMS, ontologyTermEntities);
		entity.set(MatchingExplanationMetaData.N_GRAM_SCORE, ngramScore);

		return entity;
	}

	private MatchingExplanation entityToMappingExplanation(Entity mappingExplanationEntity)
	{
		String identifier = mappingExplanationEntity.getString(MatchingExplanationMetaData.IDENTIFIER);
		String queryString = mappingExplanationEntity.getString(MatchingExplanationMetaData.MATCHED_QUERY_STRING);
		String matchedWords = mappingExplanationEntity.getString(MatchingExplanationMetaData.MATCHED_WORDS);
		Double ngramScore = mappingExplanationEntity.getDouble(MatchingExplanationMetaData.N_GRAM_SCORE);

		List<OntologyTerm> ontologyTerms = new ArrayList<>();
		Iterable<Entity> ontologyTermEntities = mappingExplanationEntity
				.getEntities(MatchingExplanationMetaData.ONTOLOGY_TERMS);
		if (ontologyTermEntities != null)
		{
			List<OntologyTerm> collect = stream(ontologyTermEntities.spliterator(), false).map(OntologyTermEntity::new)
					.map(OntologyTermRepository::toOntologyTerm).collect(toList());
			ontologyTerms.addAll(collect);
		}

		return MatchingExplanation.create(identifier, ontologyTerms, queryString, matchedWords, ngramScore);
	}

	private AttributeMappingDecision entityToAttributeMappingDecision(Entity entity)
	{
		String identifier = entity.getString(AttributeMappingDecisionMetaData.IDENTIFIER);
		String owner = entity.getString(AttributeMappingDecisionMetaData.OWNER);
		DecisionOptions decision = DecisionOptions.valueOf(entity.getString(AttributeMappingDecisionMetaData.DECISION));
		String comment = entity.getString(AttributeMappingDecisionMetaData.COMMENT);
		return AttributeMappingDecision.create(identifier, decision, comment, owner);
	}

	private Entity attributeMappingDecisionToEntity(AttributeMappingDecision attributeMappingDecision)
	{
		String identifier = attributeMappingDecision.getIdentifier();
		String comment = attributeMappingDecision.getComment();
		DecisionOptions decision = attributeMappingDecision.getDecision();
		String owner = attributeMappingDecision.getOwner();

		Entity entity = new DynamicEntity(attributeMappingDecisionMetaData);
		entity.set(AttributeMappingDecisionMetaData.IDENTIFIER, identifier);
		entity.set(AttributeMappingDecisionMetaData.OWNER, owner);
		entity.set(AttributeMappingDecisionMetaData.DECISION, decision);
		entity.set(AttributeMappingDecisionMetaData.COMMENT, comment);

		return entity;
	}
}