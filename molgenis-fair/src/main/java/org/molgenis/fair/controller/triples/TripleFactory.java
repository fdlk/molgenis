package org.molgenis.fair.controller.triples;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class TripleFactory extends AbstractSystemEntityFactory<Triple, TripleMetadata, String>
{
	TripleFactory(TripleMetadata tripleMetadata, EntityPopulator entityPopulator)
	{
		super(Triple.class, tripleMetadata, entityPopulator);
	}
}
