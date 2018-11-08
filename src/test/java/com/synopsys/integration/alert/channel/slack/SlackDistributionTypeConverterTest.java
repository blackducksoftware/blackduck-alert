package com.synopsys.integration.alert.channel.slack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDistributionTypeConverter;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.CommonTypeConverter;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

public class SlackDistributionTypeConverterTest extends AlertIntegrationTest {

    @Autowired
    private CommonDistributionRepository commonDistributionRepository;

    @Autowired
    private SlackDistributionRepository slackDistributionRepository;

    @Autowired
    private CommonTypeConverter commonTypeConverter;

    @Autowired
    private ContentConverter contentConverter;

    @After
    public void cleanUp() {
        commonDistributionRepository.deleteAll();
        slackDistributionRepository.deleteAll();
    }

    @Test
    public void populateConfigFromEntityTest() {
        final SlackDistributionTypeConverter slackDistributionTypeConverter = new SlackDistributionTypeConverter(contentConverter, commonTypeConverter, commonDistributionRepository);

        final SlackDistributionConfigEntity slackDistributionConfigEntity = new SlackDistributionConfigEntity("webhook url", "ducky", "#myCoolRoom");
        final SlackDistributionConfigEntity savedSlackEntity = slackDistributionRepository.save(slackDistributionConfigEntity);

        final CommonDistributionConfigEntity commonDistributionConfigEntity = new CommonDistributionConfigEntity(savedSlackEntity.getId(), SlackChannel.COMPONENT_NAME, "nice name", "some_provider", FrequencyType.REAL_TIME,
            Boolean.FALSE, FormatType.DEFAULT);
        final CommonDistributionConfigEntity savedCommonEntity = commonDistributionRepository.save(commonDistributionConfigEntity);

        final Config config = slackDistributionTypeConverter.populateConfigFromEntity(savedSlackEntity);
        Assert.assertTrue(SlackDistributionConfig.class.isAssignableFrom(config.getClass()));
        final SlackDistributionConfig hipChatConfig = (SlackDistributionConfig) config;

        Assert.assertEquals(slackDistributionConfigEntity.getWebhook(), hipChatConfig.getWebhook());
        Assert.assertEquals(slackDistributionConfigEntity.getChannelUsername(), hipChatConfig.getChannelUsername());
        Assert.assertEquals(slackDistributionConfigEntity.getChannelName(), hipChatConfig.getChannelName());

        Assert.assertEquals(savedCommonEntity.getDistributionConfigId().toString(), hipChatConfig.getDistributionConfigId());
        Assert.assertEquals(savedCommonEntity.getDistributionType(), hipChatConfig.getDistributionType());
        Assert.assertEquals(savedCommonEntity.getName(), hipChatConfig.getName());
        Assert.assertEquals(savedCommonEntity.getProviderName(), hipChatConfig.getProviderName());
        Assert.assertEquals(savedCommonEntity.getFrequency().toString(), hipChatConfig.getFrequency());
        Assert.assertEquals(savedCommonEntity.getFilterByProject().toString(), hipChatConfig.getFilterByProject());
    }
}
