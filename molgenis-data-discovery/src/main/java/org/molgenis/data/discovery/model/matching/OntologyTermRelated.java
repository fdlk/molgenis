package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTermImpl;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTermRelated.class)
public abstract class OntologyTermRelated
{
	public abstract OntologyTermImpl getTarget();

	public abstract OntologyTermImpl getSource();

	public abstract int getStopLevel();

	public static OntologyTermRelated create(OntologyTermImpl target, OntologyTermImpl source, int stopLevel)
	{
		return new AutoValue_OntologyTermRelated(target, source, stopLevel);
	}
}