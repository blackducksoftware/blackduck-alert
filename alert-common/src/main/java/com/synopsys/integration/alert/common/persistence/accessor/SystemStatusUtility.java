package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Date;

public interface SystemStatusUtility {

    boolean isSystemInitialized();

    void setSystemInitialized(final boolean systemInitialized);

    void startupOccurred();

    Date getStartupTime();
}
