package org.molgenis.data.discovery.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.discovery.meta.BiobankUniversePackage;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = AttributeTermFrequencyServiceImplTest.Config.class)
public class AttributeTermFrequencyServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	DataService dataService;

	@Autowired
	BiobankSampleAttributeMetaData biobankSampleAttributeMetaData;

	@Autowired
	AttributeTermFrequencyServiceImpl attributeTermFrequencyServiceImpl;

	@BeforeMethod
	public void setup()
	{
		Entity attr1 = new DynamicEntity(biobankSampleAttributeMetaData);
		attr1.set(BiobankSampleAttributeMetaData.IDENTIFIER, "1");
		attr1.set(BiobankSampleAttributeMetaData.NAME, "attr1");
		attr1.set(BiobankSampleAttributeMetaData.LABEL, "history of hypertension");

		Entity attr2 = new DynamicEntity(biobankSampleAttributeMetaData);
		attr2.set(BiobankSampleAttributeMetaData.IDENTIFIER, "2");
		attr2.set(BiobankSampleAttributeMetaData.NAME, "attr2");
		attr2.set(BiobankSampleAttributeMetaData.LABEL, "history of heart attack");

		Entity attr3 = new DynamicEntity(biobankSampleAttributeMetaData);
		attr3.set(BiobankSampleAttributeMetaData.IDENTIFIER, "3");
		attr3.set(BiobankSampleAttributeMetaData.NAME, "attr3");
		attr3.set(BiobankSampleAttributeMetaData.LABEL, "history of diabetes");

		List<Entity> biobankAttributeEntities = Arrays.asList(attr1, attr2, attr3);

		when(dataService.findAll(BiobankSampleAttributeMetaData.BIOBANK_SAMPLE_ATTRIBUTE))
				.thenReturn(biobankAttributeEntities.stream());

		when(dataService.count(BiobankSampleAttributeMetaData.BIOBANK_SAMPLE_ATTRIBUTE,
				QueryImpl.query().pageSize(Integer.MAX_VALUE))).thenReturn((long) 3);
	}

	@Test
	public void getTermFrequency()
	{
		assertEquals(attributeTermFrequencyServiceImpl.getTermFrequency("history"), 0f);

		assertEquals(attributeTermFrequencyServiceImpl.getTermFrequency("heart"), 0.477121254719662f);
	}

	@Test
	public void getTermOccurrence()
	{
		Integer actual = attributeTermFrequencyServiceImpl.getTermOccurrence("history");
		assertEquals(3, actual.intValue());
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.discovery.meta", "org.molgenis.auth" })
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public BiobankUniversePackage biobankUniversePackage()
		{
			return mock(BiobankUniversePackage.class);
		}

		@Bean
		public AttributeTermFrequencyServiceImpl attributeTermFrequencyServiceImpl()
		{
			return new AttributeTermFrequencyServiceImpl(dataService());
		}
	}
}
