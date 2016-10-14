package org.molgenis.data.semanticsearch.explain.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermHit;
import org.molgenis.data.semanticsearch.explain.bean.QueryExpansionSolution;
import org.molgenis.data.semanticsearch.explain.criteria.MatchingCriterion;
import org.molgenis.data.semanticsearch.explain.criteria.impl.StrictMatchingCriterion;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.utils.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.*;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;
import static org.molgenis.ontology.utils.NGramDistanceAlgorithm.stringMatching;
import static org.molgenis.ontology.utils.Stemmer.splitAndStem;

/**
 * This explain service only explains the attributes generated by {@link org.molgenis.data.semanticsearch.service.SemanticSearchService}
 */
public class ExplainMappingServiceImpl implements ExplainMappingService
{
	private final OntologyService ontologyService;
	private final TagGroupGenerator tagGroupGenerator;
	private final Joiner termJoiner = Joiner.on(' ');

	final static MatchingCriterion STRICT_MATCHING_CRITERION = new StrictMatchingCriterion();

	private final static ExplainedMatchCandidate<String> EMPTY_EXPLANATION = ExplainedMatchCandidate.create(EMPTY);

	private static final Logger LOG = LoggerFactory.getLogger(ExplainMappingServiceImpl.class);

	@Autowired
	public ExplainMappingServiceImpl(OntologyService ontologyService, TagGroupGenerator tagGroupGenerator)
	{
		this.ontologyService = requireNonNull(ontologyService);
		this.tagGroupGenerator = requireNonNull(tagGroupGenerator);
	}

	@Override
	public ExplainedMatchCandidate<String> explainMapping(SearchParam searchParam, String matchedResult)
	{
		Set<String> lexicalQueries = searchParam.getLexicalQueries();

		Set<String> matchedSourceWords = splitRemoveStopWords(matchedResult);

		// The scope contains all matched ontology terms from the target plus their children up to the default expansion level
		Set<OntologyTerm> ontologyTermScope = searchParam.getTagGroups().stream()
				.flatMap(tagGroup -> tagGroup.getOntologyTerms().stream()).distinct()
				.flatMap(ot -> stream(ontologyService.getChildren(ot, DEFAULT_EXPANSION_LEVEL).spliterator(), false))
				.collect(toSet());

		LOG.debug("OntologyTerms {}", ontologyTermScope);

		// throw the entire scope at the ontologyService and find all ontology terms within the scope that loosely match
		// at least one of the source words
		List<OntologyTerm> relevantOntologyTerms = ontologyService
				.findOntologyTerms(ontologyService.getAllOntologyIds(), matchedSourceWords, ontologyTermScope.size(),
						ontologyTermScope);

		// Now filter out the OntologyTerms that strictly match the source words and create a single tag group for each
		// of the terms
		List<OntologyTermHit> tagGroups = tagGroupGenerator
				.applyTagMatchingCriterion(relevantOntologyTerms, matchedSourceWords, STRICT_MATCHING_CRITERION);

		// create a list of combinations of the ontology terms we found, all with the same, highest, score
		List<TagGroup> matchedSourceTagGroups = tagGroupGenerator.combineTagGroups(matchedSourceWords, tagGroups);

		LOG.debug("Candidates: {}", matchedSourceTagGroups);

		// create a list of query expansion soloutions and get the best solution after sorting
		QueryExpansionSolution queryExpansionSolution = searchParam.getTagGroups().stream()
				.map(tagGroup -> getQueryExpansionSolution(tagGroup, matchedSourceTagGroups.get(0))).sorted()
				.findFirst().orElse(null);

		Optional<Hit<ExplainedMatchCandidate<String>>> max = stream(lexicalQueries.spliterator(), false)
				.map(lexicalQuery -> computeScoreForMatchedSource(queryExpansionSolution, lexicalQuery, matchedResult))
				.max(naturalOrder());

		return max.map(Hit::getResult).orElse(EMPTY_EXPLANATION);
	}

	QueryExpansionSolution getQueryExpansionSolution(TagGroup targetTagGroup, TagGroup sourceTagGroup)
	{
		Map<OntologyTerm, OntologyTerm> matchedOntologyTerms = new LinkedHashMap<>();

		Multimap<OntologyTerm, OntologyTerm> children = LinkedHashMultimap.create();

		for (OntologyTerm atomicOntologyTerm : targetTagGroup.getOntologyTerms())
		{
			children.put(atomicOntologyTerm, atomicOntologyTerm);
			children.putAll(atomicOntologyTerm,
					ontologyService.getChildren(atomicOntologyTerm, DEFAULT_EXPANSION_LEVEL));
		}

		for (OntologyTerm sourceOntologyTerm : sourceTagGroup.getOntologyTerms())
		{
			children.asMap().entrySet().stream().filter(entry -> entry.getValue().contains(sourceOntologyTerm))
					.forEach(entry -> matchedOntologyTerms.put(entry.getKey(), sourceOntologyTerm));
		}

		// If for each of the root ontology terms, the term itself or one of its children get matched,
		// then the quality is high
		boolean highQuality = matchedOntologyTerms.size() == children.asMap().keySet().size();
		float percentage = (float) matchedOntologyTerms.size() / children.asMap().keySet().size();
		return QueryExpansionSolution.create(matchedOntologyTerms, percentage, highQuality);
	}

	/**
	 * Computes the similarity score for the matched candidate using either ontology terms and the target
	 * label. And figures out how the words in the source attribute are related to the target label or target tag groups.
	 * Whichever gives higher similarity score gets selected as the final explanation.
	 *
	 * @param queryExpansionSolution the single OntologyTerm-based QueryExpansionSolution
	 * @param targetQueryTerm        label of target attribute
	 * @param match                  label of source attribute
	 * @return ExplainedMatchCandidate, used to paint the source attribute label
	 */
	Hit<ExplainedMatchCandidate<String>> computeScoreForMatchedSource(QueryExpansionSolution queryExpansionSolution,
			String targetQueryTerm, String match)
	{
		// Explain the match using ontology terms
		List<ExplainedQueryString> explainedUsingOntologyTerms = new ArrayList<>();

		for (Entry<OntologyTerm, OntologyTerm> entry : queryExpansionSolution.getMatchOntologyTerms().entrySet())
		{
			OntologyTerm targetOntologyTerm = entry.getKey();
			OntologyTerm sourceOntologyTerm = entry.getValue();

			String bestMatchingSynonym = findBestMatchingSynonym(match, sourceOntologyTerm);
			String joinedMatchedWords = termJoiner.join(findMatchedWords(match, bestMatchingSynonym));
			float score = Math.round(stringMatching(joinedMatchedWords, match) * 10) / 10f;
			explainedUsingOntologyTerms.add(ExplainedQueryString
					.create(joinedMatchedWords, bestMatchingSynonym, targetOntologyTerm.getLabel(), score));
		}

		// Explain the match using the target label
		List<ExplainedQueryString> explainedUsingTargetLabel = new ArrayList<>();

		float score = (float) (Math.round(stringMatching(targetQueryTerm, match) * 10) / 10f);
		explainedUsingTargetLabel.add(ExplainedQueryString
				.create(termJoiner.join(findMatchedWords(targetQueryTerm, match)), targetQueryTerm, EMPTY, score));

		// Choose to be explained whether by ontology terms or target label depending one which one produces a higher
		// similarity score
		Hit<ExplainedMatchCandidate<String>> explainedCandidateUsingOntologyTerms = createExplainedCandidate(match,
				explainedUsingOntologyTerms, queryExpansionSolution.isHighQuality());
		Hit<ExplainedMatchCandidate<String>> explainedCandidateUsingTargetLabel = createExplainedCandidate(match,
				explainedUsingTargetLabel, false);

		return explainedCandidateUsingOntologyTerms.getScore() >= explainedCandidateUsingTargetLabel
				.getScore() ? explainedCandidateUsingOntologyTerms : explainedCandidateUsingTargetLabel;
	}

	private Hit<ExplainedMatchCandidate<String>> createExplainedCandidate(String match,
			List<ExplainedQueryString> explainedUsingOntologyTerms, boolean isHighQuality)
	{
		String combinedQuery = termJoiner.join(splitIntoUniqueTerms(
				explainedUsingOntologyTerms.stream().map(ExplainedQueryString::getQueryString)
						.collect(Collectors.joining(" "))));

		float score = Math.round(stringMatching(combinedQuery, match) * 10) / 10f;

		return Hit.create(ExplainedMatchCandidate.create(match, explainedUsingOntologyTerms, isHighQuality), score);
	}

	/**
	 * Finds matching words between two {@link String}s. The match is made based on the stemmed words, but the resulting
	 * matched words are the unstemmed words from the second string.
	 *
	 * @param target the target string
	 * @param source the source string
	 * @return unstemmed words from target that are matched by words in source
	 */
	private Set<String> findMatchedWords(String target, String source)
	{
		Set<String> result = new LinkedHashSet<>();
		Set<String> stemmedSourceWords = splitAndStem(source);
		for (String unstemmedTargetWord : splitIntoUniqueTerms(target))
		{
			String stemmedSourceWord = Stemmer.stem(unstemmedTargetWord);
			if (stemmedSourceWords.contains(stemmedSourceWord))
			{
				result.add(unstemmedTargetWord);
			}
		}
		return result;
	}

	/**
	 * Finds the best matching synonym from {@link OntologyTerm}s for the given target query
	 *
	 * @param attributeLabel the attribute label to match
	 * @param ontologyTerm   the OntologyTerm whose label and synonyms are candidates for the match
	 * @return the best matching label or synonym
	 */
	private String findBestMatchingSynonym(String attributeLabel, OntologyTerm ontologyTerm)
	{
		Hit<String> hit = getLowerCaseTerms(ontologyTerm).stream()
				.map(synonym -> Hit.create(synonym, (float) stringMatching(attributeLabel, synonym)))
				.sorted(reverseOrder()).findFirst().get();

		return hit.getResult();
	}
}