package org.molgenis.data.discovery.repo;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.biobank.BiobankUniverseMemberVector;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.model.matching.AttributeMappingDecision;
import org.molgenis.data.discovery.model.matching.MatchingExplanation;
import org.molgenis.data.discovery.repo.impl.BiobankUniverseRepositoryImpl.DecisionAction;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.SemanticType;

import java.util.List;
import java.util.stream.Stream;

/**
 * This repository communicates with the {@link DataService} to manipulate data related to {@link BiobankUniverse}
 *
 * @author chaopang
 */
public interface BiobankUniverseRepository
{
	void addKeyConcepts(BiobankUniverse biobankUniverse, List<SemanticType> semanticTypes);

	/**
	 * Get all {@link BiobankUniverse}s from the database
	 *
	 * @return a list of {@link BiobankUniverse}
	 */
	List<BiobankUniverse> getAllUniverses();

	/**
	 * Get a specific {@link BiobankUniverse} by the identifier from the database
	 *
	 * @param identifier
	 * @return a {@link BiobankUniverse}
	 */
	BiobankUniverse getUniverse(String identifier);

	/**
	 * Add a new {@link BiobankUniverse} with initial members {@link BiobankSampleCollection}s
	 *
	 * @param biobankUniverse
	 */
	void addBiobankUniverse(BiobankUniverse biobankUniverse);

	/**
	 * Cascading delete the {@link BiobankUniverse} and its related entities including {@link AttributeMappingCandidate}
	 * s, {@link AttributeMappingDecision}s and {@link MatchingExplanation}s
	 *
	 * @param universe
	 */
	void removeBiobankUniverse(BiobankUniverse universe);

	/**
	 * Add new members {@link BiobankSampleCollection}s to the existing {@link BiobankUniverse}
	 *
	 * @param biobankUniverse
	 * @param biobankSampleCollections
	 */
	void addUniverseMembers(BiobankUniverse biobankUniverse, List<BiobankSampleCollection> biobankSampleCollections);

	/**
	 * Remove the members {@link BiobankSampleCollection}s from the existing {@link BiobankUniverse}
	 *
	 * @param biobankUniverse
	 * @param biobankSampleCollections
	 */
	void removeUniverseMembers(BiobankUniverse biobankUniverse, List<BiobankSampleCollection> biobankSampleCollections);

	/**
	 * Add a new {@link BiobankSampleCollection} to the database
	 *
	 * @param biobankSampleCollection
	 */
	void addBiobankSampleCollection(BiobankSampleCollection biobankSampleCollection);

	/**
	 * Cascading delete the given {@link BiobankSampleCollection} and its related entities including
	 * {@link BiobankSampleAttribute}s, {@link AttributeMappingCandidate}s, {@link AttributeMappingDecision}s and
	 * {@link MatchingExplanation}s
	 *
	 * @param biobankSampleCollection
	 */
	void removeBiobankSampleCollection(BiobankSampleCollection biobankSampleCollection);

	/**
	 * Get all {@link BiobankSampleCollection}s in the database
	 *
	 * @return a list of {@link BiobankSampleCollection}s
	 */
	List<BiobankSampleCollection> getAllBiobankSampleCollections();

	/**
	 * Get a {@link BiobankSampleCollection} by the name
	 *
	 * @param name
	 * @return a {@link BiobankSampleCollection}
	 */
	BiobankSampleCollection getBiobankSampleCollection(String name);

	/**
	 * Get all {@link BiobankSampleAttribute}s from the given {@link BiobankSampleCollection}
	 *
	 * @param biobankSampleCollection
	 * @return a {@link List} of {@link BiobankSampleAttribute}s
	 */
	List<BiobankSampleAttribute> getBiobankSampleAttributes(BiobankSampleCollection biobankSampleCollection);

	/**
	 * Get the {@link BiobankSampleAttribute} based on the given identifier
	 *
	 * @param attributeIdentifier
	 * @return
	 */
	BiobankSampleAttribute getBiobankSampleAttributes(String attributeIdentifier);

	/**
	 * Count the number of {@link BiobankSampleAttribute}s associated with the {@link BiobankSampleCollection}
	 *
	 * @param biobankSampleCollection
	 * @return
	 */
	int countBiobankSampleAttributes(BiobankSampleCollection biobankSampleCollection);

	/**
	 * Add a list of {@link BiobankSampleAttribute}s to the database
	 *
	 * @param biobankSampleAttributes
	 */
	void addBiobankSampleAttributes(Stream<BiobankSampleAttribute> biobankSampleAttributes);

	/**
	 * Cascading delete the given list of {@link BiobankSampleAttribute}s and their related entities including
	 * {@link AttributeMappingCandidate}s, {@link AttributeMappingDecision}s and {@link MatchingExplanation}s
	 *
	 * @param biobankSampleAttributes
	 */
	void removeBiobankSampleAttributes(List<BiobankSampleAttribute> biobankSampleAttributes);

	/**
	 * Retrieve a list of {@link BiobankSampleAttribute}s based on the given {@link Query}
	 *
	 * @param query
	 * @return a list of {@link BiobankSampleAttribute}s
	 */
	List<BiobankSampleAttribute> getBiobankSampleAttributes(Query<Entity> query);

	/**
	 * Store all {@link TagGroup}s and update all {@link BiobankSampleAttribute}s
	 *
	 * @param biobankSampleAttributes
	 */
	void addTagGroupsForAttributes(List<BiobankSampleAttribute> biobankSampleAttributes);

	/**
	 * Delete all {@link TagGroup}s that are associated with the given {@link BiobankSampleAttribute}s
	 *
	 * @param biobankSampleAttributes
	 */
	void removeTagGroupsForAttributes(List<BiobankSampleAttribute> biobankSampleAttributes);

	/**
	 * Add a list of {@link AttributeMappingCandidate}s to the database
	 *
	 * @param attributeMappingCandidates
	 */
	void addAttributeMappingCandidates(List<AttributeMappingCandidate> attributeMappingCandidates);

	List<AttributeMappingCandidate> getAttributeMappingCandidates(BiobankUniverse biobankUniverse,
			BiobankSampleCollection target);

	/**
	 * Aggregate the number of candidate matches based on the target {@link BiobankSampleCollection} and source {@link BiobankSampleCollection}
	 *
	 * @param biobankUniverse
	 * @return
	 */
	AggregateResult aggregateCandidateMatches(BiobankUniverse biobankUniverse);

	/**
	 * Get all {@link AttributeMappingCandidate}s generated in the source {@link BiobankSampleCollection} in the current {@link BiobankUniverse} for the target {@link BiobankSampleAttribute}
	 *
	 * @param biobankUniverse
	 * @param targetAttrinute
	 * @param targetSampleCollection
	 * @param sourceSampleCollection
	 * @return
	 */
	List<AttributeMappingCandidate> getAttributeMappingCandidates(BiobankUniverse biobankUniverse,
			BiobankSampleAttribute targetAttrinute, BiobankSampleCollection targetSampleCollection,
			BiobankSampleCollection sourceSampleCollection);

	/**
	 * Update the {@link AttributeMappingDecision}s in the {@link AttributeMappingCandidate}s in the database for the given {@link MolgenisUser}
	 *
	 * @param attributeMappingCandidatesToUpdate
	 * @param molgenisUser                       the user who makes the curation decisions
	 * @param decisionAction                     Add or Delete
	 */
	void updateAttributeMappingCandidateDecisions(List<AttributeMappingCandidate> attributeMappingCandidatesToUpdate,
			MolgenisUser molgenisUser, DecisionAction decisionAction);

	/**
	 * Cascading delete the given list of {@link Entity}s and their related entities including
	 * {@link AttributeMappingDecision}s and {@link MatchingExplanation}s
	 *
	 * @param attributeMappingCandidates
	 */
	void removeAttributeMappingCandidates(List<Entity> attributeMappingCandidates);

	List<String> getBiobankSampleAttributeIdentifiers(BiobankSampleCollection biobankSampleCollection);

	boolean isBiobankSampleCollectionTagged(BiobankSampleCollection biobankSampleCollection);

	/**
	 * Update {@link BiobankUniverse} with the new list of {@link BiobankUniverseMemberVector}s
	 *
	 * @param biobankUniverse
	 * @param biobankUniverseMemberVectors
	 */
	void updateBiobankUniverseMemberVectors(BiobankUniverse biobankUniverse,
			List<BiobankUniverseMemberVector> biobankUniverseMemberVectors);

	/**
	 * Add a list of {@link AttributeMappingDecision}s
	 *
	 * @param attributeMappingDecisions
	 */
	void addAttributeMappingDecisions(List<AttributeMappingDecision> attributeMappingDecisions);
}