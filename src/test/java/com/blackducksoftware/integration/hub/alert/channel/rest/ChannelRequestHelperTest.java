package com.blackducksoftware.integration.hub.alert.channel.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class ChannelRequestHelperTest {

    @Test
    public void testHttpUrlSegments() throws Exception {
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final List<String> segments = Arrays.asList("api", "path", "segment");
        final HttpUrl httpUrl = createHttpUrl("http://www.google.com");
        Mockito.when(restConnection.createHttpUrl(segments)).thenReturn(httpUrl);
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        final HttpUrl receivedHttpUrl = channelRequestHelper.createHttpUrl(segments);
        assertEquals(httpUrl, receivedHttpUrl);
    }

    @Test
    public void testHttpUrlSegmentsInvalid() {
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final List<String> segments = Arrays.asList("api", "path", "segment");
        Mockito.when(restConnection.createHttpUrl(segments)).thenThrow(new NullPointerException());
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        IntegrationException thrown = null;
        try {
            channelRequestHelper.createHttpUrl(segments);
        } catch (final IntegrationException ex) {
            thrown = ex;
        }
        assertNotNull(thrown);
        assertTrue(thrown.getMessage().startsWith("URL invalid: "));
    }

    @Test
    public void testHttpUrlString() throws Exception {
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final String url = "http://www.google.com";
        final HttpUrl httpUrl = createHttpUrl(url);
        Mockito.when(restConnection.createHttpUrl(url)).thenReturn(httpUrl);
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        final HttpUrl receivedHttpUrl = channelRequestHelper.createHttpUrl(url);
        assertEquals(httpUrl, receivedHttpUrl);
    }

    @Test
    public void testHttpUrlStringInvalid() {
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final String url = "htp:/ww.google.com";
        Mockito.when(restConnection.createHttpUrl(Mockito.anyString())).thenThrow(new NullPointerException());
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        IntegrationException thrown = null;
        try {
            channelRequestHelper.createHttpUrl(url);
        } catch (final IntegrationException ex) {
            thrown = ex;
        }
        assertNotNull(thrown);
        assertTrue(thrown.getMessage().startsWith("URL invalid: "));
    }

    @Test
    public void testCreateMessageRequest() throws Exception {
        final HttpUrl httpUrl = createHttpUrl("https://google.com");
        final Request request = createRequest();
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(restConnection.createPostRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(request);
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        final Request returnedRequest = channelRequestHelper.createMessageRequest(httpUrl, null, "");
        assertEquals(request, returnedRequest);
    }

    @Test
    public void testSendGenericRequestThrowInegrationException() throws Exception {
        final Request request = createRequest();
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(restConnection.createResponse(Mockito.any())).thenThrow(new IntegrationException());
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
        IntegrationException thrown = null;
        try {
            channelRequestHelper.sendGenericRequest(request);
        } catch (final IntegrationException ex) {
            thrown = ex;
        }
        assertNotNull(thrown);
    }

    private HttpUrl createHttpUrl(final String url) {
        final HttpUrl httpUrl = HttpUrl.parse(url).newBuilder().build();
        return httpUrl;
    }

    private Request createRequest() {
        final Request.Builder builder = new Request.Builder();
        builder.url("https://google.com");
        return builder.build();
    }

}
