package org.molgenis.ontology.sorta.bean;

import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTermImpl;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SortaHit.class)
public abstract class SortaHit implements Comparable<SortaHit>
{
	public abstract OntologyTermImpl getOntologyTermImpl();

	public abstract double getScore();

	public abstract double getWeightedScore();

	public static SortaHit create(OntologyTermImpl ontologyTermImpl, double score, double weightedScore)
	{
		return new AutoValue_SortaHit(ontologyTermImpl, score, weightedScore);
	}

	@Override
	public int compareTo(SortaHit other)
	{
		return Double.compare(other.getWeightedScore(), getWeightedScore());
	}
}
