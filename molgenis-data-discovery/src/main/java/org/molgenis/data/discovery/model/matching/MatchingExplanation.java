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

	public abstract String getMatchedWords();

	public abstract double getNgramScore();

	public static MatchingExplanation create(String identifier, List<OntologyTerm> ontologyTerms, String queryString,
			String matchedWords, double ngramScore)
	{
		return new AutoValue_MatchingExplanation(identifier, ontologyTerms, queryString, matchedWords, ngramScore);
	}
}
