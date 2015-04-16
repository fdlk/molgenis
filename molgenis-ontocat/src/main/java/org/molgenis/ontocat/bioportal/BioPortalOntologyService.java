package org.molgenis.ontocat.bioportal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.molgenis.ontocat.bean.OntologyBean;
import org.molgenis.ontocat.bean.OntologyTermBean;
import org.molgenis.ontocat.ontologyservice.OntologyService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

public class BioPortalOntologyService implements OntologyService
{
	private static final Logger LOGGER = Logger.getLogger(BioPortalOntologyService.class);
	private static final String URL_BASE = "http://data.bioontology.org/";
	private static final String ID_KEY = "@id";

	private final LoadingCache<String, Ontology> cachedOntologies = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, Ontology>()
			{
				public Ontology load(String ontologyAccession) throws Exception
				{
					String httpResponse = getHttpResponse(URL_BASE + "ontologies/" + ontologyAccession);
					return convertJsonStringToOntology(httpResponse);
				}
			});

	private final LoadingCache<String, List<OntologyTerm>> cachedChildOntologyTerms = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).expireAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<OntologyTerm>>()
			{
				public List<OntologyTerm> load(String id) throws JSONException, ParseException, IOException
				{
					List<OntologyTerm> children = new ArrayList<OntologyTerm>();
					recursivelyPageChildren(id + "/children", children);
					return children;
				}
			});

	private final String APIKEY;

	public BioPortalOntologyService()
	{
		this("4d7ac5ac-789a-4d7f-b4e1-8478e87e2f3c");
	}

	public BioPortalOntologyService(String apiKey)
	{
		this.APIKEY = apiKey;
	}

	public List<Ontology> getOntologies()
	{
		try
		{
			String httpResponse = getHttpResponse(URL_BASE + "ontologies");
			return convertJsonToOntologies(httpResponse);
		}
		catch (ParseException | IOException | JSONException e)
		{
			LOGGER.error(e.getMessage());
		}

		return Collections.emptyList();
	}

	@Override
	public Ontology getOntology(String ontologyAccession)
	{
		try
		{
			return cachedOntologies.get(ontologyAccession);
		}
		catch (ParseException | ExecutionException e)
		{
			LOGGER.error(e.getMessage());
		}

		return null;
	}

	public List<OntologyTerm> getRootTerms(String ontologyAccession)
	{
		try
		{
			String httpResponse = getHttpResponse(URL_BASE + "ontologies/" + ontologyAccession + "/classes/roots");
			return convertJsonStringToOntologyTerms(httpResponse);
		}
		catch (ParseException | IOException | JSONException e)
		{
			LOGGER.error(e.getMessage());
		}
		return Collections.emptyList();
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
			catch (ParseException | ExecutionException e)
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
			String httpResponse = getHttpResponse(URL_BASE + "ontologies/" + ontologyAcronym + "/classes/");
			JSONObject jsonObject = new JSONObject(httpResponse);
			int pageCount = jsonObject.getInt("pageCount");
			List<OntologyTerm> convertJsonStringToOntologyTerms = convertJsonStringToOntologyTerms(jsonObject
					.getString("collection"));
			return pageCount * convertJsonStringToOntologyTerms.size();
		}
		catch (ParseException | IOException | JSONException e)
		{
			LOGGER.error(e.getMessage());
		}
		return 0;
	}

	private String getHttpResponse(String url) throws ParseException, IOException
	{
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(processUrl(url));
		HttpResponse response = httpClient.execute(httpGet);
		return response == null ? StringUtils.EMPTY : EntityUtils.toString(response.getEntity(), "UTF-8");
	}

	private List<OntologyTerm> convertJsonStringToOntologyTerms(String jsonString) throws JSONException
	{
		List<OntologyTerm> ontologyTerms = new ArrayList<OntologyTerm>();
		JSONArray jsonArray = new JSONArray(jsonString);
		for (int i = 0; i < jsonArray.length(); i++)
		{
			ontologyTerms.add(convertJsonStringToOntologyTerm(jsonArray.get(i).toString()));
		}
		return ontologyTerms;
	}

	private OntologyTerm convertJsonStringToOntologyTerm(String jsonString) throws JSONException
	{
		OntologyTermBean ontologyTermBean = new Gson().fromJson(jsonString, OntologyTermBean.class);
		JSONObject jsonObject = new JSONObject(jsonString);

		String iri = jsonObject.getString(ID_KEY);
		String id = ontologyTermBean.getSelf();
		String label = ontologyTermBean.getPrefLabel();
		String description = ontologyTermBean.getDefinition().size() == 0 ? StringUtils.EMPTY : ontologyTermBean
				.getDefinition().get(0);

		Ontology ontology = getOntology(conceptIriToId(ontologyTermBean.getOntology()));

		return new BioportalOntologyTerm(id, iri, label, description, ontologyTermBean.getSynonym(), ontology);
	}

	private List<Ontology> convertJsonToOntologies(String jsonString) throws JSONException
	{
		List<Ontology> ontologies = new ArrayList<Ontology>();
		JSONArray jsonArray = new JSONArray(jsonString);
		for (int i = 0; i < jsonArray.length(); i++)
		{
			ontologies.add(convertJsonStringToOntology(jsonArray.get(i).toString()));
		}
		return ontologies;
	}

	private Ontology convertJsonStringToOntology(String jsonString) throws JSONException
	{
		JSONObject jsonObject = new JSONObject(jsonString);
		OntologyBean ontologyBean = new Gson().fromJson(jsonString, OntologyBean.class);
		String acronym = ontologyBean.getAcronym();
		String iri = jsonObject.getString(ID_KEY);
		return new BioportalOntology(acronym, iri, ontologyBean.getName());
	}

	private void recursivelyPageChildren(String uri, List<OntologyTerm> children) throws ParseException, JSONException,
			IOException
	{
		JSONObject jsonObject = new JSONObject(getHttpResponse(uri));
		if (StringUtils.isNotEmpty(jsonObject.getString("nextPage")))
		{
			JSONObject linksJsonObject = new JSONObject(jsonObject.getString("links"));
			recursivelyPageChildren(linksJsonObject.getString("nextPage"), children);
		}
		String collection = jsonObject.getString("collection");
		if (StringUtils.isNotEmpty(collection))
		{
			children.addAll(convertJsonStringToOntologyTerms(collection));
		}
	}

	private String processUrl(String url)
	{
		return url.contains("apikey") ? url : url + "?apikey=" + APIKEY;
	}

	public static String conceptIriToId(String iri)
	{
		StringBuilder stringBuilder = new StringBuilder();
		String[] split = null;
		if (iri.contains("#"))
		{
			split = iri.split("#");
		}
		else
		{
			split = iri.split("/");
		}
		stringBuilder.append(split[split.length - 1]);
		return stringBuilder.toString();
	}
}