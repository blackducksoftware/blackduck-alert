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
package com.synopsys.integration.alert.channel.slack2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.ChannelITTestAssertions;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

public class SlackChannelTestIT extends AbstractChannelTest {
    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() {
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        SlackChannelMessageFormatter slackChannelMessageFormatter = new SlackChannelMessageFormatter(markupEncoderUtil);
        SlackChannelMessageConverter slackChannelMessageConverter = new SlackChannelMessageConverter(slackChannelMessageFormatter);

        SlackChannelMessageSender slackChannelMessageSender = new SlackChannelMessageSender(createRestChannelUtility(), ChannelKeys.SLACK);

        SlackChannelV2 slackChannel = new SlackChannelV2(slackChannelMessageConverter, slackChannelMessageSender);

        SlackJobDetailsModel distributionDetails = new SlackJobDetailsModel(
            null,
            properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK),
            properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME),
            properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME)
        );

        ChannelITTestAssertions.assertSendSimpleMessageSuccess(slackChannel, distributionDetails);
    }

}
