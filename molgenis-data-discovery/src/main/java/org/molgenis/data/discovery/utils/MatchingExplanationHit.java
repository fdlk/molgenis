package org.molgenis.data.discovery.utils;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import static java.lang.Double.compare;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MatchingExplanationHit.class)
public abstract class MatchingExplanationHit implements Comparable<MatchingExplanationHit>
{
	public abstract String getMatchedWords();

	public abstract float getVsmScore();

	public abstract float getNgramScore();

	public static MatchingExplanationHit create(String matchedWords, float vsmScore, float ngramScore)
	{
		return new AutoValue_MatchingExplanationHit(matchedWords, vsmScore, ngramScore);
	}

	public int compareTo(MatchingExplanationHit o)
	{
		return compare(o.getVsmScore(), getVsmScore());
	}

	public float getScore()
	{
		return getVsmScore();
	}
}
