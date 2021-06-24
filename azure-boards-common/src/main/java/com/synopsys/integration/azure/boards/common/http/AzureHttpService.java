/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.http;

import static com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender.AZURE_API_VERSION_QUERY_PARAM_NAME;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.gson.Gson;
import com.synopsys.integration.function.ThrowingSupplier;

public class AzureHttpService {
    public static final String CONTENT_TYPE_DEFAULT = "application/json";
    public static final String CONTENT_TYPE_JSON_PATCH = "application/json-patch+json";

    private final String baseUrl;
    private final HttpRequestFactory httpRequestFactory;
    private final Gson gson;
    private final AzureApiVersionAppender azureApiVersionAppender;

    public AzureHttpService(String baseUrl, HttpRequestFactory httpRequestFactory, Gson gson, AzureApiVersionAppender azureApiVersionAppender) {
        this.baseUrl = sanitizeUrl(baseUrl);
        this.httpRequestFactory = httpRequestFactory;
        this.gson = gson;
        this.azureApiVersionAppender = azureApiVersionAppender;
    }

    public <T> T get(String urlEndpoint, Type responseType) throws HttpServiceException {
        return executeRequestAndParseResponse(() -> createGetRequest(urlEndpoint), responseType);
    }

    public <T> T post(String urlEndpoint, Object requestBodyObject, Type responseType) throws HttpServiceException {
        return post(urlEndpoint, requestBodyObject, responseType, contentType());
    }

    public <T> T post(String urlEndpoint, Object requestBodyObject, Type responseType, String contentType) throws HttpServiceException {
        GenericUrl url = constructRequestUrl(urlEndpoint);
        return executeRequestAndParseResponse(
            () -> buildRequest(HttpMethods.POST, url, requestBodyObject, acceptHeader(), contentType),
            responseType
        );
    }

    public <T> T delete(String urlEndpoint, Type responseType) throws HttpServiceException {
        return executeRequestAndParseResponse(() -> createDeleteRequest(urlEndpoint), responseType);
    }

    public <T> T executeRequestAndParseResponse(HttpRequest httpRequest, Type responseType) throws HttpServiceException {
        return executeRequestAndParseResponse(() -> httpRequest, responseType);
    }

    public <T> T executeRequestAndParseResponse(ThrowingSupplier<HttpRequest, IOException> httpRequestSupplier, Type responseType) throws HttpServiceException {
        HttpResponse httpResponse = null;
        try {
            HttpRequest httpRequest = httpRequestSupplier.get();
            httpResponse = httpRequest.execute();
            if (!httpResponse.isSuccessStatusCode()) {
                throw new HttpServiceException(httpResponse.getStatusMessage(), httpResponse.getStatusCode());
            }
            return parseResponse(httpResponse, responseType);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        } finally {
            disconnectResponse(httpResponse);
        }
    }

    public HttpRequest buildRequestWithDefaultHeaders(String httpMethod, GenericUrl url, Object requestBodyObject) throws IOException {
        return buildRequest(httpMethod, url, requestBodyObject, acceptHeader(), contentType());
    }

    public HttpRequest buildRequest(String httpMethod, GenericUrl url, Object requestBodyObject, String acceptHeader, String contentType) throws IOException {
        HttpContent requestContent = requestBodyObject != null ? buildPostRequestContent(requestBodyObject) : null;
        HttpRequest postRequest = httpRequestFactory.buildRequest(httpMethod, url, requestContent);
        postRequest.getHeaders().setAccept(acceptHeader);
        postRequest.getHeaders().setContentType(contentType);
        return postRequest;
    }

    public GenericUrl constructRequestUrl(String spec) {
        StringBuilder requestUrlBuilder = new StringBuilder();

        if (!StringUtils.startsWith(spec, baseUrl)) {
            requestUrlBuilder.append(baseUrl);
        }

        if (!StringUtils.startsWith(spec, "/")) {
            requestUrlBuilder.append("/");
        }

        if (!StringUtils.contains(spec, AZURE_API_VERSION_QUERY_PARAM_NAME)) {
            spec = azureApiVersionAppender.appendApiVersion5_1(spec);
        }
        requestUrlBuilder.append(spec);

        return new GenericUrl(requestUrlBuilder.toString());
    }

    protected String acceptHeader() {
        return CONTENT_TYPE_DEFAULT;
    }

    protected String contentType() {
        return CONTENT_TYPE_DEFAULT;
    }

    private HttpContent buildPostRequestContent(Object requestBodyObject) {
        String objectJson = gson.toJson(requestBodyObject);
        return ByteArrayContent.fromString(null, objectJson);
    }

    private void disconnectResponse(HttpResponse response) throws HttpServiceException {
        if (response == null) {
            return;
        }
        try {
            response.disconnect();
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

    private <T> T parseResponse(HttpResponse response, Type responseType) throws IOException {
        String responseString = response.parseAsString();
        return gson.fromJson(responseString, responseType);
    }

    private HttpRequest createGetRequest(String urlEndpoint) throws IOException {
        GenericUrl url = constructRequestUrl(urlEndpoint);
        return buildRequestWithDefaultHeaders(HttpMethods.GET, url, null);
    }

    private HttpRequest createDeleteRequest(String urlEndpoint) throws IOException {
        GenericUrl url = constructRequestUrl(urlEndpoint);
        return buildRequestWithDefaultHeaders(HttpMethods.DELETE, url, null);
    }

    private static String sanitizeUrl(String url) {
        url = StringUtils.trim(url);
        if (StringUtils.endsWith(url, "/")) {
            return StringUtils.chomp(url);
        }
        return url;
    }

}
