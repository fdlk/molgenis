package org.molgenis.api.tests.app;

import com.google.common.collect.ImmutableMap;
import org.molgenis.app.manager.controller.AppController;
import org.molgenis.app.manager.controller.AppManagerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import org.testng.util.Strings;

import java.util.List;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITE;
import static org.molgenis.app.manager.controller.AppManagerController.URI;
import static org.molgenis.apps.model.AppMetaData.APP;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;

public class AppManagerAPIIT
{
	private static final Logger LOG = LoggerFactory.getLogger(AppManagerAPIIT.class);

	// User credentials
	private static final String APP_TEST_USER_PASSWORD = "app_test_user_password";

	private String adminToken;
	private String userToken;
	private String testUsername;

	/**
	 * Pass down system properties via the mvn commandline argument
	 * <p>
	 * example:
	 * mvn test -Dtest="AppManagerAPIIT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl" -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
	 */
	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		baseURI = Strings.isNullOrEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envAdminPW) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);
		testUsername = "app_test_user" + System.currentTimeMillis();
		createUser(adminToken, testUsername, APP_TEST_USER_PASSWORD);

		setGrantedRepositoryPermissions(adminToken, testUsername,
				ImmutableMap.<String, Permission>builder().put(PACKAGE, READ)
														  .put(ENTITY_TYPE_META_DATA, READ)
														  .put(ATTRIBUTE_META_DATA, READ)
														  .put(APP, WRITE)
														  .put(PLUGIN, WRITE)
														  .build());

		setGrantedPluginPermissions(adminToken, testUsername, AppController.ID, AppManagerController.ID);

		userToken = login(testUsername, APP_TEST_USER_PASSWORD);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .when()
			   .multiPart("file", "example2.zip", getClass().getResourceAsStream("example2.zip"), "application/zip")
			   .post(URI + "/upload")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);
	}

	@AfterMethod
	public void afterMethod()
	{
		// delete apps
		List<String> ids = given().header(X_MOLGENIS_TOKEN, adminToken).get(URI + "/apps").path("id");
		ids.forEach(id ->
		{
			given().header(X_MOLGENIS_TOKEN, adminToken).post(URI + "/deactivate/{id}", id);
			given().header(X_MOLGENIS_TOKEN, adminToken).delete(URI + "/delete/{id}", id);
		});
	}

	@AfterClass
	public void afterClass()
	{
		// Clean up permissions
		removeRightsForUser(adminToken, testUsername);
	}

	@Test
	public void testUploadExistingApp()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .multiPart("file", "example2.zip", getClass().getResourceAsStream("example2.zip"), "application/zip")
			   .post(URI + "/upload")
			   .then()
			   .log()
			   .all()
			   .statusCode(NOT_FOUND) //TODO: why?
			   .body("errors.code", hasItem("AM08"));
	}

	@Test
	public void testActivateAndDeactivate()
	{
		List<String> ids = given().header(X_MOLGENIS_TOKEN, adminToken).get(URI + "/apps").path("id");
		String appId = ids.get(0);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .post(URI + "/activate/{id}", appId)
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);

		given().header(X_MOLGENIS_TOKEN, userToken).when().get(URI + "/apps").then().content("isActive", hasItem(true));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .post(URI + "/deactivate/{id}", appId)
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);

		given().header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .get(URI + "/apps")
			   .then()
			   .content("isActive", hasItem(false));
	}

	@Test
	public void testServe()
	{
		List<String> ids = given().header(X_MOLGENIS_TOKEN, adminToken).get(URI + "/apps").path("id");
		String appId = ids.get(0);
		given().header(X_MOLGENIS_TOKEN, adminToken).post(URI + "/activate/{id}", appId).then().statusCode(OKE);

		given().header(X_MOLGENIS_TOKEN, adminToken)
			   .get(AppController.URI + "/{uri}/", "example2")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .content("//title", hasItem("Hello app manager"));
	}

	@Test
	public void testGetApps()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .when()
			   .get(URI + "/apps")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .contentType("application/json;charset=UTF-8")
			   .content("uri", hasItem("example2"))
			   .content("label", hasItem("Hello multipage 2"))
			   .content("resourceFolder", hasItem("apps/example2"));
	}

	@Test
	public void deleteApp()
	{
		List<String> ids = given().header(X_MOLGENIS_TOKEN, adminToken).get(URI + "/apps").path("id");
		String appId = ids.get(0);

		given().header(X_MOLGENIS_TOKEN, adminToken).post(URI + "/activate/{id}", appId).then().statusCode(OKE);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .when()
			   .delete(URI + "/delete/{id}", appId)
			   .then()
			   .statusCode(OKE);

		given().header(X_MOLGENIS_TOKEN, adminToken).get(URI + "/apps").then().content("id", empty());
	}
}

