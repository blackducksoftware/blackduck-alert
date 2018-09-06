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

    public List<? extends DatabaseEntity> readEntities() {
        return new ArrayList<>(blackDuckUserEntityMap.values());
    }

    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return Optional.ofNullable(blackDuckUserEntityMap.get(new Long(id)));
    }

    public void deleteEntity(final long id) {
        blackDuckUserEntityMap.remove(new Long(id));
    }

}
