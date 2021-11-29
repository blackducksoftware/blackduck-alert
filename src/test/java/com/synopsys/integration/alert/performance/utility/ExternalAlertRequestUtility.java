package com.synopsys.integration.alert.performance.utility;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.BodyContentConverter;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class ExternalAlertRequestUtility implements AlertRequestUtility {
    private final IntLogger intLogger;
    private final IntHttpClient client;
    private final String alertURL;

    public ExternalAlertRequestUtility(IntLogger intLogger, IntHttpClient client, String alertURL) {
        this.intLogger = intLogger;
        this.client = client;
        this.alertURL = alertURL;
    }

    public void loginToExternalAlert() throws IntegrationException {
        String loginBody = "{\"alertUsername\":\"sysadmin\",\"alertPassword\":\"blackduck\"}";
        BodyContent requestBody = new StringBodyContent(loginBody, BodyContentConverter.DEFAULT);
        Request.Builder requestBuilder = createRequestBuilder("/api/login");
        requestBuilder.method(HttpMethod.POST);
        requestBuilder.bodyContent(requestBody);

        Request request = requestBuilder.build();
        Response response = client.execute(request);
        if (response.isStatusCodeError()) {
            intLogger.error("Could not log into Alert.");
            response.throwExceptionForError();
        }

        String csrfToken = response.getHeaderValue("X-CSRF-TOKEN");
        String cookie = response.getHeaderValue("Set-Cookie");
        client.addCommonRequestHeader("X-CSRF-TOKEN", csrfToken);
        client.addCommonRequestHeader("Cookie", cookie);
        intLogger.info("Logged into Alert.");
    }

    @Override
    public String executeGetRequest(String path, String error) throws IntegrationException {
        return executeRequest(path, HttpMethod.GET, null, error);
    }

    @Override
    public String executePostRequest(String path, String requestBody, String error) throws IntegrationException {
        return executeRequest(path, HttpMethod.POST, requestBody, error);
    }

    @Override
    public String executePutRequest(String path, String requestBody, String error) throws IntegrationException {
        return executeRequest(path, HttpMethod.PUT, requestBody, error);
    }

    private String executeRequest(String path, HttpMethod httpMethod, String requestBody, String error) throws IntegrationException {
        BodyContent requestBodyContent = new StringBodyContent(requestBody, BodyContentConverter.DEFAULT);
        Request.Builder requestBuilder = createRequestBuilder(path);
        requestBuilder.method(httpMethod);
        if (null != requestBody) {
            requestBuilder.bodyContent(requestBodyContent);
        }
        Request request = requestBuilder.build();
        Response response = client.execute(request);
        if (response.isStatusCodeError()) {
            intLogger.error(error);
            response.throwExceptionForError();
        }
        return response.getContentString();
    }

    public Request.Builder createRequestBuilder(String path) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(new HttpUrl(alertURL + path));
        return requestBuilder;
    }
}
