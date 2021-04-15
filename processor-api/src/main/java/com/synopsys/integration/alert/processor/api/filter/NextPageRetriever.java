/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;

public interface NextPageRetriever<T, E extends Exception> {

    AlertPagedDetails<T> retrieveNextPage(int currentOffset, int currentLimit) throws E;
}
