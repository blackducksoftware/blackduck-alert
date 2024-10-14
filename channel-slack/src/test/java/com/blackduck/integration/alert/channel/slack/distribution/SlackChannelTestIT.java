/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.slack.distribution;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.certificates.AlertSSLContextManager;
import com.blackduck.integration.alert.api.channel.rest.ChannelRestConnectionFactory;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.channel.slack.ChannelITTestAssertions;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.common.util.MarkupEncoderUtil;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

class SlackChannelTestIT {
    private Gson gson;
    private TestProperties properties;
    private EventManager eventManager;
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        gson = BlackDuckServicesFactory.createDefaultGson();
        properties = new TestProperties();
        eventManager = Mockito.mock(EventManager.class);
        executingJobManager = Mockito.mock(ExecutingJobManager.class);
    }

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    void sendMessageTestIT() {
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        SlackChannelMessageFormatter slackChannelMessageFormatter = new SlackChannelMessageFormatter(markupEncoderUtil);
        SlackChannelMessageConverter slackChannelMessageConverter = new SlackChannelMessageConverter(slackChannelMessageFormatter);

        ChannelRestConnectionFactory connectionFactory = createConnectionFactory();
        SlackChannelMessageSender slackChannelMessageSender = new SlackChannelMessageSender(ChannelKeys.SLACK, connectionFactory);

        SlackChannel slackChannel = new SlackChannel(slackChannelMessageConverter, slackChannelMessageSender, eventManager, executingJobManager);

        SlackJobDetailsModel distributionDetails = new SlackJobDetailsModel(
            null,
            properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK),
            properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME)
        );

        ChannelITTestAssertions.assertSendSimpleMessageSuccess(slackChannel, distributionDetails);
    }

    private ChannelRestConnectionFactory createConnectionFactory() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        AlertSSLContextManager alertSSLContextManager = Mockito.mock(AlertSSLContextManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);
        Mockito.when(alertSSLContextManager.buildWithClientCertificate()).thenReturn(Optional.empty());
        return new ChannelRestConnectionFactory(testAlertProperties, proxyManager, gson, alertSSLContextManager);
    }

}
