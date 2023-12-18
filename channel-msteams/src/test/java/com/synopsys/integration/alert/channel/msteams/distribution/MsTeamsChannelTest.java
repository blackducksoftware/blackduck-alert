package com.synopsys.integration.alert.channel.msteams.distribution;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.rest.proxy.ProxyInfo;

class MsTeamsChannelTest {
    private static final LinkableItem TEST_PROVIDER = new LinkableItem("Test Provider Label", "Test Provider Config Name");
    private static final ProviderDetails TEST_PROVIDER_DETAILS = new ProviderDetails(0L, TEST_PROVIDER);

    private static final LinkableItem TEST_DETAIL_1 = new LinkableItem("Detail 1 Label", "Test Detail Value");
    private static final LinkableItem TEST_DETAIL_2 = new LinkableItem("Detail 2 Label", "Test Detail Value (with URL)", "https://google.com");
    private static final List<LinkableItem> TEST_DETAILS = List.of(TEST_DETAIL_1, TEST_DETAIL_2);

    private static final String SIMPLE_MESSAGE_CLASS_NAME = SimpleMessage.class.getSimpleName();
    private static final String TEST_SUMMARY_VALUE = "Test summary field of " + SIMPLE_MESSAGE_CLASS_NAME;
    private static final String TEST_DESCRIPTION_VALUE = "Test description field of " + SIMPLE_MESSAGE_CLASS_NAME;
    private static final SimpleMessage TEST_SIMPLE_MESSAGE = SimpleMessage.original(TEST_PROVIDER_DETAILS, TEST_SUMMARY_VALUE, TEST_DESCRIPTION_VALUE, TEST_DETAILS);
    private static final ProviderMessageHolder TEST_MESSAGE_HOLDER = new ProviderMessageHolder(List.of(), List.of(TEST_SIMPLE_MESSAGE));

    protected Gson gson;
    protected TestProperties properties;
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
        ChannelRestConnectionFactory connectionFactory = createConnectionFactory();
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();

        MSTeamsChannelMessageConverter messageConverter = new MSTeamsChannelMessageConverter(new MSTeamsChannelMessageFormatter(markupEncoderUtil));
        MSTeamsChannelMessageSender messageSender = new MSTeamsChannelMessageSender(ChannelKeys.MS_TEAMS, connectionFactory);

        MSTeamsChannel msTeamsChannel = new MSTeamsChannel(messageConverter, messageSender, eventManager, executingJobManager);
        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(UUID.randomUUID(), properties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK));

        MessageResult messageResult = null;
        try {
            messageResult = msTeamsChannel.distributeMessages(
                msTeamsJobDetailsModel,
                TEST_MESSAGE_HOLDER,
                "jobName",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of()
            );
        } catch (AlertException e) {
            Assertions.fail("Failed to distribute simple channel message due to an exception", e);
        }

        Assertions.assertFalse(messageResult.hasErrors(), "The message result had errors");
        Assertions.assertFalse(messageResult.hasWarnings(), "The message result had warnings");
    }

    private ChannelRestConnectionFactory createConnectionFactory() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);
        return new ChannelRestConnectionFactory(testAlertProperties, proxyManager, gson);
    }

}
