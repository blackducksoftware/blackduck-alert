/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;

public class StatefulAlertPage<T extends Stringable> {
    private final NextPageRetriever nextPageRetriever;
    private final AlertPagedDetails alertPagedDetails;

    public StatefulAlertPage(AlertPagedDetails alertPagedDetails, NextPageRetriever<T> nextPageRetriever) {
        this.alertPagedDetails = alertPagedDetails;
        this.nextPageRetriever = nextPageRetriever;
    }

    public boolean isEmpty() {
        return alertPagedDetails.getModels().isEmpty();
    }

    public StatefulAlertPage<T> retrieveNextPage() throws IntegrationException {
        AlertPagedDetails<T> nextPage = nextPageRetriever.retrieveNextPage(alertPagedDetails.getCurrentPage(), alertPagedDetails.getPageSize());
        return new StatefulAlertPage<>(nextPage, nextPageRetriever);
    }

    public boolean hasNextPage() {
        return alertPagedDetails.getCurrentPage() < alertPagedDetails.getTotalPages() - 1;
    }

    public AlertPagedDetails<T> getCurrentPage() {
        return alertPagedDetails;
    }
}
