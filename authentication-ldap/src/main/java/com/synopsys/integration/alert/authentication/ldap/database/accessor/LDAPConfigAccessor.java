package com.synopsys.integration.alert.authentication.ldap.database.accessor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.LDAPConfigurationEntity;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.LDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPGlobalConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class LDAPConfigAccessor implements UniqueConfigurationAccessor<LDAPGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final LDAPConfigurationRepository ldapConfigurationRepository;

    public LDAPConfigAccessor(EncryptionUtility encryptionUtility, LDAPConfigurationRepository ldapConfigurationRepository) {
        this.encryptionUtility = encryptionUtility;
        this.ldapConfigurationRepository = ldapConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LDAPGlobalConfigModel> getConfiguration() {
        return ldapConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesConfigurationExist() {
        return ldapConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LDAPGlobalConfigModel createConfiguration(LDAPGlobalConfigModel ldapGlobalConfigModel) throws AlertConfigurationException {
        if (doesConfigurationExist()) {
            throw new AlertConfigurationException(String.format("A config with the name '%s' already exists.", ldapGlobalConfigModel.getName()));
        }

        OffsetDateTime createAndUpdatedDateTime = DateUtils.createCurrentDateTimestamp();
        LDAPConfigurationEntity ldapConfigurationEntity = toEntity(UUID.randomUUID(), ldapGlobalConfigModel, createAndUpdatedDateTime, createAndUpdatedDateTime);
        LDAPConfigurationEntity savedEntity = ldapConfigurationRepository.save(ldapConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LDAPGlobalConfigModel updateConfiguration(LDAPGlobalConfigModel ldapGlobalConfigModel) throws AlertConfigurationException {
        LDAPConfigurationEntity configurationEntity =
            ldapConfigurationRepository
                .findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                .orElseThrow(() -> new AlertConfigurationException(String.format("Config with name '%s' did not exist", AlertRestConstants.DEFAULT_CONFIGURATION_NAME)));

        if (ldapGlobalConfigModel.getIsManagerPasswordSet().isPresent() && ldapGlobalConfigModel.getIsManagerPasswordSet().get()) {
            String decryptedPassword = encryptionUtility.decrypt(configurationEntity.getManagerPassword());
            ldapGlobalConfigModel.setManagerPassword(decryptedPassword);
        }

        LDAPConfigurationEntity ldapConfigurationEntity = toEntity(
            configurationEntity.getConfigurationId(),
            ldapGlobalConfigModel,
            configurationEntity.getCreatedAt(),
            DateUtils.createCurrentDateTimestamp()
        );
        LDAPConfigurationEntity savedEntity = ldapConfigurationRepository.save(ldapConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration() {
        ldapConfigurationRepository.deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    private LDAPGlobalConfigModel toModel(LDAPConfigurationEntity ldapConfigurationEntity) {
        return new LDAPGlobalConfigModel(
            ldapConfigurationEntity.getConfigurationId().toString(),
            DateUtils.formatDate(ldapConfigurationEntity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(ldapConfigurationEntity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            ldapConfigurationEntity.getEnabled(),
            ldapConfigurationEntity.getServerName(),
            ldapConfigurationEntity.getManagerDn(),
            ldapConfigurationEntity.getManagerPassword(),
            ldapConfigurationEntity.getManagerPasswordSet(),
            ldapConfigurationEntity.getAuthenticationType(),
            ldapConfigurationEntity.getReferral(),
            ldapConfigurationEntity.getUserSearchBase(),
            ldapConfigurationEntity.getUserSearchFilter(),
            ldapConfigurationEntity.getUserDnPatterns(),
            ldapConfigurationEntity.getUserAttributes(),
            ldapConfigurationEntity.getGroupSearchBase(),
            ldapConfigurationEntity.getGroupSearchFilter(),
            ldapConfigurationEntity.getGroupRoleAttribute()
        );
    }

    private LDAPConfigurationEntity toEntity(UUID configurationId, LDAPGlobalConfigModel ldapGlobalConfigModel, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        return new LDAPConfigurationEntity(
            configurationId,
            ldapGlobalConfigModel.getName(),
            createdTime,
            lastUpdated,
            ldapGlobalConfigModel.getEnabled(),
            ldapGlobalConfigModel.getServerName(),
            ldapGlobalConfigModel.getManagerDn(),
            ldapGlobalConfigModel.getManagerPassword().orElse(""),
            ldapGlobalConfigModel.getIsManagerPasswordSet().orElse(Boolean.FALSE),
            ldapGlobalConfigModel.getAuthenticationType().orElse(""),
            ldapGlobalConfigModel.getReferral().orElse(""),
            ldapGlobalConfigModel.getUserSearchBase().orElse(""),
            ldapGlobalConfigModel.getUserSearchFilter().orElse(""),
            ldapGlobalConfigModel.getUserDnPatterns().orElse(""),
            ldapGlobalConfigModel.getUserAttributes().orElse(""),
            ldapGlobalConfigModel.getGroupSearchBase().orElse(""),
            ldapGlobalConfigModel.getGroupSearchFilter().orElse(""),
            ldapGlobalConfigModel.getGroupRoleAttribute().orElse("")
        );
    }
}
