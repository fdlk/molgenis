package org.molgenis.data.rdf;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Interacts with RDF4J repository.
 * <p>
 * Note that the version of RDF4J matters!
 * Most backends still implement the old version, called Sesame, with an API in the openrdf package.
 * We're using the newer RDF4J API which sits in the org.eclipse.rdf4j package.
 * <p>
 * You can use any RDF4J compatible HTTP server, like GraphDB.
 * Or you can configure a local Repository.
 */
@Service
public class TripleStore
{
	private static final Logger LOG = LoggerFactory.getLogger(TripleStore.class);

	private Repository repository;

	public TripleStore(Repository repository)
	{
		this.repository = repository;
	}

	public void store(Model model)
	{
		try (RepositoryConnection connection = repository.getConnection())
		{
			connection.begin();
			connection.add(model);
			connection.commit();
		}
	}

	public Model findAll(String s, String p, String o)
	{
		Model result = new LinkedHashModel();
		try (RepositoryConnection connection = repository.getConnection())
		{
			ValueFactory valueFactory = connection.getValueFactory();
			Resource subject = null;
			if (s != null)
			{
				subject = valueFactory.createIRI(s);
			}
			IRI predicate = null;
			if (p != null)
			{
				predicate = valueFactory.createIRI(p);
			}
			Value object = null;
			if (o != null)
			{
				try
				{
					object = valueFactory.createIRI(o);
				}
				catch (IllegalArgumentException notAnIRI)
				{
					object = valueFactory.createLiteral(o);
				}
			}

			RepositoryResult<Statement> statements = connection.getStatements(subject, predicate, object);

			while (statements.hasNext())
			{
				result.add(statements.next());
			}
		}
		return result;
	}
}
