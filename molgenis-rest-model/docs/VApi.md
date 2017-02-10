# VApi

All URIs are relative to *http://localhost:8080/api/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**v1LoginPost**](VApi.md#v1LoginPost) | **POST** /v1/login | Logs into a MOLGENIS user account
[**v2CopyEntityNamePost**](VApi.md#v2CopyEntityNamePost) | **POST** /v2/copy/{entity_name} | Creates a copy of an entity.
[**v2EntityNameGet**](VApi.md#v2EntityNameGet) | **GET** /v2/{entity_name} | Retrieves an entity collection
[**v2EntityNameIdDelete**](VApi.md#v2EntityNameIdDelete) | **DELETE** /v2/{entity_name}/{id} | Deletes an entity
[**v2EntityNameIdGet**](VApi.md#v2EntityNameIdGet) | **GET** /v2/{entity_name}/{id} | Retrieves an entity
[**v2EntityNameMetaAttributeNameGet**](VApi.md#v2EntityNameMetaAttributeNameGet) | **GET** /v2/{entity_name}/meta/{attribute_name} | Retrieve attribute metadata
[**v2EntityNamePost**](VApi.md#v2EntityNamePost) | **POST** /v2/{entity_name} | Retrieves an entity collection
[**v2VersionGet**](VApi.md#v2VersionGet) | **GET** /v2/version | Retrieves the MOLGENIS version


<a name="v1LoginPost"></a>
# **v1LoginPost**
> LoginResponse v1LoginPost(body)

Logs into a MOLGENIS user account

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
LoginRequest body = new LoginRequest(); // LoginRequest | User credentials
try {
    LoginResponse result = apiInstance.v1LoginPost(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v1LoginPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**LoginRequest**](LoginRequest.md)| User credentials |

### Return type

[**LoginResponse**](LoginResponse.md)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

<a name="v2CopyEntityNamePost"></a>
# **v2CopyEntityNamePost**
> v2CopyEntityNamePost(entityName, body)

Creates a copy of an entity.

The copy will be created in the same package and backend as the original entity, so both must be writable.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
String entityName = "entityName_example"; // String | Name of the entity
CopyEntityRequest body = new CopyEntityRequest(); // CopyEntityRequest | 
try {
    apiInstance.v2CopyEntityNamePost(entityName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v2CopyEntityNamePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **entityName** | **String**| Name of the entity | [enum: AnnotationJobExecution, Attribute, AttributeMapping, CGD, EntityMapping, EntityType, FileIngest, FileIngestJobExecution, FileMeta, FreemarkerTemplate, Gavin, GavinJobExecution, GeneNetworkScore, Group, GroupAuthority, GroupMember, ImportRun, IndexAction, IndexActionGroup, IndexJobExecution, JavaMailProperty, Language, MailSettings, MappingProject, MappingTarget, OMIM, Ontology, OntologyTerm, OntologyTermDynamicAnnotation, OntologyTermHit, OntologyTermNodePath, OntologyTermSynonym, Package, Script, ScriptParameter, ScriptType, SortaJobExecution, StaticContent, Tag, TermFrequency, Token, User, UserAuthority, age_units, app, biobank_size, biobanks, body_parts, cadd, clinvar, collection_types, collections, countries, dann, data_types, dataexplorer, directory, disease_types, exac, fitcon, genomicdata, gonl, hpo, i18nstrings, image_data_types, imaging_modality, material_types, networks, persons, sex_types, snpEff, temp_types, thousand_genomes]
 **body** | [**CopyEntityRequest**](CopyEntityRequest.md)|  |

### Return type

null (empty response body)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

<a name="v2EntityNameGet"></a>
# **v2EntityNameGet**
> EntityCollectionResponseV2 v2EntityNameGet(entityName, attrs, q, aggs, sort, start, num)

Retrieves an entity collection

Retrieves an entity collection based on entity name

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
String entityName = "entityName_example"; // String | Name of the entity
List<String> attrs = Arrays.asList("attrs_example"); // List<String> | Defines which fields from the Entity to select. For each attribute that references another entity, may be postfixed with the attrs to fetch for that entity, between (). Special attribute names are ~id and ~lbl for the idAttribute and labelAttribute respectively.
String q = "q_example"; // String | RSQL query to filter the Entity collection response
String aggs = "aggs_example"; // String | RSQL query to filter the Entity collection aggregates. The aggregation query supports the RSQL selectors 'x', 'y' and 'distinct' and the RSQL operator '=='. The selector 'x' defines the first aggregation attribute name, 'y' defines the second aggregation attribute name, 'distinct' defines the distinct aggregation attribute name.
List<String> sort = Arrays.asList("sort_example"); // List<String> | Sort specification. Format is a comma separated list of attribute names. Each name may be followed by :asc or :desc to indicate sort order. Default sort order is ascending.
Integer start = 0; // Integer | Offset in resource collection
Integer num = 0; // Integer | Number of resources to retrieve starting at start
try {
    EntityCollectionResponseV2 result = apiInstance.v2EntityNameGet(entityName, attrs, q, aggs, sort, start, num);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v2EntityNameGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **entityName** | **String**| Name of the entity | [enum: AnnotationJobExecution, Attribute, AttributeMapping, CGD, EntityMapping, EntityType, FileIngest, FileIngestJobExecution, FileMeta, FreemarkerTemplate, Gavin, GavinJobExecution, GeneNetworkScore, Group, GroupAuthority, GroupMember, ImportRun, IndexAction, IndexActionGroup, IndexJobExecution, JavaMailProperty, Language, MailSettings, MappingProject, MappingTarget, OMIM, Ontology, OntologyTerm, OntologyTermDynamicAnnotation, OntologyTermHit, OntologyTermNodePath, OntologyTermSynonym, Package, Script, ScriptParameter, ScriptType, SortaJobExecution, StaticContent, Tag, TermFrequency, Token, User, UserAuthority, age_units, app, biobank_size, biobanks, body_parts, cadd, clinvar, collection_types, collections, countries, dann, data_types, dataexplorer, directory, disease_types, exac, fitcon, genomicdata, gonl, hpo, i18nstrings, image_data_types, imaging_modality, material_types, networks, persons, sex_types, snpEff, temp_types, thousand_genomes]
 **attrs** | [**List&lt;String&gt;**](String.md)| Defines which fields from the Entity to select. For each attribute that references another entity, may be postfixed with the attrs to fetch for that entity, between (). Special attribute names are ~id and ~lbl for the idAttribute and labelAttribute respectively. | [optional]
 **q** | **String**| RSQL query to filter the Entity collection response | [optional]
 **aggs** | **String**| RSQL query to filter the Entity collection aggregates. The aggregation query supports the RSQL selectors &#39;x&#39;, &#39;y&#39; and &#39;distinct&#39; and the RSQL operator &#39;&#x3D;&#x3D;&#39;. The selector &#39;x&#39; defines the first aggregation attribute name, &#39;y&#39; defines the second aggregation attribute name, &#39;distinct&#39; defines the distinct aggregation attribute name. | [optional]
 **sort** | [**List&lt;String&gt;**](String.md)| Sort specification. Format is a comma separated list of attribute names. Each name may be followed by :asc or :desc to indicate sort order. Default sort order is ascending. | [optional]
 **start** | **Integer**| Offset in resource collection | [optional] [default to 0]
 **num** | **Integer**| Number of resources to retrieve starting at start | [optional] [default to 0]

### Return type

[**EntityCollectionResponseV2**](EntityCollectionResponseV2.md)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

<a name="v2EntityNameIdDelete"></a>
# **v2EntityNameIdDelete**
> v2EntityNameIdDelete(entityName, id)

Deletes an entity

Deletes an entity instance based on entity name and ID

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
String entityName = "entityName_example"; // String | Name of the entity
String id = "id_example"; // String | ID of the entity instance
try {
    apiInstance.v2EntityNameIdDelete(entityName, id);
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v2EntityNameIdDelete");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **entityName** | **String**| Name of the entity |
 **id** | **String**| ID of the entity instance |

### Return type

null (empty response body)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

<a name="v2EntityNameIdGet"></a>
# **v2EntityNameIdGet**
> v2EntityNameIdGet(entityName, id, attrs, method)

Retrieves an entity

Retrieves an entity instance based on entity name and ID

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
String entityName = "entityName_example"; // String | Name of the entity
String id = "id_example"; // String | ID of the entity instance
String attrs = "attrs_example"; // String | Defines which fields from the Entity to select
String method = "method_example"; // String | Tunnel request through defined method over default API operation
try {
    apiInstance.v2EntityNameIdGet(entityName, id, attrs, method);
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v2EntityNameIdGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **entityName** | **String**| Name of the entity | [enum: AnnotationJobExecution, Attribute, AttributeMapping, CGD, EntityMapping, EntityType, FileIngest, FileIngestJobExecution, FileMeta, FreemarkerTemplate, Gavin, GavinJobExecution, GeneNetworkScore, Group, GroupAuthority, GroupMember, ImportRun, IndexAction, IndexActionGroup, IndexJobExecution, JavaMailProperty, Language, MailSettings, MappingProject, MappingTarget, OMIM, Ontology, OntologyTerm, OntologyTermDynamicAnnotation, OntologyTermHit, OntologyTermNodePath, OntologyTermSynonym, Package, Script, ScriptParameter, ScriptType, SortaJobExecution, StaticContent, Tag, TermFrequency, Token, User, UserAuthority, age_units, app, biobank_size, biobanks, body_parts, cadd, clinvar, collection_types, collections, countries, dann, data_types, dataexplorer, directory, disease_types, exac, fitcon, genomicdata, gonl, hpo, i18nstrings, image_data_types, imaging_modality, material_types, networks, persons, sex_types, snpEff, temp_types, thousand_genomes]
 **id** | **String**| ID of the entity instance |
 **attrs** | **String**| Defines which fields from the Entity to select | [optional]
 **method** | **String**| Tunnel request through defined method over default API operation | [optional]

### Return type

null (empty response body)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

<a name="v2EntityNameMetaAttributeNameGet"></a>
# **v2EntityNameMetaAttributeNameGet**
> v2EntityNameMetaAttributeNameGet(entityName, attributeName, method)

Retrieve attribute metadata

Retrieve attribute metadata based on entity name and attribute name

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
String entityName = "entityName_example"; // String | Name of the entity
String attributeName = "attributeName_example"; // String | Name of the attribute
String method = "method_example"; // String | Tunnel request through defined method over default API operation
try {
    apiInstance.v2EntityNameMetaAttributeNameGet(entityName, attributeName, method);
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v2EntityNameMetaAttributeNameGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **entityName** | **String**| Name of the entity |
 **attributeName** | **String**| Name of the attribute |
 **method** | **String**| Tunnel request through defined method over default API operation | [optional] [enum: POST, GET]

### Return type

null (empty response body)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

<a name="v2EntityNamePost"></a>
# **v2EntityNamePost**
> EntityCollectionResponseV2 v2EntityNamePost(method, entityName, body)

Retrieves an entity collection

Retrieves an entity collection based on entity name

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
String method = "method_example"; // String | Tunnels the GET method over a POST request, allowing you to put the request in the body
String entityName = "entityName_example"; // String | Name of the entity
EntityCollectionRequestV2 body = new EntityCollectionRequestV2(); // EntityCollectionRequestV2 | Entity collection retrieval request
try {
    EntityCollectionResponseV2 result = apiInstance.v2EntityNamePost(method, entityName, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v2EntityNamePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **method** | **String**| Tunnels the GET method over a POST request, allowing you to put the request in the body | [enum: GET]
 **entityName** | **String**| Name of the entity | [enum: AnnotationJobExecution, Attribute, AttributeMapping, CGD, EntityMapping, EntityType, FileIngest, FileIngestJobExecution, FileMeta, FreemarkerTemplate, Gavin, GavinJobExecution, GeneNetworkScore, Group, GroupAuthority, GroupMember, ImportRun, IndexAction, IndexActionGroup, IndexJobExecution, JavaMailProperty, Language, MailSettings, MappingProject, MappingTarget, OMIM, Ontology, OntologyTerm, OntologyTermDynamicAnnotation, OntologyTermHit, OntologyTermNodePath, OntologyTermSynonym, Package, Script, ScriptParameter, ScriptType, SortaJobExecution, StaticContent, Tag, TermFrequency, Token, User, UserAuthority, age_units, app, biobank_size, biobanks, body_parts, cadd, clinvar, collection_types, collections, countries, dann, data_types, dataexplorer, directory, disease_types, exac, fitcon, genomicdata, gonl, hpo, i18nstrings, image_data_types, imaging_modality, material_types, networks, persons, sex_types, snpEff, temp_types, thousand_genomes]
 **body** | [**EntityCollectionRequestV2**](EntityCollectionRequestV2.md)| Entity collection retrieval request |

### Return type

[**EntityCollectionResponseV2**](EntityCollectionResponseV2.md)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

<a name="v2VersionGet"></a>
# **v2VersionGet**
> v2VersionGet()

Retrieves the MOLGENIS version

Retrieves the MOLGENIS version

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.VApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: token
ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("token");
token.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//token.setApiKeyPrefix("Token");

VApi apiInstance = new VApi();
try {
    apiInstance.v2VersionGet();
} catch (ApiException e) {
    System.err.println("Exception when calling VApi#v2VersionGet");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

[token](../README.md#token)

### HTTP request headers

 - **Content-Type**: application/json, application/x-www-form-urlencoded, multipart/form-data
 - **Accept**: application/json

