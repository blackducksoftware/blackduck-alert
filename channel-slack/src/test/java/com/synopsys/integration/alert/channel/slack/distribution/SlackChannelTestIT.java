/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel.slack.distribution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.slack.ChannelITTestAssertions;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.rest.proxy.ProxyInfo;

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
            properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME),
            properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME)
        );

        ChannelITTestAssertions.assertSendSimpleMessageSuccess(slackChannel, distributionDetails);
    }

    private ChannelRestConnectionFactory createConnectionFactory() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);
        return new ChannelRestConnectionFactory(testAlertProperties, proxyManager, gson);
    }

}
