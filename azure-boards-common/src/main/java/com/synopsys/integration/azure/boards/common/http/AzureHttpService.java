package com.synopsys.integration.azure.boards.common.http;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Proxy;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;

public class AzureHttpService {
    public static final String AZURE_API_VERSION = "5.1";

    private final String baseUrl;
    private final HttpRequestFactory httpRequestFactory;
    private final Gson gson;

    public static AzureHttpService withCredential(String baseUrl, Credential oAuthCredential, Gson gson) {
        return withCredential(baseUrl, Proxy.NO_PROXY, oAuthCredential, gson);
    }

    public static AzureHttpService withCredential(String baseUrl, Proxy proxy, Credential oAuthCredential, Gson gson) {
        NetHttpTransport netHttpTransport = new NetHttpTransport.Builder()
                                                .setProxy(proxy)
                                                .build();
        return new AzureHttpService(baseUrl, netHttpTransport.createRequestFactory(oAuthCredential), gson);
    }

    public AzureHttpService(String baseUrl, HttpRequestFactory httpRequestFactory, Gson gson) {
        this.baseUrl = baseUrl;
        this.httpRequestFactory = httpRequestFactory;
        this.gson = gson;
    }

    public HttpResponse getResponse(String urlEndpoint) throws IOException {
        GenericUrl url = new GenericUrl(baseUrl + urlEndpoint);
        HttpRequest request = httpRequestFactory.buildGetRequest(url);
        HttpHeaders headers = request.getHeaders();
        headers.setAccept(acceptHeader());
        headers.setContentType(contentType());
        return request.execute();
    }

    public <T> T get(String urlEndpoint, Type responseType) throws HttpServiceException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = getResponse(urlEndpoint);
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

    public HttpResponse post(String urlEndpoint, Object requestBodyObject) throws IOException {
        GenericUrl url = new GenericUrl(baseUrl + urlEndpoint);
        HttpRequest postRequest = buildPostRequest(url, requestBodyObject);
        return postRequest.execute();
    }

    public <T> T post(String urlEndpoint, Object requestBodyObject, Class<T> responseClass) throws HttpServiceException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = post(urlEndpoint, requestBodyObject);
            if (!httpResponse.isSuccessStatusCode()) {
                throw new HttpServiceException(httpResponse.getStatusMessage(), httpResponse.getStatusCode());
            }
            return parseResponse(httpResponse, responseClass);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        } finally {
            disconnectResponse(httpResponse);
        }
    }

    protected String acceptHeader() {
        return "application/json";
    }

    protected String contentType() {
        return "application/json";
    }

    protected HttpRequest buildPostRequest(GenericUrl url, Object requestBodyObject) throws IOException {
        HttpContent requestContent = buildPostRequestContent(requestBodyObject);
        HttpRequest postRequest = httpRequestFactory.buildPostRequest(url, requestContent);
        postRequest.getHeaders().setAccept(acceptHeader());
        postRequest.getHeaders().setContentType(contentType());
        return postRequest;
    }

    protected HttpContent buildPostRequestContent(Object requestBodyObject) {
        String objectJson = gson.toJson(requestBodyObject);
        return ByteArrayContent.fromString(null, objectJson);
    }

    protected void disconnectResponse(HttpResponse response) throws HttpServiceException {
        if (response == null) {
            return;
        }
        try {
            response.disconnect();
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

    protected <T> T parseResponse(HttpResponse response, Type responseType) throws IOException {
        String responseString = response.parseAsString();
        return gson.fromJson(responseString, responseType);
    }

}
