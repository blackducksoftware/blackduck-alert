/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

// Javadoc of Runnable implies running in a thread and being executed asynchronously.
@FunctionalInterface
public interface Procedure {
    void run();
}
