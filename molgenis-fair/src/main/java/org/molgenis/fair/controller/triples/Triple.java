package org.molgenis.fair.controller.triples;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.fair.controller.triples.TripleMetadata.*;

public class Triple extends StaticEntity
{
	public Triple(Entity entity)
	{
		super(entity);
	}

	public Triple(EntityType entityType)
	{
		super(entityType);
	}

	public Triple(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setSubject(IRI subject)
	{
		set(SUBJECT, subject.stringValue());
	}

	public IRI getSubject()
	{
		return SimpleValueFactory.getInstance().createIRI(getString(SUBJECT));
	}

	public void setRelation(IRI predicate)
	{
		set(PREDICATE, predicate.stringValue());
	}

	public IRI getPredicate()
	{
		return SimpleValueFactory.getInstance().createIRI(getString(PREDICATE));
	}

	public void setObject(String object)
	{
		set(OBJECT, object);
	}

	public String getObject()
	{
		return getString(OBJECT);
	}
}
