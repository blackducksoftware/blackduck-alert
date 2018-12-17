package com.synopsys.integration.alert.channel.email;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDistributionUIConfig;
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
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.rest.RestConstants;

public class EmailChannelDescriptorTestIT extends DescriptorTestConfigTest {

    public static final String SUBJECT_LINE = "Alert It Test";
    @Autowired
    private BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    @Autowired
    private BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    @Autowired
    private UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    @Autowired
    private EmailDescriptor emailDescriptor;

    @Before
    public void testSetup() throws Exception {
        final DatabaseEntity project1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", "", ""));
        final DatabaseEntity project2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project two", "", "", ""));
        final DatabaseEntity project3 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project three", "", "", ""));
        final DatabaseEntity project4 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project four", "", "", ""));

        final DatabaseEntity user1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));
        final DatabaseEntity user2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));
        final DatabaseEntity user3 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));

        final UserProjectRelation userProjectRelation1 = new UserProjectRelation(user1.getId(), project1.getId());
        final UserProjectRelation userProjectRelation2 = new UserProjectRelation(user1.getId(), project2.getId());
        final UserProjectRelation userProjectRelation3 = new UserProjectRelation(user2.getId(), project3.getId());
        final UserProjectRelation userProjectRelation4 = new UserProjectRelation(user3.getId(), project4.getId());
        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4)));

        final String blackDuckTimeoutKey = TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT.getPropertyKey();
        final ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        final String blackDuckApiKey = TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY.getPropertyKey();
        final ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        final String blackDuckProviderUrlKey = TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL.getPropertyKey();
        final ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        final ConfigurationAccessor.ConfigurationModel global_config = configurationAccessor
                                                                           .createConfiguration(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL, List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));
    }

    @Override
    public Map<String, String> createGlobalConfiguration() {
        return null;
    }

    @Override
    public Map<String, String> createDistributionConfiguration() {
        return null;
    }

    @Override
    public DistributionEvent createChannelEvent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());
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
        final DistributionEvent event = new DistributionEvent("1L", EmailGroupChannel.COMPONENT_NAME, createdAt, BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), content, fieldAccessor);
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

    @Override
    public FieldModel getFieldModel() {
        final Map<String, FieldValueModel> valueMap = new HashMap<>();
        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final String smtpUser = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        final String smtpPassword = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        final Boolean smtpEhlo = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        final Boolean smtpAuth = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        final Integer smtpPort = Integer.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        valueMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), new FieldValueModel(List.of(String.valueOf(smtpHost)), true));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), new FieldValueModel(List.of(String.valueOf(smtpFrom)), true));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), new FieldValueModel(List.of(String.valueOf(smtpUser)), true));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), new FieldValueModel(List.of(String.valueOf(smtpPassword)), true));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), new FieldValueModel(List.of(String.valueOf(smtpEhlo)), true));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), new FieldValueModel(List.of(String.valueOf(smtpAuth)), true));
        valueMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), new FieldValueModel(List.of(String.valueOf(smtpPort)), true));

        valueMap.put(EmailDistributionUIConfig.KEY_SUBJECT_LINE, new FieldValueModel(List.of(SUBJECT_LINE), true));
        valueMap.put(EmailDistributionUIConfig.KEY_PROJECT_OWNER_ONLY, new FieldValueModel(List.of("true"), true));
        valueMap.put(EmailDistributionUIConfig.KEY_EMAIL_ADDRESSES, new FieldValueModel(List.of("noreply@blackducksoftware.com"), true));

        final FieldModel model = new FieldModel("1L", EmailGroupChannel.COMPONENT_NAME, ConfigContextEnum.DISTRIBUTION.name(), valueMap);
        return model;
    }

}
