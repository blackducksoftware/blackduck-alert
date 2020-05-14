package com.synopsys.integration.alert.channel.email;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.ChannelDescriptorTest;
import com.synopsys.integration.alert.channel.email.actions.EmailActionHelper;
import com.synopsys.integration.alert.channel.email.actions.EmailDistributionTestAction;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.template.EmailChannelMessageParser;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.email.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelChannelDescriptorTestIT extends ChannelDescriptorTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();
    private static final EmailChannelKey EMAIL_CHANNEL_KEY = new EmailChannelKey();

    public static final String UNIT_TEST_JOB_NAME = "EmailUnitTestJob";
    public static final String UNIT_TEST_PROJECT_NAME = "TestProject1";
    private static final String EMAIL_TEST_PROVIDER_CONFIG_NAME = "emailTestProviderConfig";
    @Autowired
    private DefaultProviderDataAccessor providerDataAccessor;
    @Autowired
    private ProviderProjectRepository providerProjectRepository;
    @Autowired
    private ProviderUserRepository providerUserRepository;
    @Autowired
    private ProviderUserProjectRelationRepository providerUserProjectRelationRepository;
    @Autowired
    private ProviderUserRepository blackDuckUserRepository;
    @Autowired
    private EmailDescriptor emailDescriptor;
    @Autowired
    private EmailChannelKey emailChannelKey;
    @Autowired
    private Gson gson;
    @Autowired
    private DefaultAuditUtility auditUtility;
    @Autowired
    private EmailAddressHandler emailAddressHandler;
    @Autowired
    private EmailChannelMessageParser emailChannelMessageParser;

    private ConfigurationModel providerConfig;

    @BeforeEach
    public void testSetup() throws Exception {
        ConfigurationFieldModel nameField = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        nameField.setFieldValue(EMAIL_TEST_PROVIDER_CONFIG_NAME);
        ConfigurationFieldModel enabledField = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        enabledField.setFieldValue("true");
        providerConfig = configurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, List.of(nameField, enabledField));
        Long providerConfigId = providerConfig.getConfigurationId();

        List<ProviderUserModel> allUsers = providerDataAccessor.getUsersByProviderConfigId(providerConfigId);
        deleteUsers(providerConfigId, allUsers);
        List<ProviderProject> allProjects = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId);
        providerDataAccessor.deleteProjects(allProjects);

        ProviderProject project1 = saveProject(providerConfigId, new ProviderProject(UNIT_TEST_PROJECT_NAME, "", "", ""));
        ProviderProject project2 = saveProject(providerConfigId, new ProviderProject("TestProject2", "", "", ""));
        ProviderProject project3 = saveProject(providerConfigId, new ProviderProject("Project three", "", "", ""));
        ProviderProject project4 = saveProject(providerConfigId, new ProviderProject("Project four", "", "", ""));
        ProviderProject project5 = saveProject(providerConfigId, new ProviderProject("Project UnitTest five", "", "", "noreply@blackducksoftware.com"));

        ProviderUserEntity user1 = blackDuckUserRepository.save(new ProviderUserEntity("noreply@blackducksoftware.com", false, providerConfigId));
        ProviderUserEntity user2 = blackDuckUserRepository.save(new ProviderUserEntity("noreply@blackducksoftware.com", false, providerConfigId));
        ProviderUserEntity user3 = blackDuckUserRepository.save(new ProviderUserEntity("noreply@blackducksoftware.com", false, providerConfigId));

        mapUsersToProjectByEmail(providerConfigId, project1.getHref(), Set.of(user1.getEmailAddress()));
        mapUsersToProjectByEmail(providerConfigId, project2.getHref(), Set.of(user1.getEmailAddress()));
        mapUsersToProjectByEmail(providerConfigId, project3.getHref(), Set.of(user2.getEmailAddress()));
        mapUsersToProjectByEmail(providerConfigId, project4.getHref(), Set.of(user3.getEmailAddress()));
        mapUsersToProjectByEmail(providerConfigId, project5.getHref(), Set.of(user3.getEmailAddress()));

        String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        provider_global = configurationAccessor
                              .createConfiguration(BLACK_DUCK_PROVIDER_KEY, ConfigContextEnum.GLOBAL, List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));

    }

    @AfterEach
    public void cleanUp() throws Exception {
        if (null != providerConfig) {
            configurationAccessor.deleteConfiguration(providerConfig);
        }
    }

    @Override
    public Optional<ConfigurationModel> saveGlobalConfiguration() throws Exception {
        Map<String, String> valueMap = new HashMap<>();
        String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        String smtpUser = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        String smtpPassword = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        Boolean smtpEhlo = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        Boolean smtpAuth = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        Integer smtpPort = Integer.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        valueMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), smtpHost);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), smtpFrom);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), smtpUser);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), smtpPassword);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), String.valueOf(smtpEhlo));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), String.valueOf(smtpAuth));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), String.valueOf(smtpPort));

        Map<String, ConfigurationFieldModel> fieldModelMap = MockConfigurationModelFactory.mapStringsToFields(valueMap);

        return Optional.of(configurationAccessor.createConfiguration(EMAIL_CHANNEL_KEY, ConfigContextEnum.GLOBAL, fieldModelMap.values()));
    }

    @Override
    public ConfigurationModel saveDistributionConfiguration() throws Exception {
        List<ConfigurationFieldModel> models = new LinkedList<>();
        models.addAll(MockConfigurationModelFactory.createEmailDistributionFields());
        return configurationAccessor.createConfiguration(EMAIL_CHANNEL_KEY, ConfigContextEnum.DISTRIBUTION, models);
    }

    @Override
    public DistributionEvent createChannelEvent() throws AlertException {
        LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", UNIT_TEST_PROJECT_NAME)
                                             .applySubTopic(subTopic.getName(), subTopic.getValue())
                                             .build();
        List<ConfigurationModel> models = List.of();
        try {
            models = configurationAccessor.getConfigurationsByDescriptorKey(EMAIL_CHANNEL_KEY);
        } catch (AlertDatabaseConstraintException e) {
            e.printStackTrace();
        }

        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (ConfigurationModel model : models) {
            fieldMap.putAll(model.getCopyOfKeyToFieldMap());
        }

        FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), RestConstants.JSON_DATE_FORMAT);
        DistributionEvent event = new DistributionEvent(String.valueOf(distribution_config.getConfigurationId()), EMAIL_CHANNEL_KEY.getUniversalKey(), createdAt, EMAIL_TEST_PROVIDER_CONFIG_NAME, ProcessingType.DEFAULT.name(),
            MessageContentGroup.singleton(content), fieldAccessor);
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

    @Override
    public boolean assertGlobalFields(Set<DefinedFieldModel> globalFields) {
        boolean result = true;
        Set<String> fieldNames = Arrays.stream(EmailPropertyKeys.values()).map(EmailPropertyKeys::getPropertyKey).collect(Collectors.toSet());
        result = result && globalFields
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
    public Map<String, String> createInvalidDistributionFieldMap() {
        return Map.of();
    }

    @Override
    public FieldModel createTestConfigDestination() {
        return createFieldModel(new SlackChannelKey().getUniversalKey(), "noreply@blackducksoftware.com");
    }

    @Override
    public String getTestJobName() {
        return UNIT_TEST_JOB_NAME;
    }

    @Override
    public String getDestinationName() {
        return EMAIL_CHANNEL_KEY.getUniversalKey();
    }

    @Override
    public TestAction getTestAction() {
        AlertProperties alertProperties = new TestAlertProperties();
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(alertProperties);
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, new MessageContentGroupCsvCreator(), gson);
        EmailChannel emailChannel = new EmailChannel(emailChannelKey, gson, alertProperties, auditUtility, emailAddressHandler, freemarkerTemplatingService, emailChannelMessageParser, emailAttachmentFileCreator);

        EmailActionHelper emailActionHelper = new EmailActionHelper(new EmailAddressHandler(providerDataAccessor), providerDataAccessor);
        return new EmailDistributionTestAction(emailChannel, emailActionHelper);
    }

    @Test
    public void testProjectOwner() throws Exception {
        // update the distribution jobs configuration and run the send test again
        // set the project owner field to false
        List<ConfigurationModel> model = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(getDescriptor().getDescriptorKey(), ConfigContextEnum.DISTRIBUTION);
        for (ConfigurationModel configurationModel : model) {
            Long configId = configurationModel.getConfigurationId();
            List<ConfigurationFieldModel> fieldModels = MockConfigurationModelFactory.createEmailDistributionFieldsProjectOwnerOnly();
            configurationAccessor.updateConfiguration(configId, fieldModels);
        }
        testDistributionConfig();
    }

    private void mapUsersToProjectByEmail(Long providerConfigId, String projectHref, Collection<String> emailAddresses) throws AlertDatabaseConstraintException {
        ProviderProjectEntity project = providerProjectRepository.findFirstByHref(projectHref)
                                            .orElseThrow(() -> new AlertDatabaseConstraintException("A project with the following href did not exist: " + projectHref));
        Long projectId = project.getId();
        for (String emailAddress : emailAddresses) {
            providerUserRepository.findByEmailAddressAndProviderConfigId(emailAddress, providerConfigId)
                .stream()
                .map(ProviderUserEntity::getId)
                .forEach(userId -> providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(userId, projectId)));
        }
    }

    private void deleteUsers(Long providerConfigId, Collection<ProviderUserModel> users) {
        users.forEach(user -> providerUserRepository.deleteByProviderConfigIdAndEmailAddress(providerConfigId, user.getEmailAddress()));
    }

    private ProviderProject saveProject(Long providerConfigId, ProviderProject providerProject) {
        ProviderProjectEntity trimmedBlackDuckProjectEntity = convertToProjectEntity(providerConfigId, providerProject);
        return convertToProjectModel(providerProjectRepository.save(trimmedBlackDuckProjectEntity));
    }

    private ProviderProject convertToProjectModel(ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    private ProviderProjectEntity convertToProjectEntity(Long providerConfigId, ProviderProject providerProject) {
        String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), DefaultProviderDataAccessor.MAX_DESCRIPTION_LENGTH);
        return new ProviderProjectEntity(providerProject.getName(), trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerConfigId);
    }
}
