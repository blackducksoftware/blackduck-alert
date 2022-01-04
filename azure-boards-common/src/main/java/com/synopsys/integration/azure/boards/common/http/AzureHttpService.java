/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.http;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.gson.Gson;
import com.synopsys.integration.function.ThrowingSupplier;

public class AzureHttpService {
    private final Gson gson;
    private final AzureHttpRequestCreator requestCreator;

    public AzureHttpService(Gson gson, AzureHttpRequestCreator requestCreator) {
        this.gson = gson;
        this.requestCreator = requestCreator;
    }

    public <T> T get(String urlEndpoint, Type responseType) throws HttpServiceException {
        return executeRequestAndParseResponse(() -> requestCreator.createGetRequest(urlEndpoint), responseType);
    }

    public <T> T post(String urlEndpoint, Object requestBodyObject, Type responseType) throws HttpServiceException {
        return post(urlEndpoint, requestBodyObject, responseType, AzureHttpRequestCreator.CONTENT_TYPE_DEFAULT);
    }

    public <T> T post(String urlEndpoint, Object requestBodyObject, Type responseType, String contentType) throws HttpServiceException {
        GenericUrl url = requestCreator.createRequestUrl(urlEndpoint);
        return executeRequestAndParseResponse(
            () -> requestCreator.createRequest(HttpMethods.POST, url, requestBodyObject, AzureHttpRequestCreator.CONTENT_TYPE_DEFAULT, contentType),
            responseType
        );
    }

    public <T> T delete(String urlEndpoint, Type responseType) throws HttpServiceException {
        return executeRequestAndParseResponse(() -> requestCreator.createDeleteRequest(urlEndpoint), responseType);
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

}
