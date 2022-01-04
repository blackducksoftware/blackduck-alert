/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;

public interface PageRetriever<T, E extends Exception> {

    AlertPagedDetails<T> retrieveNextPage(int currentOffset, int currentLimit) throws E;

    AlertPagedDetails<T> retrievePage(int currentOffset, int currentLimit) throws E;
}
