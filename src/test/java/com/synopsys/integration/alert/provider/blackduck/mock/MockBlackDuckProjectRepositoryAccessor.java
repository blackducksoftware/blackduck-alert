package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;

public class MockBlackDuckProjectRepositoryAccessor extends BlackDuckProjectRepositoryAccessor {
    private final Map<Long, BlackDuckProjectEntity> blackDuckProjectEntityMap = new HashMap<>();
    private Long count = 1L;

    public MockBlackDuckProjectRepositoryAccessor() {
        super(null);
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final BlackDuckProjectEntity blackDuckProjectEntity = (BlackDuckProjectEntity) entity;

        final BlackDuckProjectEntity newEntity = new BlackDuckProjectEntity(blackDuckProjectEntity.getName(), blackDuckProjectEntity.getDescription(), blackDuckProjectEntity.getHref(), blackDuckProjectEntity.getProjectOwnerEmail());
        if (null == blackDuckProjectEntity.getId()) {
            newEntity.setId(count);
            count++;
        } else {
            newEntity.setId(blackDuckProjectEntity.getId());
        }
        blackDuckProjectEntityMap.put(newEntity.getId(), newEntity);
        return newEntity;
    }

    @Override
    public List<? extends DatabaseEntity> readEntities() {
        return new ArrayList<>(blackDuckProjectEntityMap.values());
    }

    @Override
    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return Optional.ofNullable(blackDuckProjectEntityMap.get(new Long(id)));
    }

    @Override
    public void deleteEntity(final long id) {
        blackDuckProjectEntityMap.remove(new Long(id));
    }

    @Override
    public List<BlackDuckProjectEntity> deleteAndSaveAll(final Iterable<BlackDuckProjectEntity> blackDuckProjectEntities) {
        blackDuckProjectEntityMap.clear();
        final List<BlackDuckProjectEntity> blackDuckProjectEntitiesSaved = new ArrayList<>();
        blackDuckProjectEntities.forEach(blackDuckProjectEntity -> {
            blackDuckProjectEntitiesSaved.add((BlackDuckProjectEntity) saveEntity(blackDuckProjectEntity));
        });
        return blackDuckProjectEntitiesSaved;
    }

    @Override
    public BlackDuckProjectEntity findByName(final String name) {
        final Optional<BlackDuckProjectEntity> optionalBlackDuckProjectEntity = blackDuckProjectEntityMap.entrySet()
                                                                                    .stream()
                                                                                    .map(entry -> entry.getValue())
                                                                                    .filter(blackDuckProjectEntity -> name.equals(blackDuckProjectEntity.getName()))
                                                                                    .findFirst();
        return optionalBlackDuckProjectEntity.orElse(null);
    }

}
