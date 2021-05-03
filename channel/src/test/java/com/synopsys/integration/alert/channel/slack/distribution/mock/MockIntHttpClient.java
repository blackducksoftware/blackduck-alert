package com.synopsys.integration.alert.channel.slack.distribution.mock;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.DefaultResponse;
import com.synopsys.integration.rest.response.Response;

public class MockIntHttpClient extends IntHttpClient {

    public MockIntHttpClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        super(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo);
    }

    //could possibly do this at a higher level
    @Override
    public Response execute(Request request) throws IntegrationException {
        //return execute(request, new BasicHttpContext());

        HttpUriRequest httpUriRequest = createHttpUriRequest(request);
        CloseableHttpClient closeableHttpClient = getClientBuilder().build();

        //CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpUriRequest);
        CloseableHttpResponse closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class); //TODO perhaps it would be better to implement this than mockito

        Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(RestConstants.OK_200);

        return new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse);
    }

}

//may need to make a new class that extends Response to return here
