package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MatchingExplanation.class)
public abstract class MatchingExplanation
{
	public abstract String getIdentifier();

	public abstract List<OntologyTerm> getOntologyTerms();

	public abstract String getQueryString();

	public abstract String getMatchedTargetWords();

	public abstract String getMatchedSourceWords();

	public abstract double getNgramScore();

	public static MatchingExplanation create(String identifier, List<OntologyTerm> ontologyTerms, String queryString,
			String matchedTargetWords, String matchedSourceWords, double ngramScore)
	{
		return new AutoValue_MatchingExplanation(identifier, ontologyTerms, queryString, matchedTargetWords,
				matchedSourceWords, ngramScore);
	}

	public String getMatchedWords()
	{
		return getMatchedTargetWords() + ' ' + getMatchedSourceWords();
	}
}
