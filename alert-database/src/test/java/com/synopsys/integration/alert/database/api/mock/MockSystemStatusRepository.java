package com.synopsys.integration.alert.database.api.mock;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.database.system.SystemStatus;
import com.synopsys.integration.alert.database.system.SystemStatusRepository;

public class MockSystemStatusRepository extends DefaultMockJPARepository<SystemStatus, Long> implements SystemStatusRepository {

    //Only methods that are used by a test are currently implemented, all others are left default.
    private SystemStatus systemStatus;

    public MockSystemStatusRepository(Boolean startingStatus) {
        this.systemStatus = new SystemStatus(startingStatus, new Date());
    }

    @Override
    public List<SystemStatus> findAll() {
        return List.of(systemStatus);
    }

    @Override
    public <S extends SystemStatus> S save(S entity) {
        this.systemStatus = new SystemStatus(entity.isInitialConfigurationPerformed(), entity.getStartupTime());

        return (S) this.systemStatus;
    }

    @Override
    public Optional<SystemStatus> findById(Long aLong) {
        return Optional.ofNullable(this.systemStatus);
    }

}
