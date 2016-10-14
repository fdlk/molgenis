package org.molgenis.data.discovery.filters;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.model.matching.MatchingExplanation;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.utils.Stemmer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.splitIntoUniqueTerms;
import static org.molgenis.ontology.utils.NGramDistanceAlgorithm.STOPWORDSLIST;
import static org.molgenis.ontology.utils.Stemmer.splitAndStem;

/**
 * Created by chaopang on 13/10/16.
 */
public class MatchExplanationPostFilter implements PostFilter
{
	@Override
	public boolean filter(AttributeMappingCandidate attributeMappingCandidate, SearchParam searchParam,
			BiobankUniverse biobankUniverse)
	{
		MatchingExplanation explanation = attributeMappingCandidate.getExplanation();

		if (explanation.getNgramScore() > searchParam.getMatchParam().getHighQualityThreshold())
		{
			return true;
		}

		List<OntologyTerm> ontologyTerms = explanation.getOntologyTerms();

		List<SemanticType> conceptFilter = biobankUniverse.getKeyConcepts();

		Multimap<String, OntologyTerm> ontologyTermWithSameSynonyms = LinkedHashMultimap.create();

		Set<String> stemmedMatchedWords = splitAndStem(explanation.getMatchedSourceWords());

		for (OntologyTerm ontologyTerm : ontologyTerms)
		{
			Optional<String> findFirst = ontologyTerm.getSynonyms().stream().map(Stemmer::splitAndStem)
					.filter(stemmedSynonymWords -> stemmedMatchedWords.containsAll(stemmedSynonymWords))
					.map(words -> words.stream().sorted().collect(joining(" "))).findFirst();

			if (findFirst.isPresent())
			{
				ontologyTermWithSameSynonyms.put(findFirst.get(), ontologyTerm);
			}
		}

		List<Collection<OntologyTerm>> collect = ontologyTermWithSameSynonyms.asMap().values().stream()
				.filter(ots -> areOntologyTermsImportant(conceptFilter, ots)).collect(toList());

		String matchedWords = splitIntoUniqueTerms(explanation.getMatchedSourceWords()).stream()
				.map(String::toLowerCase).filter(word -> !STOPWORDSLIST.contains(word)).collect(joining(" "));

		// TODO: for testing purpose
		return !collect.isEmpty() && matchedWords.length() >= 3;
		// return true;
	}

	private boolean areOntologyTermsImportant(List<SemanticType> conceptFilter, Collection<OntologyTerm> ots)
	{
		// Good ontology terms are defined as the ontology terms whose semantic types are global concepts and not in
		// the conceptFilter
		long countOfGoodOntologyTerms = ots.stream()
				.filter(ot -> ot.getSemanticTypes().isEmpty() || ot.getSemanticTypes().stream().allMatch(
						semanticType -> semanticType.isGlobalKeyConcept() && !conceptFilter.contains(semanticType)))
				.count();

		// Bad ontology terms are defined as the ontology terms whose any of the semantic types are not global
		// concepts or in the conceptFilter
		long countOfBadOntologyTerms = ots.stream()
				.filter(ot -> !ot.getSemanticTypes().isEmpty() && ot.getSemanticTypes().stream().anyMatch(
						semanticType -> !semanticType.isGlobalKeyConcept() || conceptFilter.contains(semanticType)))
				.count();

		// If there are more good ontology terms than the bad ones, we keep the ontology terms
		return countOfGoodOntologyTerms >= countOfBadOntologyTerms;
	}
}
