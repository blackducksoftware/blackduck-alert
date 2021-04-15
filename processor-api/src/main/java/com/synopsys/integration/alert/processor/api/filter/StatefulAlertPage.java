/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;

public class StatefulAlertPage<T, E extends Exception> {
    private final NextPageRetriever<T, E> nextPageRetriever;
    private final AlertPagedDetails alertPagedDetails;

    public StatefulAlertPage(AlertPagedDetails alertPagedDetails, NextPageRetriever<T, E> nextPageRetriever) {
        this.alertPagedDetails = alertPagedDetails;
        this.nextPageRetriever = nextPageRetriever;
    }

    public boolean isEmpty() {
        return alertPagedDetails.getModels().isEmpty();
    }

    public StatefulAlertPage<T, E> retrieveNextPage() throws E {
        if (hasNextPage()) {
            AlertPagedDetails<T> nextPage = nextPageRetriever.retrieveNextPage(alertPagedDetails.getCurrentPage(), alertPagedDetails.getPageSize());
            return new StatefulAlertPage<>(nextPage, nextPageRetriever);
        }
        return new StatefulAlertPage<>(AlertPagedDetails.EMPTY_PAGE(), nextPageRetriever);
    }

    public boolean hasNextPage() {
        return alertPagedDetails.getCurrentPage() < alertPagedDetails.getTotalPages() - 1;
    }

    public List<T> getCurrentModels() {
        return alertPagedDetails.getModels();
    }
}
