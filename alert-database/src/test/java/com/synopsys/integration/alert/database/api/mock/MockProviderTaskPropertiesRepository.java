package com.synopsys.integration.alert.database.api.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesEntity;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

public class MockProviderTaskPropertiesRepository extends DefaultMockJPARepository<ProviderTaskPropertiesEntity, Long> implements ProviderTaskPropertiesRepository {

    private Map<Long, ProviderTaskPropertiesEntity> providerTaskPropertiesEntityMap = new HashMap<>();

    public MockProviderTaskPropertiesRepository(Long providerConfigId, String taskName, String propertyName, String value) {
        ProviderTaskPropertiesEntity providerTaskPropertiesEntity = new ProviderTaskPropertiesEntity(providerConfigId, taskName, propertyName, value);
        this.save(providerTaskPropertiesEntity);
    }

    @Override
    public Optional<ProviderTaskPropertiesEntity> findByTaskNameAndPropertyName(String taskName, String propertyName) {
        return providerTaskPropertiesEntityMap.values()
                   .stream()
                   .filter(entity -> entity.getTaskName().equals(taskName) && entity.getPropertyName().equals(propertyName))
                   .findFirst();
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> S save(S entity) {
        providerTaskPropertiesEntityMap.put(entity.getProviderConfigId(), entity);
        return entity;
    }
}
