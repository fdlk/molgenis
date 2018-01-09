package org.molgenis.web;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class PluginInterceptorTest extends AbstractMockitoTest
{
	@Mock
	private MenuReaderService menuReaderService;
	@Mock
	private PermissionService permissionService;
	@Mock
	private Authentication authentication;
	@Mock
	private Menu menu;

	private PluginInterceptor molgenisPluginInterceptor;

	@BeforeMethod
	public void setUp()
	{
		molgenisPluginInterceptor = new PluginInterceptor(menuReaderService, permissionService);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisPluginInterceptor()
	{
		new PluginInterceptor(null, null);
	}

	@Test
	public void preHandle() throws Exception
	{
		String uri = PluginController.PLUGIN_URI_PREFIX + "test";
		PluginController molgenisPlugin = createMolgenisPluginController(uri);
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(molgenisPlugin);

		PluginInterceptor molgenisPluginInterceptor = new PluginInterceptor(menuReaderService,
				permissionService);

		MockHttpServletRequest request = new MockHttpServletRequest();
		assertTrue(molgenisPluginInterceptor.preHandle(request, null, handlerMethod));
		assertEquals(request.getAttribute(PluginAttributes.KEY_CONTEXT_URL), uri);
	}

	@Test
	public void preHandle_hasContextUrl() throws Exception
	{
		String uri = PluginController.PLUGIN_URI_PREFIX + "test";
		PluginController molgenisPlugin = createMolgenisPluginController(uri);
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(molgenisPlugin);

		PluginInterceptor molgenisPluginInterceptor = new PluginInterceptor(menuReaderService,
				permissionService);

		String contextUri = "/plugin/not-test";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute(PluginAttributes.KEY_CONTEXT_URL, contextUri);
		assertTrue(molgenisPluginInterceptor.preHandle(request, null, handlerMethod));
		assertEquals(request.getAttribute(PluginAttributes.KEY_CONTEXT_URL), contextUri);

	}

	@Test
	public void postHandle() throws Exception
	{
		when(menuReaderService.getMenu()).thenReturn(menu);
		String uri = PluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "test");
		assertSame(modelAndView.getModel().get(PluginAttributes.KEY_MENU), menu);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), false);
	}

	@Test
	public void postHandle_pluginIdExists() throws Exception
	{
		when(menuReaderService.getMenu()).thenReturn(menu);
		String uri = PluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject(PluginAttributes.KEY_PLUGIN_ID, "plugin_id");
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "plugin_id");
		assertSame(modelAndView.getModel().get(PluginAttributes.KEY_MENU), menu);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), false);
	}

	@Test
	public void postHandlePluginidWithQueryString() throws Exception
	{
		when(menuReaderService.getMenu()).thenReturn(menu);
		String uri = PluginController.PLUGIN_URI_PREFIX + "plugin_id_test";

		HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
		when(mockHttpServletRequest.getQueryString()).thenReturn("entity=entityTypeId");

		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));

		ModelAndView modelAndView = new ModelAndView();
		molgenisPluginInterceptor.postHandle(mockHttpServletRequest, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "plugin_id_test");
		assertSame(modelAndView.getModel().get(PluginAttributes.KEY_MENU), menu);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), false);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID_WITH_QUERY_STRING),
				"plugin_id_test?entity=entityTypeId");
	}

	@Test
	public void postHandle_authenticated() throws Exception
	{
		when(menuReaderService.getMenu()).thenReturn(menu);
		boolean isAuthenticated = true;
		when(authentication.isAuthenticated()).thenReturn(isAuthenticated);

		String uri = PluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		PluginController pluginController = createMolgenisPluginController(uri);
		DataService dataService = mock(DataService.class);
		pluginController.setDataService(dataService);
		when(handlerMethod.getBean()).thenReturn(pluginController);
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "test");
		assertSame(modelAndView.getModel().get(PluginAttributes.KEY_MENU), menu);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), isAuthenticated);
	}

	@Test
	public void postHandle_notAuthenticated() throws Exception
	{
		when(menuReaderService.getMenu()).thenReturn(menu);
		boolean isAuthenticated = false;
		when(authentication.isAuthenticated()).thenReturn(isAuthenticated);

		String uri = PluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "test");
		assertSame(modelAndView.getModel().get(PluginAttributes.KEY_MENU), menu);
		assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), isAuthenticated);
	}

	private PluginController createMolgenisPluginController(String uri)
	{
		PluginController pluginController = new PluginController(uri)
		{
		};
		DataService dataService = mock(DataService.class);
		pluginController.setDataService(dataService);
		return pluginController;
	}
}
