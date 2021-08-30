package com.synopsys.integration.azure.boards.common.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class AzureHttpRequestCreatorTest {
    private static final String TEST_BASE_URL = "https://azure-boards-url";
    private static final String TEST_ENDPOINT = "/api/endpoint";

    private static final Gson GSON = new GsonBuilder().create();
    private static final AzureApiVersionAppender API_VERSION_APPENDER = new AzureApiVersionAppender();

    private static AzureHttpRequestCreator azureHttpRequestCreator;

    @BeforeAll
    public static void init() {
        ApacheHttpTransport httpTransport = new ApacheHttpTransport(ApacheHttpTransport.newDefaultHttpClient());
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        azureHttpRequestCreator = new AzureHttpRequestCreator(TEST_BASE_URL, GSON, requestFactory, API_VERSION_APPENDER);
    }

    @Test
    void createGetRequestTest() throws IOException {
        HttpRequest getRequest = azureHttpRequestCreator.createGetRequest(TEST_ENDPOINT);
        assertRequest(getRequest, "GET");
    }

    @Test
    void createDeleteRequestTest() throws IOException {
        HttpRequest deleteRequest = azureHttpRequestCreator.createDeleteRequest(TEST_ENDPOINT);
        assertRequest(deleteRequest, "DELETE");
    }

    @Test
    void createRequestWithDefaultHeadersNoBodyContentTest() throws IOException {
        String testHttpMethod = "PATCH";
        GenericUrl testUrl = new GenericUrl(TEST_BASE_URL + TEST_ENDPOINT);
        HttpRequest request = azureHttpRequestCreator.createRequestWithDefaultHeaders(testHttpMethod, testUrl, null);
        assertRequestWithDefaultHeaders(request, testHttpMethod, testUrl);
        assertNull(request.getContent(), "Expected the request body content to be null");
    }

    @Test
    void createRequestWithDefaultHeadersWithBodyContentTest() throws IOException {
        String testHttpMethod = "PATCH";
        GenericUrl testUrl = new GenericUrl(TEST_BASE_URL + TEST_ENDPOINT);

        JsonObject testBodyContent = new JsonObject();
        testBodyContent.add("field1", new JsonPrimitive("field-value"));

        HttpRequest request = azureHttpRequestCreator.createRequestWithDefaultHeaders(testHttpMethod, testUrl, testBodyContent);
        assertRequestWithDefaultHeaders(request, testHttpMethod, testUrl);
        assertNotNull(request.getContent(), "Expected the request body content not to be null");
    }

    @Test
    void createRequestTest() throws IOException {
        String testHttpMethod = "OPTIONS";
        GenericUrl testUrl = new GenericUrl(TEST_BASE_URL + TEST_ENDPOINT);
        String testAccept = "custom/accept";
        String testContentType = "other/content-type";

        HttpRequest request = azureHttpRequestCreator.createRequest(testHttpMethod, testUrl, null, testAccept, testContentType);
        assertRequestWithHeaders(request, testHttpMethod, testUrl, testAccept, testContentType);
    }

    @Test
    void createRequestUrlTest() {
        String baseUrlWithoutTrailingSlash = "https://without";
        String baseUrlWithTrailingSlash = "https://with/";

        String specWithLeadingSlash = "/spec";
        String specWithoutLeadingSlash = "spec";
        String specWithBaseUrl = baseUrlWithoutTrailingSlash + specWithLeadingSlash;
        String specWithVersion = API_VERSION_APPENDER.appendApiVersion("spec", "version");

        AzureHttpRequestCreator testRequestCreatorWithoutTrailingSlash = new AzureHttpRequestCreator(baseUrlWithoutTrailingSlash, null, null, API_VERSION_APPENDER);
        assertUrls(testRequestCreatorWithoutTrailingSlash, specWithLeadingSlash, specWithoutLeadingSlash, specWithBaseUrl, specWithVersion);

        AzureHttpRequestCreator testRequestCreatorWithTrailingSlash = new AzureHttpRequestCreator(baseUrlWithTrailingSlash, null, null, API_VERSION_APPENDER);
        assertUrls(testRequestCreatorWithTrailingSlash, specWithLeadingSlash, specWithoutLeadingSlash, specWithBaseUrl, specWithVersion);
    }

    @Test
    void createRequestBodyContentTest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("field1", new JsonPrimitive("a value"));
        jsonObject.add("field2", new JsonArray(0));
        jsonObject.add("field3", new JsonObject());

        String jsonObjectString = GSON.toJson(jsonObject);

        HttpContent bodyContent = azureHttpRequestCreator.createRequestBodyContent(jsonObject);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bodyContent.writeTo(outputStream);
        String bodyContentString = outputStream.toString();

        assertEquals(jsonObjectString, bodyContentString);
    }

    private static void assertRequest(HttpRequest request, String method) {
        assertEquals(method, request.getRequestMethod());
        assertTrue(request.getUrl().toString().startsWith(TEST_BASE_URL), "Expected request URL to start with the test URL");
        assertTrue(request.getUrl().toString().contains(TEST_ENDPOINT), "Expected request URL to contain the test endpoint");
    }

    private static void assertRequestWithDefaultHeaders(HttpRequest request, String method, GenericUrl testUrl) {
        assertRequestWithHeaders(request, method, testUrl, AzureHttpRequestCreator.CONTENT_TYPE_DEFAULT, AzureHttpRequestCreator.CONTENT_TYPE_DEFAULT);
    }

    private static void assertRequestWithHeaders(HttpRequest request, String method, GenericUrl testUrl, String accept, String contentType) {
        assertRequest(request, method);
        assertEquals(testUrl, request.getUrl());

        HttpHeaders headers = request.getHeaders();
        assertEquals(accept, headers.getAccept());
        assertEquals(contentType, headers.getContentType());
    }

    private static void assertUrls(AzureHttpRequestCreator requestCreator, String... specs) {
        for (String spec : specs) {
            assertUrl(requestCreator, spec);
        }
    }

    private static void assertUrl(AzureHttpRequestCreator requestCreator, String spec) {
        GenericUrl requestUrlFromSpecWithBaseUrl = requestCreator.createRequestUrl(spec);
        assertTrue(requestUrlFromSpecWithBaseUrl.toString().contains(spec));
    }

}
