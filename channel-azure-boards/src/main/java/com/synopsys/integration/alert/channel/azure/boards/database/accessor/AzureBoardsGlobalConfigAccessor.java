package com.synopsys.integration.alert.channel.azure.boards.database.accessor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;

//TODO: This class is a placeholder and is currently unimplemented
@Component
public class AzureBoardsGlobalConfigAccessor implements ConfigurationAccessor<AzureBoardsGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public AzureBoardsGlobalConfigAccessor(EncryptionUtility encryptionUtility) {
        this.encryptionUtility = encryptionUtility;
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfigurationCount() {
        return 0;
    }

    @Override
    public Optional<AzureBoardsGlobalConfigModel> getConfiguration(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<AzureBoardsGlobalConfigModel> getConfigurationByName(String configurationName) {
        return Optional.empty();
    }

    @Override
    public boolean existsConfigurationByName(String configurationName) {
        return false;
    }

    @Override
    public boolean existsConfigurationById(UUID id) {
        return false;
    }

    @Override
    public AlertPagedModel<AzureBoardsGlobalConfigModel> getConfigurationPage(
        int page, int size, String searchTerm, String sortName, String sortOrder
    ) {
        return null;
    }

    @Override
    public AzureBoardsGlobalConfigModel createConfiguration(AzureBoardsGlobalConfigModel configuration) throws AlertConfigurationException {
        return null;
    }

    @Override
    public AzureBoardsGlobalConfigModel updateConfiguration(UUID configurationId, AzureBoardsGlobalConfigModel configuration) throws AlertConfigurationException {
        return null;
    }

    @Override
    public void deleteConfiguration(UUID configurationId) {

    }
}
