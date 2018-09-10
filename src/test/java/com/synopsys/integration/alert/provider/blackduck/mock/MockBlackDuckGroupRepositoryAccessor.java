package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupRepositoryAccessor;

public class MockBlackDuckGroupRepositoryAccessor extends BlackDuckGroupRepositoryAccessor {
    private final Map<Long, BlackDuckGroupEntity> blackDuckGroupEntityMap = new HashMap<>();
    private Long count = 1L;

    public MockBlackDuckGroupRepositoryAccessor() {
        super(null);
    }

    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final BlackDuckGroupEntity blackDuckGroupEntity = (BlackDuckGroupEntity) entity;

        final BlackDuckGroupEntity newEntity = new BlackDuckGroupEntity(blackDuckGroupEntity.getName(), blackDuckGroupEntity.getActive(), blackDuckGroupEntity.getHref());
        if (null == blackDuckGroupEntity.getId()) {
            newEntity.setId(count);
            count++;
        } else {
            newEntity.setId(blackDuckGroupEntity.getId());
        }
        blackDuckGroupEntityMap.put(newEntity.getId(), newEntity);
        return newEntity;
    }

    public List<? extends DatabaseEntity> readEntities() {
        return new ArrayList<>(blackDuckGroupEntityMap.values());
    }

    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return Optional.ofNullable(blackDuckGroupEntityMap.get(new Long(id)));
    }

    public void deleteEntity(final long id) {
        blackDuckGroupEntityMap.remove(new Long(id));
    }

    public BlackDuckGroupEntity findByName(final String name) {
        final Optional<BlackDuckGroupEntity> optionalBlackDuckGroupEntity = blackDuckGroupEntityMap.values().stream().filter(blackDuckGroupEntity -> blackDuckGroupEntity.getName().equals(name)).findFirst();
        return optionalBlackDuckGroupEntity.orElse(null);
    }

}
