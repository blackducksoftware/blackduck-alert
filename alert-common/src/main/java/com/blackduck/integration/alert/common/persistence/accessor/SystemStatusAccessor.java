/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;

public interface SystemStatusAccessor {

    boolean isSystemInitialized();

    void setSystemInitialized(boolean systemInitialized);

    void startupOccurred();

    OffsetDateTime getStartupTime();
}
