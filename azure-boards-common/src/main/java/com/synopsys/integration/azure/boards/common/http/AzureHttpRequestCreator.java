/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.http;

import static com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender.AZURE_API_VERSION_QUERY_PARAM_NAME;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.gson.Gson;

public class AzureHttpRequestCreator {
    public static final String CONTENT_TYPE_DEFAULT = "application/json";
    public static final String CONTENT_TYPE_JSON_PATCH = "application/json-patch+json";

    private final String baseUrl;
    private final Gson gson;
    private final HttpRequestFactory httpRequestFactory;
    private final AzureApiVersionAppender azureApiVersionAppender;

    public AzureHttpRequestCreator(String baseUrl, Gson gson, HttpRequestFactory httpRequestFactory, AzureApiVersionAppender azureApiVersionAppender) {
        this.baseUrl = sanitizeUrl(baseUrl);
        this.gson = gson;
        this.httpRequestFactory = httpRequestFactory;
        this.azureApiVersionAppender = azureApiVersionAppender;
    }

    public HttpRequest createGetRequest(String urlEndpoint) throws IOException {
        GenericUrl url = createRequestUrl(urlEndpoint);
        return createRequestWithDefaultHeaders(HttpMethods.GET, url, null);
    }

    public HttpRequest createDeleteRequest(String urlEndpoint) throws IOException {
        GenericUrl url = createRequestUrl(urlEndpoint);
        return createRequestWithDefaultHeaders(HttpMethods.DELETE, url, null);
    }

    public HttpRequest createRequestWithDefaultHeaders(String httpMethod, GenericUrl url, @Nullable Object requestBodyObject) throws IOException {
        return createRequest(httpMethod, url, requestBodyObject, CONTENT_TYPE_DEFAULT, CONTENT_TYPE_DEFAULT);
    }

    public HttpRequest createRequest(String httpMethod, GenericUrl url, @Nullable Object requestBodyObject, String acceptHeader, String contentType) throws IOException {
        HttpContent requestContent = requestBodyObject != null ? createRequestBodyContent(requestBodyObject) : null;
        HttpRequest postRequest = httpRequestFactory.buildRequest(httpMethod, url, requestContent);
        postRequest.getHeaders().setAccept(acceptHeader);
        postRequest.getHeaders().setContentType(contentType);
        return postRequest;
    }

    public GenericUrl createRequestUrl(String spec) {
        StringBuilder requestUrlBuilder = new StringBuilder();

        if (!StringUtils.startsWith(spec, baseUrl)) {
            requestUrlBuilder.append(baseUrl);
            if (!StringUtils.startsWith(spec, "/")) {
                requestUrlBuilder.append("/");
            }
        }

        if (!StringUtils.contains(spec, AZURE_API_VERSION_QUERY_PARAM_NAME)) {
            spec = azureApiVersionAppender.appendApiVersion5_1(spec);
        }
        requestUrlBuilder.append(spec);

        return new GenericUrl(requestUrlBuilder.toString());
    }

    public HttpContent createRequestBodyContent(Object requestBodyObject) {
        String objectJson = gson.toJson(requestBodyObject);
        return ByteArrayContent.fromString(null, objectJson);
    }

    private static String sanitizeUrl(String url) {
        url = StringUtils.trim(url);
        url = StringUtils.removeEnd(url, "/");
        return url;
    }

}
