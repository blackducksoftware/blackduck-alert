package com.blackduck.integration.alert.api.processor.filter;

import com.blackduck.integration.alert.common.rest.model.AlertPagedDetails;

public interface PageRetriever<T, E extends Exception> {

    AlertPagedDetails<T> retrieveNextPage(int currentOffset, int currentLimit) throws E;

    AlertPagedDetails<T> retrievePage(int currentOffset, int currentLimit) throws E;
}
