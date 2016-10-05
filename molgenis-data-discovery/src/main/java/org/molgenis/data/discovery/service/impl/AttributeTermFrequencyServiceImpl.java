package org.molgenis.data.discovery.service.impl;

import com.google.common.base.Supplier;
import org.molgenis.data.DataService;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.molgenis.ontology.utils.Stemmer;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.BIOBANK_SAMPLE_ATTRIBUTE;

/**
 * This service retrieves the inverse document frequency for the terms that are available derived from all
 * {@link BiobankSampleAttribute}s
 *
 * @author chaopang
 */
public class AttributeTermFrequencyServiceImpl implements TermFrequencyService
{
	private final DataService dataService;

	private Supplier<Integer> memoizedTotalAttributes = memoizeWithExpiration(new Supplier<Integer>()
	{
		public Integer get()
		{
			return (int) dataService.count(BIOBANK_SAMPLE_ATTRIBUTE, QueryImpl.query().pageSize(Integer.MAX_VALUE));
		}
	}, (long) 30, MINUTES);

	private Supplier<Map<String, Integer>> memoizeTermFrequency = memoizeWithExpiration(
			new Supplier<Map<String, Integer>>()
			{
				public Map<String, Integer> get()
				{
					return createTermFrequency();
				}
			}, (long) 30, MINUTES);

	public AttributeTermFrequencyServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public float getTermFrequency(String term)
	{
		return (float) Math.log10((double) memoizedTotalAttributes.get() / getTermOccurrence(term));
	}

	public int getTermOccurrence(String term)
	{
		String stemmedTerm = Stemmer.stem(term);
		return memoizeTermFrequency.get().containsKey(stemmedTerm) ? memoizeTermFrequency.get().get(stemmedTerm) : 1;
	}

	public void updateTermFrequency()
	{

	}

	private Map<String, Integer> createTermFrequency()
	{
		Map<String, Integer> termFrequency = new HashMap<>();

		dataService.findAll(BIOBANK_SAMPLE_ATTRIBUTE).forEach(entity ->
		{

			String label = entity.getString(BiobankSampleAttributeMetaData.LABEL);

			for (String word : Stemmer.splitAndStem(label))
			{
				if (!termFrequency.containsKey(word))
				{
					termFrequency.put(word, 0);
				}
				termFrequency.put(word, termFrequency.get(word) + 1);
			}
		});

		return termFrequency;
	}
}
