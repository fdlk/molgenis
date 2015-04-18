package org.molgenis.ontocat.bioportal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.molgenis.ontocat.bean.OntologyBean;
import org.molgenis.ontocat.bean.OntologyTermBean;

import com.google.gson.Gson;

public class BioportalOntologyParser
{
	public static final String ID_KEY = "@id";
	private static final Logger LOGGER = Logger.getLogger(BioportalOntologyParser.class);

	public static List<OntologyTerm> convertJsonStringToOntologyTerms(String jsonString)
	{
		List<OntologyTerm> ontologyTerms = new ArrayList<OntologyTerm>();
		try
		{
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				ontologyTerms.add(convertJsonStringToOntologyTerm(jsonArray.get(i).toString()));
			}
		}
		catch (JSONException e)
		{
			LOGGER.error("Error occured while converting json string to List of OntologyTerms. Json string : "
					+ jsonString + "\n" + e.getMessage());
		}
		return ontologyTerms;
	}

	public static OntologyTerm convertJsonStringToOntologyTerm(String jsonString)
	{
		try
		{
			OntologyTermBean ontologyTermBean = new Gson().fromJson(jsonString, OntologyTermBean.class);
			JSONObject jsonObject = new JSONObject(jsonString);
			String iri = jsonObject.getString(ID_KEY);
			String id = ontologyTermBean.getSelf();
			String label = ontologyTermBean.getPrefLabel();
			String description = ontologyTermBean.getDefinition().size() == 0 ? StringUtils.EMPTY : ontologyTermBean
					.getDefinition().get(0);
			String ontologyAcroymn = conceptIriToId(ontologyTermBean.getOntology());

			return new BioportalOntologyTerm(id, iri, label, description, ontologyTermBean.getSynonym(),
					ontologyAcroymn);
		}
		catch (JSONException e)
		{
			LOGGER.error("Error occured while converting json string to ontologyTerm. Json string : " + jsonString
					+ "\n" + e.getMessage());
		}
		return null;
	}

	public static List<Ontology> convertJsonStringToOntologies(String jsonString)
	{
		List<Ontology> ontologies = new ArrayList<Ontology>();
		try
		{
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				ontologies.add(convertJsonStringToOntology(jsonArray.get(i).toString()));
			}
		}
		catch (JSONException e)
		{
			LOGGER.error("Error occured while converting json string to List of Ontologies. Json string : "
					+ jsonString + "\n" + e.getMessage());
		}
		return ontologies;
	}

	public static Ontology convertJsonStringToOntology(String jsonString)
	{
		try
		{
			JSONObject jsonObject = new JSONObject(jsonString);
			OntologyBean ontologyBean = new Gson().fromJson(jsonString, OntologyBean.class);
			String acronym = ontologyBean.getAcronym();
			String iri = jsonObject.getString(ID_KEY);
			return new BioportalOntology(acronym, iri, ontologyBean.getName());
		}
		catch (JSONException e)
		{
			LOGGER.error("Error occured while converting json string to ontology. Json string : " + jsonString + "\n"
					+ e.getMessage());
		}
		return null;
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
