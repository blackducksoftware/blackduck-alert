package com.synopsys.integration.alert.database.api;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesEntity;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

@Component
public class DefaultProviderTaskPropertiesAccessor implements ProviderTaskPropertiesAccessor {
    private ProviderTaskPropertiesRepository providerTaskPropertiesRepository;

    @Autowired
    public DefaultProviderTaskPropertiesAccessor(ProviderTaskPropertiesRepository providerTaskPropertiesRepository) {
        this.providerTaskPropertiesRepository = providerTaskPropertiesRepository;
    }

    @Override
    public Optional<String> getTaskProperty(String taskName, String propertyKey) {
        if (StringUtils.isBlank(taskName) || StringUtils.isBlank(propertyKey)) {
            Optional.empty();
        }
        return providerTaskPropertiesRepository.findByTaskNameAndPropertyName(taskName, propertyKey).map(ProviderTaskPropertiesEntity::getValue);
    }

    @Override
    public void setTaskProperty(Long configId, String taskName, String propertyKey, String propertyValue) throws AlertDatabaseConstraintException {
        if (null == configId || StringUtils.isBlank(taskName) || StringUtils.isBlank(propertyKey) || StringUtils.isBlank(propertyValue)) {
            throw new AlertDatabaseConstraintException("All fields are required to save a task property");
        }
        ProviderTaskPropertiesEntity taskPropertyToSave = new ProviderTaskPropertiesEntity(configId, taskName, propertyKey, propertyValue);
        providerTaskPropertiesRepository.save(taskPropertyToSave);
    }

}
