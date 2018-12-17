package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;

public class MockBlackDuckUserRepositoryAccessor extends BlackDuckUserRepositoryAccessor {
    private final Map<Long, BlackDuckUserEntity> blackDuckUserEntityMap = new HashMap<>();
    private Long count = 1L;

    public MockBlackDuckUserRepositoryAccessor() {
        super(null);
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final BlackDuckUserEntity blackDuckUserEntity = (BlackDuckUserEntity) entity;

        final BlackDuckUserEntity newEntity = new BlackDuckUserEntity(blackDuckUserEntity.getEmailAddress(), blackDuckUserEntity.getOptOut());
        if (null == blackDuckUserEntity.getId()) {
            newEntity.setId(count);
            count++;
        } else {
            newEntity.setId(blackDuckUserEntity.getId());
        }
        blackDuckUserEntityMap.put(newEntity.getId(), newEntity);
        return newEntity;
    }

    @Override
    public List<? extends DatabaseEntity> readEntities() {
        return new ArrayList<>(blackDuckUserEntityMap.values());
    }

    @Override
    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return Optional.ofNullable(blackDuckUserEntityMap.get(Long.valueOf(id)));
    }

    @Override
    public void deleteEntity(final long id) {
        blackDuckUserEntityMap.remove(Long.valueOf(id));
    }

    @Override
    public List<BlackDuckUserEntity> deleteAndSaveAll(final Iterable<BlackDuckUserEntity> userEntitiesToDelete, final Iterable<BlackDuckUserEntity> userEntitiesToAdd) {
        userEntitiesToDelete.forEach(blackDuckUserEntity -> {
            blackDuckUserEntityMap.remove(blackDuckUserEntity.getId());
        });
        final List<BlackDuckUserEntity> blackDuckUserEntitiesSaved = new ArrayList<>();
        userEntitiesToAdd.forEach(blackDuckUserEntity -> {
            blackDuckUserEntitiesSaved.add((BlackDuckUserEntity) saveEntity(blackDuckUserEntity));
        });
        return blackDuckUserEntitiesSaved;
    }
}
