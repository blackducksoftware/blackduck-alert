package com.synopsys.integration.alert.channel.slack.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.slack.distribution.mock.MockProcessingAuditAccessor;
import com.synopsys.integration.alert.channel.util.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.SlackJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.rest.proxy.ProxyInfo;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class SlackDistributionEventReceiverTest {
    private SlackChannelKey slackChannelKey = new SlackChannelKey();

    @Test
    public void testSlackDistributionEventReceiver() throws IOException {
        Gson gson = new Gson();
        SlackJobDetailsAccessor slackJobDetailsAccessor = Mockito.mock(SlackJobDetailsAccessor.class);

        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        SlackChannelMessageFormatter slackChannelMessageFormatter = new SlackChannelMessageFormatter(markupEncoderUtil);
        SlackChannelMessageConverter slackChannelMessageConverter = new SlackChannelMessageConverter(slackChannelMessageFormatter);
        SlackChannelMessageSender slackChannelMessageSender = new SlackChannelMessageSender(createRestChannelUtility(), ChannelKeys.SLACK);
        SlackChannel slackChannel = new SlackChannel(slackChannelMessageConverter, slackChannelMessageSender);

        MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();

        //Create a sequence of 21 responses from the MockSlackServer where response #2 is a 429. All others are 200s.
        MockWebServer mockSlackServer = new MockWebServer();
        mockSlackServer.enqueue(new MockResponse().setResponseCode(200));
        mockSlackServer.enqueue(new MockResponse().setResponseCode(429));
        for (int i = 0; i < 20; i++) {
            mockSlackServer.enqueue(new MockResponse().setResponseCode(200));
        }
        mockSlackServer.start();
        String url = mockSlackServer.url("/").toString();

        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(null, url, "channelName", "userName");

        Mockito.when(slackJobDetailsAccessor.retrieveDetails(Mockito.any())).thenReturn(Optional.of(slackJobDetailsModel));

        SlackDistributionEventReceiver slackDistributionEventReceiver = new SlackDistributionEventReceiver(gson, processingAuditAccessor, slackJobDetailsAccessor, slackChannel, slackChannelKey);
        slackDistributionEventReceiver.handleEvent(createSlackDistributionEvent(Set.of(1L, 2L, 3L), createProviderMessageHolder(3)));
        slackDistributionEventReceiver.handleEvent(createSlackDistributionEvent(Set.of(4L, 5L, 6L), createProviderMessageHolder(3)));

        assertEquals(3, processingAuditAccessor.getSuccessfulIds().size());
        assertEquals(3, processingAuditAccessor.getFailureIds().size());
        assertTrue(processingAuditAccessor.getSuccessfulIds().contains(5L));
        assertTrue(processingAuditAccessor.getFailureIds().contains(2L));
    }

    private RestChannelUtility createRestChannelUtility() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager);
        return new RestChannelUtility(channelRestConnectionFactory);
    }

    private ProviderMessageHolder createProviderMessageHolder(int numberOfMessages) {
        List<ProjectMessage> projectMessages = new ArrayList<>();
        List<SimpleMessage> simpleMessages = new ArrayList<>();

        for (long i = 0; i < numberOfMessages; i++) {
            LinkableItem provider = new LinkableItem("Provider", "provider-" + i);
            ProviderDetails providerDetails = new ProviderDetails(i, provider);
            LinkableItem project = new LinkableItem("Project", "Common Project-" + i);

            ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(providerDetails, project, ProjectOperation.CREATE);
            projectMessages.add(projectMessage);

            LinkableItem details = new LinkableItem("Details", "detail-" + i);

            SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "summary", "description", List.of(details));
            simpleMessages.add(simpleMessage);
        }
        return new ProviderMessageHolder(projectMessages, simpleMessages);
    }

    private DistributionEvent createSlackDistributionEvent(Set<Long> notificationIds, ProviderMessageHolder providerMessages) {
        return new DistributionEvent(slackChannelKey, UUID.randomUUID(), notificationIds, providerMessages);
    }
}