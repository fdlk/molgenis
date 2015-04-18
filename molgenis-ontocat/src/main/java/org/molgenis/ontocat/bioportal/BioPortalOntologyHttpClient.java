package org.molgenis.ontocat.bioportal;

import static org.molgenis.ontocat.bioportal.BioportalOntologyParser.convertJsonStringToOntologyTerms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class BioPortalOntologyHttpClient
{
	private static final Logger LOGGER = Logger.getLogger(BioPortalOntologyHttpClient.class);

	private final String APIKEY;

	public BioPortalOntologyHttpClient(String apiKey)
	{
		APIKEY = apiKey;
	}

	public List<OntologyTerm> recursivelyPageChildren(String url)
	{
		List<OntologyTerm> children = new ArrayList<OntologyTerm>();
		try
		{
			JSONObject jsonObject = new JSONObject(getHttpResponse(url));
			if (StringUtils.isNotEmpty(jsonObject.getString("nextPage")))
			{
				JSONObject linksJsonObject = new JSONObject(jsonObject.getString("links"));
				children.addAll(recursivelyPageChildren(linksJsonObject.getString("nextPage")));
			}
			String collection = jsonObject.getString("collection");
			if (StringUtils.isNotEmpty(collection))
			{
				children.addAll(convertJsonStringToOntologyTerms(collection));
			}
		}
		catch (JSONException e)
		{
			LOGGER.error("Error occurred when recursively retrieving children from BioPortal. The url for getting current page of children is : "
					+ url + "\n" + e.getMessage());
		}
		return children;
	}

	public String getHttpResponse(String url)
	{
		String responseString = StringUtils.EMPTY;
		for (int i = 0; i < 50; i++)
		{
			if (StringUtils.isNotEmpty(responseString)) break;

			if (i > 0)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					LOGGER.error(e.getMessage());
				}
			}

			try
			{
				HttpClient httpClient = HttpClientBuilder.create().build();
				HttpGet httpGet = new HttpGet(processUrl(url));
				HttpResponse httpResponse = httpClient.execute(httpGet);
				responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			}
			catch (IOException e)
			{
				LOGGER.error("Failed to retrieve the response for request " + url + "\n" + e.getMessage());
			}
		}
		return responseString;
	}

	private String processUrl(String url)
	{
		return url.contains("apikey") ? url : url + "?apikey=" + APIKEY;
	}
}
