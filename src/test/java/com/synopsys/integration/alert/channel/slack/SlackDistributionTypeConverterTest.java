package com.synopsys.integration.alert.channel.slack;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDistributionTypeConverter;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.CommonTypeConverter;
import com.synopsys.integration.alert.common.enumeration.DigestType;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@Transactional
@WebAppConfiguration
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class SlackDistributionTypeConverterTest {

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

        final CommonDistributionConfigEntity commonDistributionConfigEntity = new CommonDistributionConfigEntity(savedSlackEntity.getId(), SlackChannel.COMPONENT_NAME, "nice name", "some_provider", DigestType.REAL_TIME,
            Boolean.FALSE);
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
