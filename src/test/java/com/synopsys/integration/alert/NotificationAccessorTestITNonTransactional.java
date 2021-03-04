package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_INTEGRATION)
@Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("alertdb")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class NotificationAccessorTestITNonTransactional {
    @Autowired
    private ConfigurationAccessor configurationAccessor;
    @Autowired
    private DefaultNotificationAccessor defaultNotificationAccessor;

    private ConfigurationModel providerConfigModel = null;

    @BeforeEach
    public void init() {
        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("true");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("My Black Duck Config");

        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://a-blackduck-server");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("123456789012345678901234567890123456789012345678901234567890");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigModel = configurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields);
    }

    @AfterEach
    @Transactional
    public void cleanup() {
        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<AlertNotificationModel> notificationModels = defaultNotificationAccessor.findAll(pageRequest, false);
        defaultNotificationAccessor.deleteNotificationList(notificationModels.getContent());
    }

    @Test
    public void setNotificationsProcessedTest() {
        AlertNotificationModel notificationModel = createAlertNotificationModel(false);

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(List.of(notificationModel));

        defaultNotificationAccessor.setNotificationsProcessed(savedModels);

        assertEquals(1, savedModels.size());
        Optional<AlertNotificationModel> alertNotificationModelTest = defaultNotificationAccessor.findById(savedModels.get(0).getId());
        assertTrue(alertNotificationModelTest.isPresent());
        assertTrue(alertNotificationModelTest.get().getProcessed());
    }

    @Test
    public void setNotificationsProcessedByIdTest() {
        AlertNotificationModel notificationModel = createAlertNotificationModel(false);

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(List.of(notificationModel));
        List<Long> notificationIds = savedModels
                                         .stream()
                                         .map(AlertNotificationModel::getId)
                                         .collect(Collectors.toList());

        defaultNotificationAccessor.setNotificationsProcessedById(new HashSet<>(notificationIds));

        assertEquals(1, notificationIds.size());
        Optional<AlertNotificationModel> alertNotificationModelTest = defaultNotificationAccessor.findById(notificationIds.get(0));
        assertTrue(alertNotificationModelTest.isPresent());
        assertTrue(alertNotificationModelTest.get().getProcessed());
    }

    private AlertNotificationModel createAlertNotificationModel(boolean processed) {
        String NOTIFICATION_TYPE = "notificationType";
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        return new AlertNotificationModel(1L, providerConfigModel.getConfigurationId(), "provider", "providerConfigName", NOTIFICATION_TYPE, "{content: \"content is here...\"}", createdAt, createdAt, processed);
    }

}
