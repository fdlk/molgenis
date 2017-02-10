# swagger-java-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-java-client</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "io.swagger:swagger-java-client:1.0.0"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/swagger-java-client-1.0.0.jar
* target/lib/*.jar

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java

import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.VApi;

import java.io.File;
import java.util.*;

public class VApiExample {

    public static void main(String[] args) {
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
    }
}

```

## Documentation for API Endpoints

All URIs are relative to *http://localhost:8080/api/*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*VApi* | [**v1LoginPost**](docs/VApi.md#v1LoginPost) | **POST** /v1/login | Logs into a MOLGENIS user account
*VApi* | [**v2CopyEntityNamePost**](docs/VApi.md#v2CopyEntityNamePost) | **POST** /v2/copy/{entity_name} | Creates a copy of an entity.
*VApi* | [**v2EntityNameGet**](docs/VApi.md#v2EntityNameGet) | **GET** /v2/{entity_name} | Retrieves an entity collection
*VApi* | [**v2EntityNameIdDelete**](docs/VApi.md#v2EntityNameIdDelete) | **DELETE** /v2/{entity_name}/{id} | Deletes an entity
*VApi* | [**v2EntityNameIdGet**](docs/VApi.md#v2EntityNameIdGet) | **GET** /v2/{entity_name}/{id} | Retrieves an entity
*VApi* | [**v2EntityNameMetaAttributeNameGet**](docs/VApi.md#v2EntityNameMetaAttributeNameGet) | **GET** /v2/{entity_name}/meta/{attribute_name} | Retrieve attribute metadata
*VApi* | [**v2EntityNamePost**](docs/VApi.md#v2EntityNamePost) | **POST** /v2/{entity_name} | Retrieves an entity collection
*VApi* | [**v2VersionGet**](docs/VApi.md#v2VersionGet) | **GET** /v2/version | Retrieves the MOLGENIS version


## Documentation for Models

 - [AttributeResponseV2](docs/AttributeResponseV2.md)
 - [CopyEntityRequest](docs/CopyEntityRequest.md)
 - [EntityCollectionRequestV2](docs/EntityCollectionRequestV2.md)
 - [EntityCollectionResponseV2](docs/EntityCollectionResponseV2.md)
 - [EntityTypeResponseV2](docs/EntityTypeResponseV2.md)
 - [Error](docs/Error.md)
 - [ErrorMessage](docs/ErrorMessage.md)
 - [ErrorMessageResponse](docs/ErrorMessageResponse.md)
 - [LoginRequest](docs/LoginRequest.md)
 - [LoginResponse](docs/LoginResponse.md)
 - [Range](docs/Range.md)


## Documentation for Authorization

Authentication schemes defined for the API:
### token

- **Type**: API key
- **API key parameter name**: x-molgenis-token
- **Location**: HTTP header


## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issue.

## Author



