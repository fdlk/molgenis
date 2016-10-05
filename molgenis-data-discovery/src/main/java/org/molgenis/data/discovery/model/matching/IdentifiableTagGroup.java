package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.model.SemanticType;

import java.util.List;

/**
 * {@link OntologyTerm}s that got matched to an attribute.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_IdentifiableTagGroup.class)
public abstract class IdentifiableTagGroup implements Comparable<IdentifiableTagGroup>
{
	public static IdentifiableTagGroup create(String identifier, List<OntologyTermImpl> ontologyTermImpls,
			List<SemanticType> semanticTypes, String matchedWords, float score)
	{
		return new AutoValue_IdentifiableTagGroup(identifier, ontologyTermImpls, semanticTypes, matchedWords,
				Math.round(score * 100000));
	}

	public abstract String getIdentifier();

	/**
	 * The ontology terms that got matched to the attribute, combined into one {@link OntologyTermImpl}
	 */
	public abstract List<OntologyTermImpl> getOntologyTermImpls();

	public abstract List<SemanticType> getSemanticTypes();

	public abstract String getMatchedWords();

	public abstract int getScoreInt();

	public float getScore()
	{
		return getScoreInt() / 100000.0f;
	}

	@Override
	public int compareTo(IdentifiableTagGroup o)
	{
		return Double.compare(getScore(), o.getScore());
	}
}
