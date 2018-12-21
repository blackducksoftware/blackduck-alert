package com.synopsys.integration.alert.channel.email;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.ChannelDescriptorTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.DateRange;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelChannelDescriptorTestIT extends ChannelDescriptorTest {
    public static final String UNIT_TEST_JOB_NAME = "EmailUnitTestJob";
    public static final String UNIT_TEST_PROJECT_NAME = "TestProject1";
    @Autowired
    private BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    @Autowired
    private BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    @Autowired
    private UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    @Autowired
    private EmailDescriptor emailDescriptor;

    @BeforeEach
    public void testSetup() throws Exception {
        final DatabaseEntity project1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(UNIT_TEST_PROJECT_NAME, "", "", ""));
        final DatabaseEntity project2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("TestProject2", "", "", ""));
        final DatabaseEntity project3 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project three", "", "", ""));
        final DatabaseEntity project4 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project four", "", "", ""));
        final DatabaseEntity project5 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project UnitTest five", "", "", "noreply@blackducksoftware.com"));

        final DatabaseEntity user1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));
        final DatabaseEntity user2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));
        final DatabaseEntity user3 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));

        final UserProjectRelation userProjectRelation1 = new UserProjectRelation(user1.getId(), project1.getId());
        final UserProjectRelation userProjectRelation2 = new UserProjectRelation(user1.getId(), project2.getId());
        final UserProjectRelation userProjectRelation3 = new UserProjectRelation(user2.getId(), project3.getId());
        final UserProjectRelation userProjectRelation4 = new UserProjectRelation(user3.getId(), project4.getId());
        final UserProjectRelation userProjectRelation5 = new UserProjectRelation(user3.getId(), project5.getId());
        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4, userProjectRelation5)));

        final String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        final ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        final String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        final ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        final String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        final ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        provider_global = configurationAccessor
                              .createConfiguration(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL, List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));
    }

    @Override
    public Optional<ConfigurationAccessor.ConfigurationModel> saveGlobalConfiguration() throws Exception {
        final Map<String, String> valueMap = new HashMap<>();
        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final String smtpUser = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        final String smtpPassword = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        final Boolean smtpEhlo = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        final Boolean smtpAuth = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        final Integer smtpPort = Integer.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        valueMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), smtpHost);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), smtpFrom);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), smtpUser);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), smtpPassword);
        valueMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), String.valueOf(smtpEhlo));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), String.valueOf(smtpAuth));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), String.valueOf(smtpPort));

        final Map<String, ConfigurationFieldModel> fieldModelMap = MockConfigurationModelFactory.mapStringsToFields(valueMap);

        return Optional.of(configurationAccessor.createConfiguration(EmailGroupChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL, fieldModelMap.values()));
    }

    @Override
    public ConfigurationAccessor.ConfigurationModel saveDistributionConfiguration() throws Exception {
        final List<ConfigurationFieldModel> models = new LinkedList<>();
        models.addAll(MockConfigurationModelFactory.createEmailDistributionFields());
        return configurationAccessor.createConfiguration(EmailGroupChannel.COMPONENT_NAME, ConfigContextEnum.DISTRIBUTION, models);
    }

    @Override
    public DistributionEvent createChannelEvent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", UNIT_TEST_PROJECT_NAME, null, subTopic, Collections.emptyList());
        List<ConfigurationAccessor.ConfigurationModel> models = List.of();
        try {
            models = configurationAccessor.getConfigurationsByDescriptorName(EmailGroupChannel.COMPONENT_NAME);
        } catch (final AlertDatabaseConstraintException e) {
            e.printStackTrace();
        }

        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (final ConfigurationAccessor.ConfigurationModel model : models) {
            fieldMap.putAll(model.getCopyOfKeyToFieldMap());
        }

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        final String createdAt = RestConstants.formatDate(DateRange.createCurrentDateTimestamp());
        final DistributionEvent event = new DistributionEvent(String.valueOf(distribution_config.getConfigurationId()), EmailGroupChannel.COMPONENT_NAME, createdAt, BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), content,
            fieldAccessor);
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

    @Override
    public boolean assertGlobalFields(final Collection<DefinedFieldModel> globalFields) {
        boolean result = true;
        final Set<String> fieldNames = Arrays.stream(EmailPropertyKeys.values()).map(EmailPropertyKeys::getPropertyKey).collect(Collectors.toSet());
        result = result && globalFields.stream().map(DefinedFieldModel::getKey).allMatch(fieldNames::contains);

        final Optional<DefinedFieldModel> emailPassword = globalFields.stream()
                                                              .filter(field -> EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey().equals(field.getKey()))
                                                              .findFirst();
        if (emailPassword.isPresent()) {
            result = result && emailPassword.get().getSensitive();
        }
        return result;
    }

    @Override
    public boolean assertDistributionFields(final Collection<DefinedFieldModel> distributionFields) {
        final Set<String> fieldNames = Set.of(EmailDescriptor.KEY_SUBJECT_LINE, EmailDescriptor.KEY_PROJECT_OWNER_ONLY, EmailDescriptor.KEY_EMAIL_ADDRESSES);
        return distributionFields.stream().map(DefinedFieldModel::getKey).allMatch(fieldNames::contains);
    }

    @Override
    public Map<String, String> createInvalidGlobalFieldMap() {
        return Map.of(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "abc",
            EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "def",
            EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "xyz");
    }

    @Override
    public Map<String, String> createInvalidDistributionFieldMap() {
        return Map.of();
    }

    @Override
    public String createTestConfigDestination() {
        return "noreply@blackducksoftware.com";
    }

    @Override
    public String getTestJobName() {
        return UNIT_TEST_JOB_NAME;
    }

    @Test
    public void testProjectOwner() throws Exception {
        // update the distribution jobs configuration and run the send test again
        // set the project owner field to false
        final List<ConfigurationAccessor.ConfigurationModel> model = configurationAccessor.getConfigurationByDescriptorNameAndContext(getDescriptor().getName(), ConfigContextEnum.DISTRIBUTION);
        for (final ConfigurationAccessor.ConfigurationModel configurationModel : model) {
            final Long configId = configurationModel.getConfigurationId();
            final List<ConfigurationFieldModel> fieldModels = MockConfigurationModelFactory.createEmailDistributionFieldsProjectOwnerOnly();
            configurationAccessor.updateConfiguration(configId, fieldModels);
        }
        this.testDistributionConfig();
    }
}
