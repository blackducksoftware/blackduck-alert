package com.synopsys.integration.alert.database.api.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.settings.SettingsKeyEntity;
import com.synopsys.integration.alert.database.settings.SettingsKeyRepository;

public class MockSettingsKeyRepository extends DefaultMockJPARepository<SettingsKeyEntity, Long> implements SettingsKeyRepository {

    private Map<String, SettingsKeyEntity> settingsKeyEntities = new HashMap<>();
    private Map<Long, SettingsKeyEntity> settingsKeyEntitiesById = new HashMap<>();

    @Override
    public Optional<SettingsKeyEntity> findByKey(String key) {
        return settingsKeyEntities.containsKey(key) ? Optional.of(settingsKeyEntities.get(key)) : Optional.empty();
    }

    @Override
    public List<SettingsKeyEntity> findAll() {
        return new ArrayList<SettingsKeyEntity>(settingsKeyEntities.values());
    }

    @Override
    public void deleteById(Long aLong) {
        if (settingsKeyEntitiesById.containsKey(aLong)) {
            String key = settingsKeyEntitiesById.get(aLong).getKey();
            settingsKeyEntities.remove(key);
            settingsKeyEntitiesById.remove(aLong);
        }
    }

    @Override
    public <S extends SettingsKeyEntity> S save(S entity) {
        settingsKeyEntities.put(entity.getKey(), entity);
        settingsKeyEntitiesById.put(entity.getId(), entity);
        return entity;
    }
}
