package com.blackduck.integration.alert.database.job.api;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.system.SystemStatusEntity;
import com.blackduck.integration.alert.database.system.SystemStatusRepository;

@Component
public class DefaultSystemStatusAccessor implements SystemStatusAccessor {
    public static final Long SYSTEM_STATUS_ID = 1L;
    private final SystemStatusRepository systemStatusRepository;

    @Autowired
    public DefaultSystemStatusAccessor(SystemStatusRepository systemStatusRepository) {
        this.systemStatusRepository = systemStatusRepository;
    }

    @Override
    @Transactional
    public boolean isSystemInitialized() {
        return getSystemStatus().isInitialConfigurationPerformed();
    }

    @Override
    @Transactional
    public void setSystemInitialized(boolean systemInitialized) {
        SystemStatusEntity systemStatus = getSystemStatus();
        SystemStatusEntity newSystemStatus = new SystemStatusEntity(systemInitialized, systemStatus.getStartupTime());
        updateSystemStatus(newSystemStatus);
    }

    @Override
    @Transactional
    public void startupOccurred() {
        SystemStatusEntity systemStatus = getSystemStatus();
        SystemStatusEntity newSystemStatus = new SystemStatusEntity(systemStatus.isInitialConfigurationPerformed(), createCurrentDateTimestamp());
        updateSystemStatus(newSystemStatus);
    }

    @Override
    @Transactional
    public OffsetDateTime getStartupTime() {
        return getSystemStatus().getStartupTime();
    }

    private OffsetDateTime createCurrentDateTimestamp() {
        return DateUtils.createCurrentDateTimestamp();
    }

    private SystemStatusEntity getSystemStatus() {
        return systemStatusRepository.findById(SYSTEM_STATUS_ID).orElse(new SystemStatusEntity());
    }

    private void updateSystemStatus(SystemStatusEntity systemStatus) {
        systemStatus.setId(SYSTEM_STATUS_ID);
        systemStatusRepository.save(systemStatus);
    }
}
