package com.synopsys.integration.alert.authentication.ldap.database.accessor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.LDAPConfigurationEntity;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.LDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class LDAPConfigAccessor implements UniqueConfigurationAccessor<LDAPConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final LDAPConfigurationRepository ldapConfigurationRepository;

    @Autowired
    public LDAPConfigAccessor(EncryptionUtility encryptionUtility, LDAPConfigurationRepository ldapConfigurationRepository) {
        this.encryptionUtility = encryptionUtility;
        this.ldapConfigurationRepository = ldapConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LDAPConfigModel> getConfiguration() {
        return ldapConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesConfigurationExist() {
        return ldapConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LDAPConfigModel createConfiguration(LDAPConfigModel ldapConfigModel) throws AlertConfigurationException {
        if (doesConfigurationExist()) {
            throw new AlertConfigurationException("An LDAP configuration already exists.");
        }

        OffsetDateTime createAndUpdatedDateTime = DateUtils.createCurrentDateTimestamp();
        LDAPConfigurationEntity ldapConfigurationEntity = toEntity(UUID.randomUUID(), ldapConfigModel, createAndUpdatedDateTime, createAndUpdatedDateTime);
        LDAPConfigurationEntity savedEntity = ldapConfigurationRepository.save(ldapConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LDAPConfigModel updateConfiguration(LDAPConfigModel ldapConfigModel) throws AlertConfigurationException {
        LDAPConfigurationEntity existingLDAPConfigurationEntity =
            ldapConfigurationRepository
                .findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                .orElseThrow(() -> new AlertConfigurationException("An LDAP configuration does not exist"));

        if (ldapConfigModel.getIsManagerPasswordSet().isPresent() && ldapConfigModel.getIsManagerPasswordSet().get()) {
            String decryptedPassword = encryptionUtility.decrypt(existingLDAPConfigurationEntity.getManagerPassword());
            ldapConfigModel.setManagerPassword(decryptedPassword);
        }

        LDAPConfigurationEntity updatedLDAPConfigurationEntity = toEntity(
            existingLDAPConfigurationEntity.getConfigurationId(),
            ldapConfigModel,
            existingLDAPConfigurationEntity.getCreatedAt(),
            DateUtils.createCurrentDateTimestamp()
        );
        LDAPConfigurationEntity savedEntity = ldapConfigurationRepository.save(updatedLDAPConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration() {
        ldapConfigurationRepository.deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    private LDAPConfigModel toModel(LDAPConfigurationEntity ldapConfigurationEntity) {
        return new LDAPConfigModel(
            ldapConfigurationEntity.getConfigurationId().toString(),
            DateUtils.formatDate(ldapConfigurationEntity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(ldapConfigurationEntity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            ldapConfigurationEntity.getEnabled(),
            ldapConfigurationEntity.getServerName(),
            ldapConfigurationEntity.getManagerDn(),
            ldapConfigurationEntity.getManagerPassword(),
            StringUtils.isNotBlank(ldapConfigurationEntity.getManagerPassword()),
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

    private LDAPConfigurationEntity toEntity(UUID configurationId, LDAPConfigModel ldapConfigModel, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        return new LDAPConfigurationEntity(
            configurationId,
            createdTime,
            lastUpdated,
            ldapConfigModel.getEnabled().orElse(Boolean.FALSE),
            ldapConfigModel.getServerName(),
            ldapConfigModel.getManagerDn(),
            ldapConfigModel.getManagerPassword().orElse(""),
            ldapConfigModel.getAuthenticationType().orElse(""),
            ldapConfigModel.getReferral().orElse(""),
            ldapConfigModel.getUserSearchBase().orElse(""),
            ldapConfigModel.getUserSearchFilter().orElse(""),
            ldapConfigModel.getUserDnPatterns().orElse(""),
            ldapConfigModel.getUserAttributes().orElse(""),
            ldapConfigModel.getGroupSearchBase().orElse(""),
            ldapConfigModel.getGroupSearchFilter().orElse(""),
            ldapConfigModel.getGroupRoleAttribute().orElse("")
        );
    }
}
