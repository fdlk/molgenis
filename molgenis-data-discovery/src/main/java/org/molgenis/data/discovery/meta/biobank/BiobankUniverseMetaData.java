package org.molgenis.data.discovery.meta.biobank;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.discovery.meta.BiobankUniversePackage.PACKAGE_UNIVERSE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.discovery.meta.BiobankUniversePackage;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.ontology.core.meta.SemanticTypeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BiobankUniverseMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "BiobankUniverse";
	public static final String BIOBANK_UNIVERSE = PACKAGE_UNIVERSE + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String MEMBERS = "members";
	public static final String OWNER = "owner";
	public static final String KEY_CONCEPTS = "keyConcepts";
	public static final String VECTORS = "vectors";

	private final BiobankUniversePackage biobankUniversePackage;
	private final BiobankSampleCollectionMetaData biobankSampleCollectionMetaData;
	private final MolgenisUserMetaData molgenisUserMetaData;
	private final SemanticTypeMetaData semanticTypeMetaData;

	@Autowired
	public BiobankUniverseMetaData(BiobankUniversePackage biobankUniversePackage,
			BiobankSampleCollectionMetaData biobankSampleCollectionMetaData, MolgenisUserMetaData molgenisUserMetaData,
			SemanticTypeMetaData semanticTypeMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_UNIVERSE);

		this.biobankUniversePackage = requireNonNull(biobankUniversePackage);
		this.biobankSampleCollectionMetaData = requireNonNull(biobankSampleCollectionMetaData);
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
		this.semanticTypeMetaData = requireNonNull(semanticTypeMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("Biobank universe");
		setPackage(biobankUniversePackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(NAME, ROLE_LABEL);
		addAttribute(MEMBERS).setDataType(MREF).setRefEntity(biobankSampleCollectionMetaData);
		addAttribute(OWNER).setDataType(XREF).setRefEntity(molgenisUserMetaData);
		addAttribute(KEY_CONCEPTS).setDataType(MREF).setRefEntity(semanticTypeMetaData);
		addAttribute(VECTORS).setDataType(TEXT);
	}
}
