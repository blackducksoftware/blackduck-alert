package com.synopsys.integration.alert.performance;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class AlertRequestUtility {
    private final IntLogger intLogger;
    private final IntHttpClient client;
    private final String alertURL;

    public AlertRequestUtility(IntLogger intLogger, IntHttpClient client, String alertURL) {
        this.intLogger = intLogger;
        this.client = client;
        this.alertURL = alertURL;
    }

    public Response executeGetRequest(String path, String error) throws IntegrationException {
        return executeRequest(path, HttpMethod.GET, null, error);
    }

    public Response executePostRequest(String path, BodyContent requestBody, String error) throws IntegrationException {
        return executeRequest(path, HttpMethod.POST, null, error);
    }

    public Response executePutRequest(String path, BodyContent requestBody, String error) throws IntegrationException {
        return executeRequest(path, HttpMethod.PUT, null, error);
    }

    private Response executeRequest(String path, HttpMethod httpMethod, BodyContent requestBody, String error) throws IntegrationException {
        Request.Builder requestBuilder = createRequestBuilder(path);
        requestBuilder.method(httpMethod);
        if (null != requestBody) {
            requestBuilder.bodyContent(requestBody);
        }
        Request request = requestBuilder.build();
        Response response = client.execute(request);
        if (response.isStatusCodeError()) {
            intLogger.error(error);
            response.throwExceptionForError();
        }
        return response;
    }

    public Request.Builder createRequestBuilder(String path) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(new HttpUrl(alertURL + path));
        return requestBuilder;
    }
}
