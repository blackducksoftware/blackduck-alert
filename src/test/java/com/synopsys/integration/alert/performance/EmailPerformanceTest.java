package com.synopsys.integration.alert.performance;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManager;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
public class EmailPerformanceTest {
    private final static String EMAIL_PERFORMANCE_JOB_NAME = "Email Performance Job";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();

    private static String EMAIL_CHANNEL_KEY;

    private static String EMAIL_SMTP_HOST;
    private static String EMAIL_SMTP_FROM;
    private static String EMAIL_RECIPIENT;

    @BeforeAll
    public static void initTest() {
        EMAIL_CHANNEL_KEY = ChannelKeys.EMAIL.getUniversalKey();

        TestProperties testProperties = new TestProperties();
        EMAIL_SMTP_HOST = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        EMAIL_SMTP_FROM = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        EMAIL_RECIPIENT = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT);
    }

    @Test
    @Ignore
    @Disabled
    public void testEmailJob() throws Exception {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunner.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManager configurationManager = new ConfigurationManager(gson, alertRequestUtility, blackDuckProviderService.getBlackDuckProviderKey(), EMAIL_CHANNEL_KEY);
        IntegrationPerformanceTestRunner integrationPerformanceTestRunner = new IntegrationPerformanceTestRunner(gson, dateTimeFormatter, alertRequestUtility, blackDuckProviderService, configurationManager);

        Map<String, FieldValueModel> emailGlobalConfigFields = new HashMap<>();
        emailGlobalConfigFields.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), new FieldValueModel(List.of(EMAIL_SMTP_HOST), true));
        emailGlobalConfigFields.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), new FieldValueModel(List.of(EMAIL_SMTP_FROM), true));
        emailGlobalConfigFields.put(FieldModelTestAction.KEY_DESTINATION_NAME, new FieldValueModel(List.of(EMAIL_RECIPIENT), true));

        FieldModel emailGlobalConfig = new FieldModel(EMAIL_CHANNEL_KEY, ConfigContextEnum.GLOBAL.name(), emailGlobalConfigFields);

        Map<String, FieldValueModel> emailJobFields = new HashMap<>();
        emailJobFields.put(ChannelDescriptor.KEY_ENABLED, new FieldValueModel(List.of("true"), true));
        emailJobFields.put(ChannelDescriptor.KEY_CHANNEL_NAME, new FieldValueModel(List.of(EMAIL_CHANNEL_KEY), true));
        emailJobFields.put(ChannelDescriptor.KEY_NAME, new FieldValueModel(List.of(EMAIL_PERFORMANCE_JOB_NAME), true));
        emailJobFields.put(ChannelDescriptor.KEY_FREQUENCY, new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        emailJobFields.put(ChannelDescriptor.KEY_PROVIDER_TYPE, new FieldValueModel(List.of(blackDuckProviderService.getBlackDuckProviderKey()), true));

        emailJobFields.put(EmailDescriptor.KEY_SUBJECT_LINE, new FieldValueModel(List.of(EMAIL_PERFORMANCE_JOB_NAME), true));
        emailJobFields.put(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, new FieldValueModel(List.of("true"), true));
        emailJobFields.put(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, new FieldValueModel(List.of(EMAIL_RECIPIENT), true));

        integrationPerformanceTestRunner.runTest(emailGlobalConfig, emailJobFields, EMAIL_PERFORMANCE_JOB_NAME);
    }

}
