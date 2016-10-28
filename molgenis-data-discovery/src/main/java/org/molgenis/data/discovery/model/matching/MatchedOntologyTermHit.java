package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermHit;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MatchedOntologyTermHit.class)
public abstract class MatchedOntologyTermHit implements Comparable<MatchedOntologyTermHit>
{
	public abstract OntologyTermHit getTarget();

	public abstract OntologyTermHit getSource();

	public abstract Double getSimilarity();

	public static MatchedOntologyTermHit create(OntologyTermHit target, OntologyTermHit source, Double similarity)
	{
		return new AutoValue_MatchedOntologyTermHit(target, source, similarity);
	}

	public int compareTo(MatchedOntologyTermHit o)
	{
		return Double.compare(o.getSimilarity(), getSimilarity());
	}

	public String getCombinedMatchedWords()
	{
		return getTarget().getMatchedWords() + " " + getSource().getMatchedWords();
	}
}
