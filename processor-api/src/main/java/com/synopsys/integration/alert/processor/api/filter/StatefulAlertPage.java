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
    private final PageRetriever<T, E> pageRetriever;
    private final AlertPagedDetails alertPagedDetails;

    public StatefulAlertPage(AlertPagedDetails alertPagedDetails, PageRetriever<T, E> pageRetriever) {
        this.alertPagedDetails = alertPagedDetails;
        this.pageRetriever = pageRetriever;
    }

    public boolean isEmpty() {
        return alertPagedDetails.getModels().isEmpty();
    }

    public StatefulAlertPage<T, E> retrieveNextPage() throws E {
        if (hasNextPage()) {
            AlertPagedDetails<T> nextPage = pageRetriever.retrieveNextPage(alertPagedDetails.getCurrentPage(), alertPagedDetails.getPageSize());
            return new StatefulAlertPage<>(nextPage, pageRetriever);
        }
        return new StatefulAlertPage<>(AlertPagedDetails.emptyPage(), pageRetriever);
    }

    public boolean hasNextPage() {
        return alertPagedDetails.getCurrentPage() < alertPagedDetails.getTotalPages() - 1;
    }

    public List<T> getCurrentModels() {
        return alertPagedDetails.getModels();
    }
}
