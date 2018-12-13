/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel.rest;

import org.junit.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class RestDistributionChannelTest extends ChannelTest {
    // TODO figure out what this was supposed to be testing:
    //  @Test
    public void sendMessageFailureTest() {
        //        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        //        final BlackDuckProperties blackDuckProperties = new TestBlackDuckProperties(testAlertProperties);
        //        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        //        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        //        final Gson gson = new Gson();
        //        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity, DistributionEvent> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity, DistributionEvent>(
        //                gson, testAlertProperties, blackDuckProperties, auditUtility, null, null, channelRestConnectionFactory) {
        //
        //            @Override
        //            public String getDistributionType() {
        //                return null;
        //            }
        //
        //            @Override
        //            public String getApiUrl(final GlobalChannelConfigEntity entity) {
        //                return null;
        //            }
        //
        //            @Override
        //            public List<Request> createRequests(final GlobalChannelConfigEntity globalConfig, final DistributionEvent event) throws IntegrationException {
        //                return Arrays.asList(new Request.Builder().uri("http://google.com").build());
        //            }
        //        }
        //        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        //        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        //        final SlackChannelEvent event = new SlackChannelEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT",
        //                content, 1L, "more garbage", "garbage", "garbage");
        //        Exception thrownException = null;
        //        try {
        //            restChannel.sendAuditedMessage(event);
        //        } catch (final IntegrationException ex) {
        //            thrownException = ex;
        //        }
        //
        //        assertNotNull(thrownException);
    }

    @Test
    public void testCreateMessageRequest() {
        //        final Request request = createRequest();
        //        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        //        final BlackDuckProperties blackDuckProperties = new TestBlackDuckProperties(testAlertProperties);
        //        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        //        final Gson gson = new Gson();
        //        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity, DistributionEvent> restChannel = createMockRestDistributionChannel(gson, testAlertProperties, blackDuckProperties,
        //                channelRestConnectionFactory, request);
        //        final Request returnedRequest = restChannel.createPostMessageRequest("https://google.com", null, "{}");
        //
        //        assertEquals(request.getUri(), returnedRequest.getUri());
        //        assertEquals(request.getMethod(), returnedRequest.getMethod());
        //        assertEquals(request.getMimeType(), returnedRequest.getMimeType());
        //        assertEquals(request.getQueryParameters(), returnedRequest.getQueryParameters());
        //        assertEquals(request.getAdditionalHeaders(), returnedRequest.getAdditionalHeaders());
        //        assertEquals(request.getBodyEncoding(), returnedRequest.getBodyEncoding());
        //        assertEquals(((StringBodyContent) request.getBodyContent()).getBodyContent(), ((StringBodyContent) returnedRequest.getBodyContent()).getBodyContent());
    }

    @Test
    public void testCreateQueryParametersMessageRequest() {
        //        final Request request = createRequest();
        //        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        //        final BlackDuckProperties blackDuckProperties = new TestBlackDuckProperties(testAlertProperties);
        //        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        //        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity, DistributionEvent> restChannel = createMockRestDistributionChannel(gson, testAlertProperties, blackDuckProperties,
        //                channelRestConnectionFactory, request);
        //        final Request returnedRequest = restChannel.createPostMessageRequest("https://google.com", null, null, "{}");
        //
        //        assertEquals(request.getUri(), returnedRequest.getUri());
        //        assertEquals(request.getMethod(), returnedRequest.getMethod());
        //        assertEquals(request.getMimeType(), returnedRequest.getMimeType());
        //        assertEquals(request.getQueryParameters(), returnedRequest.getQueryParameters());
        //        assertEquals(request.getAdditionalHeaders(), returnedRequest.getAdditionalHeaders());
        //        assertEquals(request.getBodyEncoding(), returnedRequest.getBodyEncoding());
        //        assertEquals(((StringBodyContent) request.getBodyContent()).getBodyContent(), ((StringBodyContent) returnedRequest.getBodyContent()).getBodyContent());
    }

    @Test
    public void testSendGenericRequestThrowInegrationException() throws Exception {
        //        final Request request = createRequest();
        //        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        //        Mockito.when(restConnection.execute(request)).thenThrow(new IntegrationException());
        //        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        //        final BlackDuckProperties blackDuckProperties = new TestBlackDuckProperties(testAlertProperties);
        //        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        //        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity, DistributionEvent> restChannel = createMockRestDistributionChannel(gson, testAlertProperties, blackDuckProperties,
        //                channelRestConnectionFactory, request);
        //        IntegrationException thrown = null;
        //        try {
        //            restChannel.sendGenericRequest(restConnection, request);
        //        } catch (final IntegrationException ex) {
        //            thrown = ex;
        //        }
        //        assertNotNull(thrown);
    }

    private Request createRequest() {
        Request.Builder builder = new Request.Builder();
        builder = builder.uri("https://google.com").method(HttpMethod.POST).bodyContent(new StringBodyContent("{}"));
        return builder.build();
    }

    private RestDistributionChannel createMockRestDistributionChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties,
            final ChannelRestConnectionFactory channelRestConnectionFactory, final Request request) {
        //        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity, DistributionEvent> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity, DistributionEvent>(
        //                gson,
        //                alertProperties, blackDuckProperties, null, null,
        //                null, channelRestConnectionFactory) {
        //
        //            @Override
        //            public String getDistributionType() {
        //                return null;
        //            }
        //
        //            @Override
        //            public String getApiUrl(final GlobalChannelConfigEntity entity) {
        //                return null;
        //            }
        //
        //            @Override
        //            public List<Request> createRequests(final GlobalChannelConfigEntity globalConfig, final DistributionEvent event) throws AlertException {
        //                return Arrays.asList(request);
        //            }
        //        };
        //
        //        return restChannel;
        return null;
    }
}
