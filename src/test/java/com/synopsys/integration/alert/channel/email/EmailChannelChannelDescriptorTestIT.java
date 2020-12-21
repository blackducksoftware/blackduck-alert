package com.synopsys.integration.alert.channel.email;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.ChannelDescriptorTestIT;
import com.synopsys.integration.alert.channel.email.actions.EmailDistributionTestAction;
import com.synopsys.integration.alert.channel.email.actions.EmailGlobalTestAction;
import com.synopsys.integration.alert.channel.email.actions.EmailTestActionHelper;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.template.EmailChannelMessageParser;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.email.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultAuditAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.TestAlertProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelChannelDescriptorTestIT extends ChannelDescriptorTestIT {
    public static final String UNIT_TEST_PROJECT_NAME = "TestProject1";
    private static final String EMAIL_TEST_PROVIDER_CONFIG_NAME = "emailTestProviderConfig";
    private static final String DEFAULT_TEST_EMAIL_ADDRESS = "noreply@blackducksoftware.com";

    @Autowired
    private EmailDescriptor emailDescriptor;
    @Autowired
    private Gson gson;
    @Autowired
    private DefaultAuditAccessor auditUtility;
    @Autowired
    private EmailAddressHandler emailAddressHandler;
    @Autowired
    private EmailChannelMessageParser emailChannelMessageParser;
    @Autowired
    private EmailGlobalTestAction emailGlobalTestAction;
    @Autowired
    private EmailTestActionHelper emailTestActionHelper;

    @Override
    public Optional<ConfigurationModel> saveGlobalConfiguration() {
        Map<String, String> valueMap = new HashMap<>();
        String smtpHost = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        String smtpFrom = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        String smtpUser = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        String smtpPassword = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        Boolean smtpEhlo = Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        Boolean smtpAuth = Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        Integer smtpPort = Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        valueMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), smtpHost);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), smtpFrom);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), smtpUser);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), smtpPassword);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), String.valueOf(smtpEhlo));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), String.valueOf(smtpAuth));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), String.valueOf(smtpPort));

        Map<String, ConfigurationFieldModel> fieldModelMap = MockConfigurationModelFactory.mapStringsToFields(valueMap);

        return Optional.of(configurationAccessor.createConfiguration(ChannelKeys.EMAIL, ConfigContextEnum.GLOBAL, fieldModelMap.values()));
    }

    @Override
    public DistributionJobDetailsModel createDistributionJobDetails() {
        return new EmailJobDetailsModel(
            "Alert unit test subject line",
            false,
            true,
            null,
            List.of("noreply@blackducksoftware.com")
        );
    }

    @Override
    public DistributionEvent createChannelEvent() throws AlertException {
        LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", UNIT_TEST_PROJECT_NAME)
                                             .applySubTopic(subTopic.getName(), subTopic.getValue())
                                             .build();

        ConfigurationModel emailGlobalConfig = optionalChannelGlobalConfig
                                                   .orElseThrow(() -> new AlertRuntimeException("Missing Email global config"));

        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), RestConstants.JSON_DATE_FORMAT);
        DistributionEvent event = new DistributionEvent(ChannelKeys.EMAIL.getUniversalKey(), createdAt, 1L, ProcessingType.DEFAULT.name(),
            MessageContentGroup.singleton(content), distributionJobModel, emailGlobalConfig);
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

    @Override
    public boolean assertGlobalFields(Set<DefinedFieldModel> globalFields) {
        Set<String> fieldNames = Arrays.stream(EmailPropertyKeys.values()).map(EmailPropertyKeys::getPropertyKey).collect(Collectors.toSet());
        boolean result = globalFields
                             .stream()
                             .map(DefinedFieldModel::getKey)
                             .allMatch(fieldNames::contains);

        Optional<DefinedFieldModel> emailPassword = globalFields
                                                        .stream()
                                                        .filter(field -> EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey().equals(field.getKey()))
                                                        .findFirst();
        if (emailPassword.isPresent()) {
            result = result && emailPassword.get().getSensitive();
        }
        return result;
    }

    @Override
    public boolean assertDistributionFields(Set<DefinedFieldModel> distributionFields) {
        Set<String> fieldNames = Set.of(EmailDescriptor.KEY_SUBJECT_LINE, EmailDescriptor.KEY_PROJECT_OWNER_ONLY);
        Set<String> passedFieldNames = distributionFields.stream().map(DefinedFieldModel::getKey).collect(Collectors.toSet());
        return passedFieldNames.containsAll(fieldNames);
    }

    @Override
    public Map<String, String> createInvalidGlobalFieldMap() {
        return Map.of(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), "",
            EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), "",
            EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "abc",
            EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "def",
            EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "xyz");
    }

    @Override
    public FieldUtility createValidGlobalFieldUtility(ConfigurationModel configurationModel) {
        FieldUtility validGlobalFieldUtility = super.createValidGlobalFieldUtility(configurationModel);
        ConfigurationFieldModel destinationField = ConfigurationFieldModel.create(TestAction.KEY_DESTINATION_NAME);
        destinationField.setFieldValue("noreply@blackducksoftware.com");
        validGlobalFieldUtility.addFields(Map.of(TestAction.KEY_DESTINATION_NAME, destinationField));
        return validGlobalFieldUtility;
    }

    @Override
    public FieldModel createTestConfigDestination() {
        return createFieldModel(ChannelKeys.EMAIL.getUniversalKey(), DEFAULT_TEST_EMAIL_ADDRESS);
    }

    @Override
    public String getEventDestinationName() {
        return ChannelKeys.EMAIL.getUniversalKey();
    }

    @Override
    public TestAction getGlobalTestAction() {
        return emailGlobalTestAction;
    }

    @Override
    public ChannelDistributionTestAction getChannelDistributionTestAction() {
        TestAlertProperties alertProperties = new TestAlertProperties();
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, new MessageContentGroupCsvCreator(), gson);
        EmailChannel emailChannel = new EmailChannel(gson, alertProperties, auditUtility, emailAddressHandler, freemarkerTemplatingService, emailChannelMessageParser, emailAttachmentFileCreator);

        return new EmailDistributionTestAction(emailChannel, emailTestActionHelper);
    }

    @Test
    public void testProjectOwner() throws Exception {
        // update the distribution jobs configuration and run the send test again
        // set the project owner field to false

        String blackDuckProjectName = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PROJECT_NAME);
        String blackDuckProjectHref = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PROJECT_HREF);
        BlackDuckProjectDetailsModel projectFilter = new BlackDuckProjectDetailsModel(blackDuckProjectName, blackDuckProjectHref);

        EmailJobDetailsModel emailJobDetails = new EmailJobDetailsModel(
            "Alert unit test subject line",
            true,
            false,
            null,
            List.of()
        );
        DistributionJobRequestModel updateRequestModel = new DistributionJobRequestModel(
            distributionJobModel.isEnabled(),
            distributionJobModel.getName(),
            distributionJobModel.getDistributionFrequency(),
            distributionJobModel.getProcessingType(),
            distributionJobModel.getChannelDescriptorName(),
            providerGlobalConfig.getConfigurationId(),
            true,
            null,
            distributionJobModel.getNotificationTypes(),
            List.of(projectFilter),
            distributionJobModel.getPolicyFilterPolicyNames(),
            distributionJobModel.getVulnerabilityFilterSeverityNames(),
            emailJobDetails
        );

        distributionJobModel = jobAccessor.updateJob(distributionJobModel.getJobId(), updateRequestModel);
        testDistributionConfig();
    }

    @Override
    protected ConfigurationModel saveProviderGlobalConfig() {
        ConfigurationFieldModel nameField = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        nameField.setFieldValue(EMAIL_TEST_PROVIDER_CONFIG_NAME);
        ConfigurationFieldModel enabledField = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        enabledField.setFieldValue("true");

        String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.createSensitive(blackDuckApiKey);
        blackDuckApiField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));
        return configurationAccessor.createConfiguration(providerKey, ConfigContextEnum.GLOBAL, List.of(nameField, enabledField, blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));
    }

}
