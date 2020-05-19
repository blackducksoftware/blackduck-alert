package com.synopsys.integration.alert.database.api.mock;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.system.SystemStatusEntity;
import com.synopsys.integration.alert.database.system.SystemStatusRepository;

public class MockSystemStatusRepository extends DefaultMockJPARepository<SystemStatusEntity, Long> implements SystemStatusRepository {

    //Only methods that are used by a test are currently implemented, all others are left default.
    private SystemStatusEntity systemStatus;

    public MockSystemStatusRepository(Boolean startingStatus) {
        this.systemStatus = new SystemStatusEntity(startingStatus, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    public List<SystemStatusEntity> findAll() {
        return List.of(systemStatus);
    }

    @Override
    public <S extends SystemStatusEntity> S save(S entity) {
        this.systemStatus = new SystemStatusEntity(entity.isInitialConfigurationPerformed(), entity.getStartupTime());

        return (S) this.systemStatus;
    }

    @Override
    public Optional<SystemStatusEntity> findById(Long aLong) {
        return Optional.ofNullable(this.systemStatus);
    }

}
