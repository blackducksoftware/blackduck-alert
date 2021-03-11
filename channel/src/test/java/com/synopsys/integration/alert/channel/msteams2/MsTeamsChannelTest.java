package com.synopsys.integration.alert.channel.msteams2;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.exception.IntegrationException;

public class MsTeamsChannelTest extends AbstractChannelTest {
    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IntegrationException {
        RestChannelUtility restChannelUtility = createRestChannelUtility();
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();

        MSTeamsChannelMessageConverter messageConverter = new MSTeamsChannelMessageConverter(new MSTeamsChannelMessageFormatter(markupEncoderUtil));
        MSTeamsChannelMessageSender messageSender = new MSTeamsChannelMessageSender(restChannelUtility, ChannelKeys.MS_TEAMS);
        MSTeamsChannelV2 msTeamsChannel = new MSTeamsChannelV2(messageConverter, messageSender);

        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(UUID.randomUUID(), properties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK));

        LinkableItem testProvider = new LinkableItem("Test Provider Label", "Test Provider Config Name");
        ProviderDetails testProviderDetails = new ProviderDetails(0L, testProvider);

        LinkableItem testDetail1 = new LinkableItem("Detail 1 Label", "Test Detail Value");
        LinkableItem testDetail2 = new LinkableItem("Detail 2 Label", "Test Detail Value (with URL)", "https://google.com");

        String simpleMessageClassName = SimpleMessage.class.getSimpleName();
        SimpleMessage testMessageContent = SimpleMessage.original(testProviderDetails, "Test summary field of " + simpleMessageClassName, "Test description field of " + simpleMessageClassName, List.of(testDetail1, testDetail2));

        ProviderMessageHolder testMessageHolder = new ProviderMessageHolder(List.of(), List.of(testMessageContent));
        MessageResult messageResult = msTeamsChannel.distributeMessages(msTeamsJobDetailsModel, testMessageHolder);

        assertFalse(messageResult.hasErrors(), "The message result had errors");
        assertFalse(messageResult.hasWarnings(), "The message result had warnings");
    }

}
