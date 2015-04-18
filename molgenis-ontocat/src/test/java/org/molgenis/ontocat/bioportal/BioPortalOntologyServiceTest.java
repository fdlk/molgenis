//package org.molgenis.ontocat.bioportal;
//
//import static org.mockito.Mockito.mock;
//import static org.testng.Assert.assertEquals;
//
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//public class BioPortalOntologyServiceTest
//{
//	private BioPortalOntologyService bioPortalOntologyService;
//
//	@BeforeClass
//	public void setup()
//	{
//		bioPortalOntologyService = mock(BioPortalOntologyService.class);
//	}
//
//	@Test
//	public void conceptIriToId()
//	{
//		assertEquals(BioPortalOntologyService.conceptIriToId("http://www.molgenis.org/123"), "123");
//		assertEquals(BioPortalOntologyService.conceptIriToId("http://www.molgenis.org#456"), "456");
//	}
//
//	@Test
//	public void getOntologies()
//	{
//		bioPortalOntologyService.getOntologies();
//	}
//
//	@Test
//	public void getOntology()
//	{
//		throw new RuntimeException("Test not implemented");
//	}
//
//	@Test
//	public void getProxyCountForOntology()
//	{
//		throw new RuntimeException("Test not implemented");
//	}
//
//	@Test
//	public void getChildren()
//	{
//		throw new RuntimeException("Test not implemented");
//	}
//
//	@Test
//	public void getRootTerms()
//	{
//		throw new RuntimeException("Test not implemented");
//	}
// }
