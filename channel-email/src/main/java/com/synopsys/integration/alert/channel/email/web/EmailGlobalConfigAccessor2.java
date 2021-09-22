package com.synopsys.integration.alert.channel.email.web;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor2;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel2;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

public class EmailGlobalConfigAccessor2 implements ConfigurationAccessor2<EmailGlobalConfigModel> {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final DescriptorConfigRepository descriptorConfigsRepository;
    private final ConfigContextRepository configContextRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public EmailGlobalConfigAccessor2(
        RegisteredDescriptorRepository registeredDescriptorRepository,
        DefinedFieldRepository definedFieldRepository,
        DescriptorConfigRepository descriptorConfigsRepository,
        ConfigContextRepository configContextRepository,
        FieldValueRepository fieldValueRepository,
        EncryptionUtility encryptionUtility
    ) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.configContextRepository = configContextRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.encryptionUtility = encryptionUtility;
    }

    @Override
    public Optional<ConfigurationModel2<EmailGlobalConfigModel>> getConfigurationById(Long id) {
        return descriptorConfigsRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    public List<ConfigurationModel2<EmailGlobalConfigModel>> getAllConfigurations() {
        return null;
    }

    @Override
    public ConfigurationModel2<EmailGlobalConfigModel> createConfiguration(EmailGlobalConfigModel configuration) {
        return null;
    }

    @Override
    public ConfigurationModel2<EmailGlobalConfigModel> updateConfiguration(Long configurationId, EmailGlobalConfigModel configuration) throws AlertConfigurationException {
        return null;
    }

    @Override
    public void deleteConfiguration(EmailGlobalConfigModel configuration) {

    }

    @Override
    public void deleteConfiguration(Long configurationId) {

    }

    private ConfigurationModel2<EmailGlobalConfigModel> createConfigModel(DescriptorConfigEntity descriptorConfigEntity) {
        return createConfigModel(
            descriptorConfigEntity.getDescriptorId(),
            descriptorConfigEntity.getId(),
            descriptorConfigEntity.getCreatedAt(),
            descriptorConfigEntity.getLastUpdated()
        );
    }


    private ConfigurationModel2<EmailGlobalConfigModel> createConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        String createdAtFormatted = DateUtils.formatDate(createdAt, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = DateUtils.formatDate(lastUpdated, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        EmailGlobalConfigModel newModel = new EmailGlobalConfigModel();
        Map<String, String> additionalJavamailProperties = new HashMap<>();
        List<FieldValueEntity> fieldValueEntities = fieldValueRepository.findByConfigId(configId);
        for (FieldValueEntity fieldValueEntity : fieldValueEntities) {
            DefinedFieldEntity definedFieldEntity = definedFieldRepository.findById(fieldValueEntity.getFieldId())
                .orElseThrow(() -> new AlertRuntimeException("Field Id missing from the database"));
            String key = definedFieldEntity.getKey();
            String decryptedValue = decrypt(fieldValueEntity.getValue(), BooleanUtils.isTrue(definedFieldEntity.getSensitive()));

            if (EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey().equals(key)) {
                newModel.setFrom(decryptedValue);
            } else if (EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey().equals(key)) {
                newModel.setHost(decryptedValue);
            } else if (EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey().equals(key)) {
                newModel.setPort(Integer.valueOf(decryptedValue));
            } else if (EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey().equals(key)) {
                newModel.setAuth(Boolean.valueOf(decryptedValue));
            } else if (EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey().equals(key)) {
                newModel.setUsername(decryptedValue);
            } else if (EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey().equals(key)) {
                newModel.setPassword(decryptedValue);
            } else {
                additionalJavamailProperties.put(key, decryptedValue);
            }
        }
        newModel.setAdditionalJavaMailProperties(additionalJavamailProperties);

        return new ConfigurationModel2<>(descriptorId, configId, createdAtFormatted, lastUpdatedFormatted, newModel);
    }

    private String decrypt(String value, boolean shouldDecrypt) {
        if (shouldDecrypt && value != null) {
            return encryptionUtility.decrypt(value);
        }
        return value;
    }
}
