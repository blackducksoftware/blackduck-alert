package com.synopsys.integration.alert.channel.email;

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
import com.synopsys.integration.alert.channel.email.descriptor.EmailDistributionTypeConverter;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.CommonTypeConverter;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@Transactional
@WebAppConfiguration
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class EmailDistributionTypeConverterTestIT {
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;
    @Autowired
    private EmailGroupDistributionRepository emailGroupDistributionRepository;
    @Autowired
    private CommonTypeConverter commonTypeConverter;
    @Autowired
    private ContentConverter contentConverter;

    @After
    public void cleanUp() {
        commonDistributionRepository.deleteAll();
        emailGroupDistributionRepository.deleteAll();
    }

    @Test
    public void populateConfigFromEntityTest() {
        final EmailDistributionTypeConverter emailDistributionTypeConverter = new EmailDistributionTypeConverter(contentConverter, commonTypeConverter, commonDistributionRepository);

        final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = new EmailGroupDistributionConfigEntity("logo", "subject line", false);
        final EmailGroupDistributionConfigEntity savedEmailEntity = emailGroupDistributionRepository.save(emailGroupDistributionConfigEntity);

        final CommonDistributionConfigEntity commonDistributionConfigEntity = new CommonDistributionConfigEntity(savedEmailEntity.getId(), EmailGroupChannel.COMPONENT_NAME, "nice name", "some_provider", FrequencyType.REAL_TIME,
            Boolean.FALSE, FormatType.DEFAULT);
        final CommonDistributionConfigEntity savedCommonEntity = commonDistributionRepository.save(commonDistributionConfigEntity);

        final Config config = emailDistributionTypeConverter.populateConfigFromEntity(savedEmailEntity);
        Assert.assertTrue(EmailDistributionConfig.class.isAssignableFrom(config.getClass()));
        final EmailDistributionConfig emailConfig = (EmailDistributionConfig) config;

        Assert.assertEquals(emailGroupDistributionConfigEntity.getEmailSubjectLine(), emailConfig.getEmailSubjectLine());
        Assert.assertEquals(emailGroupDistributionConfigEntity.getEmailTemplateLogoImage(), emailConfig.getEmailTemplateLogoImage());
        Assert.assertEquals(emailGroupDistributionConfigEntity.getProjectOwnerOnly(), emailConfig.getProjectOwnerOnly());

        Assert.assertEquals(savedCommonEntity.getDistributionConfigId().toString(), emailConfig.getDistributionConfigId());
        Assert.assertEquals(savedCommonEntity.getDistributionType(), emailConfig.getDistributionType());
        Assert.assertEquals(savedCommonEntity.getName(), emailConfig.getName());
        Assert.assertEquals(savedCommonEntity.getProviderName(), emailConfig.getProviderName());
        Assert.assertEquals(savedCommonEntity.getFrequency().toString(), emailConfig.getFrequency());
        Assert.assertEquals(savedCommonEntity.getFilterByProject().toString(), emailConfig.getFilterByProject());
    }
}
