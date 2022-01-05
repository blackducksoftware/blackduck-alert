/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesEntity;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

@Component
@Transactional
public class DefaultProviderTaskPropertiesAccessor implements ProviderTaskPropertiesAccessor {
    private final ProviderTaskPropertiesRepository providerTaskPropertiesRepository;

    @Autowired
    public DefaultProviderTaskPropertiesAccessor(ProviderTaskPropertiesRepository providerTaskPropertiesRepository) {
        this.providerTaskPropertiesRepository = providerTaskPropertiesRepository;
    }

    @Override
    public Optional<String> getTaskProperty(String taskName, String propertyKey) {
        if (StringUtils.isBlank(taskName) || StringUtils.isBlank(propertyKey)) {
            return Optional.empty();
        }
        return providerTaskPropertiesRepository.findByTaskNameAndPropertyName(taskName, propertyKey).map(ProviderTaskPropertiesEntity::getValue);
    }

    @Override
    public void setTaskProperty(Long configId, String taskName, String propertyKey, String propertyValue) {
        ProviderTaskPropertiesEntity taskPropertyToSave = new ProviderTaskPropertiesEntity(configId, taskName, propertyKey, propertyValue);
        providerTaskPropertiesRepository.save(taskPropertyToSave);
    }

}
