/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import com.blackduck.integration.alert.common.rest.model.AlertPagedDetails;

public interface PageRetriever<T, E extends Exception> {

    AlertPagedDetails<T> retrieveNextPage(int currentOffset, int currentLimit) throws E;

    AlertPagedDetails<T> retrievePage(int currentOffset, int currentLimit) throws E;
}
