package com.synopsys.integration.alert.channel.slack.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.slack.distribution.mock.MockProcessingAuditAccessor;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.SlackJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.rest.proxy.ProxyInfo;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class SlackDistributionEventHandlerTest {
    private static final Set<Long> FIRST_MESSAGE_NOTIFICATION_IDS = Set.of(1L, 2L, 3L);
    private static final Set<Long> SECOND_MESSAGE_NOTIFICATION_IDS = Set.of(4L, 5L, 6L);

    private final SlackChannelKey slackChannelKey = new SlackChannelKey();

    private SlackDistributionEventHandler distributionEventHandler;
    private final MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();
    private final MockWebServer mockSlackServer = new MockWebServer();

    private final Gson gson = new Gson();

    @BeforeEach
    public void init() throws IOException {
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        SlackChannelMessageFormatter slackChannelMessageFormatter = new SlackChannelMessageFormatter(markupEncoderUtil);
        SlackChannelMessageConverter slackChannelMessageConverter = new SlackChannelMessageConverter(slackChannelMessageFormatter);
        SlackChannelMessageSender slackChannelMessageSender = new SlackChannelMessageSender(ChannelKeys.SLACK, createConnectionFactory());
        SlackChannel slackChannel = new SlackChannel(slackChannelMessageConverter, slackChannelMessageSender);

        mockSlackServer.start();
        String url = mockSlackServer.url("/").toString();

        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(null, url, "channelName", "userName");

        SlackJobDetailsAccessor slackJobDetailsAccessor = jobId -> Optional.of(slackJobDetailsModel);

        distributionEventHandler = new SlackDistributionEventHandler(slackChannel, slackJobDetailsAccessor, processingAuditAccessor);
    }

    @AfterEach
    public void cleanup() throws IOException {
        mockSlackServer.shutdown();
    }

    @Test
    public void testNotificationsWithinMessageAfterFailureAreFailures() {
        mockSlackServer.enqueue(new MockResponse().setResponseCode(200));
        mockSlackServer.enqueue(new MockResponse().setResponseCode(429));

        assertEquals(0, mockSlackServer.getRequestCount());

        distributionEventHandler.handle(createSlackDistributionEvent(FIRST_MESSAGE_NOTIFICATION_IDS, createTwoMessages()));

        assertEquals(0, processingAuditAccessor.getSuccessfulIds().size());
        assertEquals(3, processingAuditAccessor.getFailureIds().size());
        assertTrue(processingAuditAccessor.getFailureIds().containsAll(FIRST_MESSAGE_NOTIFICATION_IDS));

        assertEquals(2, mockSlackServer.getRequestCount());
    }

    @Test
    public void testMessagesAfterFailureAreSuccesses() {
        mockSlackServer.enqueue(new MockResponse().setResponseCode(200));
        mockSlackServer.enqueue(new MockResponse().setResponseCode(429));
        mockSlackServer.enqueue(new MockResponse().setResponseCode(200));
        mockSlackServer.enqueue(new MockResponse().setResponseCode(200));

        assertEquals(0, mockSlackServer.getRequestCount());

        distributionEventHandler.handle(createSlackDistributionEvent(FIRST_MESSAGE_NOTIFICATION_IDS, createTwoMessages()));
        distributionEventHandler.handle(createSlackDistributionEvent(SECOND_MESSAGE_NOTIFICATION_IDS, createTwoMessages()));

        assertEquals(3, processingAuditAccessor.getSuccessfulIds().size());
        assertEquals(3, processingAuditAccessor.getFailureIds().size());
        assertTrue(processingAuditAccessor.getFailureIds().containsAll(FIRST_MESSAGE_NOTIFICATION_IDS));
        assertTrue(processingAuditAccessor.getSuccessfulIds().containsAll(SECOND_MESSAGE_NOTIFICATION_IDS));

        assertEquals(4, mockSlackServer.getRequestCount());
    }

    private ChannelRestConnectionFactory createConnectionFactory() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);
        return new ChannelRestConnectionFactory(testAlertProperties, proxyManager, gson);
    }

    private ProviderMessageHolder createTwoMessages() {
        ProviderDetails providerDetails = new ProviderDetails(1L, new LinkableItem("", ""));
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "", "", List.of());

        return new ProviderMessageHolder(List.of(), List.of(simpleMessage, simpleMessage));
    }

    private DistributionEvent createSlackDistributionEvent(Set<Long> notificationIds, ProviderMessageHolder providerMessages) {
        return new DistributionEvent(slackChannelKey, UUID.randomUUID(), "jobName", notificationIds, providerMessages);
    }

}
