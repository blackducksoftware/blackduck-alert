package com.blackduck.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;

public interface SystemStatusAccessor {

    boolean isSystemInitialized();

    void setSystemInitialized(boolean systemInitialized);

    void startupOccurred();

    OffsetDateTime getStartupTime();
}
