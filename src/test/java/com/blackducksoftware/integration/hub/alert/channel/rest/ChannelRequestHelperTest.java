package com.blackducksoftware.integration.hub.alert.channel.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.BodyContent;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class ChannelRequestHelperTest {

    @Test
    public void testCreateMessageRequest() throws Exception {
        final Request request = createRequest();
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        final Request returnedRequest = channelRequestHelper.createPostMessageRequest("https://google.com", null, "{}");

        assertEquals(request.getUri(), returnedRequest.getUri());
        assertEquals(request.getMethod(), returnedRequest.getMethod());
        assertEquals(request.getMimeType(), returnedRequest.getMimeType());
        assertEquals(request.getQueryParameters(), returnedRequest.getQueryParameters());
        assertEquals(request.getAdditionalHeaders(), returnedRequest.getAdditionalHeaders());
        assertEquals(request.getBodyEncoding(), returnedRequest.getBodyEncoding());
        assertEquals(request.getBodyContent().getBodyContent(), returnedRequest.getBodyContent().getBodyContent());
    }

    @Test
    public void testCreateQueryParametersMessageRequest() throws Exception {
        final Request request = createRequest();
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        final Request returnedRequest = channelRequestHelper.createPostMessageRequest("https://google.com", null, null, "{}");

        assertEquals(request.getUri(), returnedRequest.getUri());
        assertEquals(request.getMethod(), returnedRequest.getMethod());
        assertEquals(request.getMimeType(), returnedRequest.getMimeType());
        assertEquals(request.getQueryParameters(), returnedRequest.getQueryParameters());
        assertEquals(request.getAdditionalHeaders(), returnedRequest.getAdditionalHeaders());
        assertEquals(request.getBodyEncoding(), returnedRequest.getBodyEncoding());
        assertEquals(request.getBodyContent().getBodyContent(), returnedRequest.getBodyContent().getBodyContent());
    }

    @Test
    public void testSendGenericRequestThrowInegrationException() throws Exception {
        final Request request = createRequest();
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(restConnection.executeRequest(request)).thenThrow(new IntegrationException());
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        IntegrationException thrown = null;
        try {
            channelRequestHelper.sendGenericRequest(request);
        } catch (final IntegrationException ex) {
            thrown = ex;
        }
        assertNotNull(thrown);
    }

    private Request createRequest() {
        Request.Builder builder = new Request.Builder();
        builder = builder.uri("https://google.com").method(HttpMethod.POST).bodyContent(new BodyContent("{}"));
        return builder.build();
    }

}
