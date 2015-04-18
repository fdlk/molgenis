package org.molgenis.ontocat.bioportal;

import static org.molgenis.ontocat.bioportal.BioportalOntologyParser.convertJsonStringToOntologies;
import static org.molgenis.ontocat.bioportal.BioportalOntologyParser.convertJsonStringToOntology;
import static org.molgenis.ontocat.bioportal.BioportalOntologyParser.convertJsonStringToOntologyTerms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.molgenis.ontocat.ontologyservice.OntologyService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class BioPortalOntologyService implements OntologyService
{
	private static final Logger LOGGER = Logger.getLogger(BioPortalOntologyService.class);

	private static final String URL_BASE = "http://data.bioontology.org/";

	private final LoadingCache<String, Ontology> cachedOntologies = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, Ontology>()
			{
				public Ontology load(String ontologyAccession)
				{
					String httpResponse = httpClient.getHttpResponse(URL_BASE + "ontologies/" + ontologyAccession);
					return convertJsonStringToOntology(httpResponse);
				}
			});

	private final LoadingCache<String, List<OntologyTerm>> cachedChildOntologyTerms = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).expireAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<OntologyTerm>>()
			{
				public List<OntologyTerm> load(String id)
				{
					List<OntologyTerm> children = new ArrayList<OntologyTerm>();
					httpClient.recursivelyPageChildren(id + "/children", children);
					return children;
				}
			});

	private final BioPortalOntologyHttpClient httpClient;

	public BioPortalOntologyService()
	{
		this("4d7ac5ac-789a-4d7f-b4e1-8478e87e2f3c");
	}

	public BioPortalOntologyService(String apiKey)
	{
		this.httpClient = new BioPortalOntologyHttpClient(apiKey);
	}

	public List<Ontology> getOntologies()
	{
		String httpResponse = httpClient.getHttpResponse(URL_BASE + "ontologies");
		return convertJsonStringToOntologies(httpResponse);
	}

	@Override
	public Ontology getOntology(String ontologyAcronym)
	{
		try
		{
			return cachedOntologies.get(ontologyAcronym);
		}
		catch (ExecutionException e)
		{
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	public List<OntologyTerm> getRootTerms(String ontologyAccession)
	{
		String httpResponse = httpClient.getHttpResponse(URL_BASE + "ontologies/" + ontologyAccession
				+ "/classes/roots");
		return convertJsonStringToOntologyTerms(httpResponse);
	}

	@Override
	public List<OntologyTerm> getChildren(OntologyTerm ontologyTerm)
	{
		if (ontologyTerm instanceof BioportalOntologyTerm)
		{
			try
			{
				String id = ((BioportalOntologyTerm) ontologyTerm).getId();
				return cachedChildOntologyTerms.get(id);
			}
			catch (ExecutionException e)
			{
				LOGGER.error(e.getMessage());
			}
		}
		return Collections.emptyList();
	}

	public int getProxyCountForOntology(String ontologyAcronym)
	{
		try
		{
			String httpResponse = httpClient.getHttpResponse(URL_BASE + "ontologies/" + ontologyAcronym + "/classes/");
			JSONObject jsonObject = new JSONObject(httpResponse);
			int pageCount = jsonObject.getInt("pageCount");
			List<OntologyTerm> convertJsonStringToOntologyTerms = convertJsonStringToOntologyTerms(jsonObject
					.getString("collection"));
			return pageCount * convertJsonStringToOntologyTerms.size();
		}
		catch (JSONException e)
		{
			LOGGER.error(e.getMessage());
		}

		return 0;
	}
}