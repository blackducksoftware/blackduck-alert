package com.synopsys.integration.alert.channel.hipchat;

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
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDistributionTypeConverter;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.CommonTypeConverter;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@Transactional
@WebAppConfiguration
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HipChatDistributionTypeConverterTest {

    @Autowired
    private CommonDistributionRepository commonDistributionRepository;

    @Autowired
    private HipChatDistributionRepository hipChatDistributionRepository;

    @Autowired
    private CommonTypeConverter commonTypeConverter;

    @Autowired
    private ContentConverter contentConverter;

    @After
    public void cleanUp() {
        commonDistributionRepository.deleteAll();
        hipChatDistributionRepository.deleteAll();
    }

    @Test
    public void populateConfigFromEntityTest() {
        final HipChatDistributionTypeConverter hipChatDistributionTypeConverter = new HipChatDistributionTypeConverter(contentConverter, commonTypeConverter, commonDistributionRepository);

        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = new HipChatDistributionConfigEntity(12345, Boolean.FALSE, "red");
        final HipChatDistributionConfigEntity savedHipChatEntity = hipChatDistributionRepository.save(hipChatDistributionConfigEntity);

        final CommonDistributionConfigEntity commonDistributionConfigEntity = new CommonDistributionConfigEntity(savedHipChatEntity.getId(), HipChatChannel.COMPONENT_NAME, "nice name", "some_provider", FrequencyType.REAL_TIME,
            Boolean.FALSE, FormatType.DEFAULT);
        final CommonDistributionConfigEntity savedCommonEntity = commonDistributionRepository.save(commonDistributionConfigEntity);

        final Config config = hipChatDistributionTypeConverter.populateConfigFromEntity(savedHipChatEntity);
        Assert.assertTrue(HipChatDistributionConfig.class.isAssignableFrom(config.getClass()));
        final HipChatDistributionConfig hipChatConfig = (HipChatDistributionConfig) config;

        Assert.assertEquals(hipChatDistributionConfigEntity.getRoomId().toString(), hipChatConfig.getRoomId());
        Assert.assertEquals(hipChatDistributionConfigEntity.getColor(), hipChatConfig.getColor());
        Assert.assertEquals(hipChatDistributionConfigEntity.getNotify().booleanValue(), hipChatConfig.getNotify());

        Assert.assertEquals(savedCommonEntity.getDistributionConfigId().toString(), hipChatConfig.getDistributionConfigId());
        Assert.assertEquals(savedCommonEntity.getDistributionType(), hipChatConfig.getDistributionType());
        Assert.assertEquals(savedCommonEntity.getName(), hipChatConfig.getName());
        Assert.assertEquals(savedCommonEntity.getProviderName(), hipChatConfig.getProviderName());
        Assert.assertEquals(savedCommonEntity.getFrequency().toString(), hipChatConfig.getFrequency());
        Assert.assertEquals(savedCommonEntity.getFilterByProject().toString(), hipChatConfig.getFilterByProject());
    }
}
