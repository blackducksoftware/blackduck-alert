package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.api.ProviderUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;

public class MockBlackDuckUserRepositoryAccessor extends ProviderUserRepositoryAccessor {
    private final Map<Long, ProviderUserEntity> blackDuckUserEntityMap = new HashMap<>();
    private Long count = 1L;

    public MockBlackDuckUserRepositoryAccessor() {
        super(null);
    }

    @Override
    public ProviderUserEntity saveEntity(final ProviderUserEntity blackDuckUserEntity) {
        final ProviderUserEntity newEntity = new ProviderUserEntity(blackDuckUserEntity.getEmailAddress(), blackDuckUserEntity.getOptOut(), blackDuckUserEntity.getProvider());
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
    public List<ProviderUserEntity> readEntities() {
        return new ArrayList<>(blackDuckUserEntityMap.values());
    }

    @Override
    public Optional<ProviderUserEntity> readEntity(final long id) {
        return Optional.ofNullable(blackDuckUserEntityMap.get(Long.valueOf(id)));
    }

    @Override
    public void deleteEntity(final long id) {
        blackDuckUserEntityMap.remove(Long.valueOf(id));
    }

    @Override
    public List<ProviderUserEntity> deleteAndSaveAll(final Iterable<ProviderUserEntity> userEntitiesToDelete, final Iterable<ProviderUserEntity> userEntitiesToAdd) {
        userEntitiesToDelete.forEach(blackDuckUserEntity -> {
            blackDuckUserEntityMap.remove(blackDuckUserEntity.getId());
        });
        final List<ProviderUserEntity> blackDuckUserEntitiesSaved = new ArrayList<>();
        userEntitiesToAdd.forEach(blackDuckUserEntity -> {
            blackDuckUserEntitiesSaved.add((ProviderUserEntity) saveEntity(blackDuckUserEntity));
        });
        return blackDuckUserEntitiesSaved;
    }
}
