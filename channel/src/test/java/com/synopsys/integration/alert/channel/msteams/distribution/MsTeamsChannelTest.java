package com.synopsys.integration.alert.channel.msteams.distribution;

import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.ChannelITTestAssertions;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class MsTeamsChannelTest extends AbstractChannelTest {
    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() {
        RestChannelUtility restChannelUtility = createRestChannelUtility();
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();

        MSTeamsChannelMessageConverter messageConverter = new MSTeamsChannelMessageConverter(new MSTeamsChannelMessageFormatter(markupEncoderUtil));
        MSTeamsChannelMessageSender messageSender = new MSTeamsChannelMessageSender(restChannelUtility, ChannelKeys.MS_TEAMS);

        MSTeamsChannelV2 msTeamsChannel = new MSTeamsChannelV2(messageConverter, messageSender);
        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(UUID.randomUUID(), properties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK));

        ChannelITTestAssertions.assertSendSimpleMessageSuccess(msTeamsChannel, msTeamsJobDetailsModel);
    }

}
