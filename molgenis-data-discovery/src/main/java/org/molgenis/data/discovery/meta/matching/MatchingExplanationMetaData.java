package org.molgenis.data.discovery.meta.matching;

import org.molgenis.data.discovery.meta.BiobankUniversePackage;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;
import static org.molgenis.data.discovery.meta.BiobankUniversePackage.PACKAGE_UNIVERSE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class MatchingExplanationMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "MatchingExplanation";
	public static final String MATCHING_EXPLANATION = PACKAGE_UNIVERSE + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String ONTOLOGY_TERMS = "ontologyTerms";
	public static final String MATCHED_QUERY_STRING = "matchedQueryString";
	public static final String MATCHED_WORDS = "matchedWords";
	public static final String N_GRAM_SCORE = "ngramScore";

	private final BiobankUniversePackage biobankUniversePackage;
	private final OntologyTermMetaData ontologyTermMetaData;

	public MatchingExplanationMetaData(BiobankUniversePackage biobankUniversePackage,
			OntologyTermMetaData ontologyTermMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_UNIVERSE);
		this.biobankUniversePackage = requireNonNull(biobankUniversePackage);
		this.ontologyTermMetaData = requireNonNull(ontologyTermMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("Matching explanation");
		setPackage(biobankUniversePackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(ONTOLOGY_TERMS).setDataType(MREF).setRefEntity(ontologyTermMetaData);
		addAttribute(MATCHED_QUERY_STRING);
		addAttribute(MATCHED_WORDS);
		addAttribute(N_GRAM_SCORE).setDataType(DECIMAL);

	}
}
