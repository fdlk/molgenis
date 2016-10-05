package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.molgenis.data.discovery.meta.matching.AttributeMappingDecisionMetaData.DecisionOptions;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeMappingDecision.class)
public abstract class AttributeMappingDecision
{
	public abstract String getIdentifier();

	public abstract DecisionOptions getDecision();

	@Nullable
	public abstract String getComment();

	public abstract String getOwner();

	public static AttributeMappingDecision create(String identifier, DecisionOptions decision, String comment,
			String owner)
	{
		return new AutoValue_AttributeMappingDecision(identifier, decision, comment, owner);
	}
}
