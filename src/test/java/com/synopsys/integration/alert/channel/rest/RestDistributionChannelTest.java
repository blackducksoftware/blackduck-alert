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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.digest.model.DigestModel;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;

public class RestDistributionChannelTest extends ChannelTest {
    @Test
    public void sendMessageFailureTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final Gson gson = new Gson();
        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity>(gson, testAlertProperties,
                hubProperties, null, null,
                null, null,
                channelRestConnectionFactory, contentConverter) {

            @Override
            public String getApiUrl(final GlobalChannelConfigEntity entity) {
                return null;
            }

            @Override
            public List<Request> createRequests(final DistributionChannelConfigEntity config, final GlobalChannelConfigEntity globalConfig, final ChannelEvent event) throws AlertException {
                return Arrays.asList(new Request.Builder().uri("http://google.com").build());
            }
        };
        final DigestModel digestModel = new DigestModel(createProjectData("Rest channel test"));
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(SlackChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), 1L, 1L);
        final SlackDistributionConfigEntity config = new SlackDistributionConfigEntity("more garbage", "garbage", "garbage");
        Exception thrownException = null;
        try {
            restChannel.sendAuditedMessage(event, config);
        } catch (final IntegrationException ex) {
            thrownException = ex;
        }

        assertNotNull(thrownException);
    }

    @Test
    public void testCreateMessageRequest() {
        final Request request = createRequest();
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final Gson gson = new Gson();
        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity>(gson, testAlertProperties,
                hubProperties, null, null,
                null, null,
                channelRestConnectionFactory, contentConverter) {

            @Override
            public String getApiUrl(final GlobalChannelConfigEntity entity) {
                return null;
            }

            @Override
            public List<Request> createRequests(final DistributionChannelConfigEntity config, final GlobalChannelConfigEntity globalConfig, final ChannelEvent event) throws AlertException {
                return Arrays.asList(request);
            }
        };
        final Request returnedRequest = restChannel.createPostMessageRequest("https://google.com", null, "{}");

        assertEquals(request.getUri(), returnedRequest.getUri());
        assertEquals(request.getMethod(), returnedRequest.getMethod());
        assertEquals(request.getMimeType(), returnedRequest.getMimeType());
        assertEquals(request.getQueryParameters(), returnedRequest.getQueryParameters());
        assertEquals(request.getAdditionalHeaders(), returnedRequest.getAdditionalHeaders());
        assertEquals(request.getBodyEncoding(), returnedRequest.getBodyEncoding());
        assertEquals(((StringBodyContent) request.getBodyContent()).getBodyContent(), ((StringBodyContent) returnedRequest.getBodyContent()).getBodyContent());
    }

    @Test
    public void testCreateQueryParametersMessageRequest() {
        final Request request = createRequest();
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final Gson gson = new Gson();
        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity>(gson, testAlertProperties,
                hubProperties, null, null,
                null, null,
                channelRestConnectionFactory, contentConverter) {

            @Override
            public String getApiUrl(final GlobalChannelConfigEntity entity) {
                return null;
            }

            @Override
            public List<Request> createRequests(final DistributionChannelConfigEntity config, final GlobalChannelConfigEntity globalConfig, final ChannelEvent event) throws AlertException {
                return Arrays.asList(request);
            }
        };
        final Request returnedRequest = restChannel.createPostMessageRequest("https://google.com", null, null, "{}");

        assertEquals(request.getUri(), returnedRequest.getUri());
        assertEquals(request.getMethod(), returnedRequest.getMethod());
        assertEquals(request.getMimeType(), returnedRequest.getMimeType());
        assertEquals(request.getQueryParameters(), returnedRequest.getQueryParameters());
        assertEquals(request.getAdditionalHeaders(), returnedRequest.getAdditionalHeaders());
        assertEquals(request.getBodyEncoding(), returnedRequest.getBodyEncoding());
        assertEquals(((StringBodyContent) request.getBodyContent()).getBodyContent(), ((StringBodyContent) returnedRequest.getBodyContent()).getBodyContent());
    }

    @Test
    public void testSendGenericRequestThrowInegrationException() throws Exception {
        final Request request = createRequest();
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(restConnection.executeRequest(request)).thenThrow(new IntegrationException());
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final Gson gson = new Gson();
        final RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity> restChannel = new RestDistributionChannel<GlobalChannelConfigEntity, DistributionChannelConfigEntity>(gson, testAlertProperties,
                hubProperties, null, null,
                null, null,
                channelRestConnectionFactory, contentConverter) {

            @Override
            public String getApiUrl(final GlobalChannelConfigEntity entity) {
                return null;
            }

            @Override
            public List<Request> createRequests(final DistributionChannelConfigEntity config, final GlobalChannelConfigEntity globalConfig, final ChannelEvent event) throws AlertException {
                return Arrays.asList(request);
            }
        };
        IntegrationException thrown = null;
        try {
            restChannel.sendGenericRequest(restConnection, request);
        } catch (final IntegrationException ex) {
            thrown = ex;
        }
        assertNotNull(thrown);
    }

    private Request createRequest() {
        Request.Builder builder = new Request.Builder();
        builder = builder.uri("https://google.com").method(HttpMethod.POST).bodyContent(new StringBodyContent("{}"));
        return builder.build();
    }
}
