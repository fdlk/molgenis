package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

/**
 * Created by chaopang on 25/10/16.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeMatchingCell.class)
public abstract class AttributeMatchingCell
{
	public abstract List<AttributeMappingCandidate> getCandidates();

	public abstract boolean isDecided();

	public abstract boolean isMatched();

	public static AttributeMatchingCell create(List<AttributeMappingCandidate> candidates, boolean decided,
			boolean matched)
	{
		return new AutoValue_AttributeMatchingCell(candidates, decided, matched);
	}
}
