/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.function.Predicate;

import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;

public class StatefulAlertPage<T, E extends Exception> {
    private final AlertPagedDetails alertPagedDetails;
    private final PageRetriever<T, E> pageRetriever;
    private final Predicate<AlertPagedDetails> hasNextPage;

    public StatefulAlertPage(AlertPagedDetails alertPagedDetails, PageRetriever<T, E> pageRetriever, Predicate<AlertPagedDetails> hasNextPage) {
        this.alertPagedDetails = alertPagedDetails;
        this.pageRetriever = pageRetriever;
        this.hasNextPage = hasNextPage;
    }

    public boolean isCurrentPageEmpty() {
        return alertPagedDetails.getModels().isEmpty();
    }

    public StatefulAlertPage<T, E> retrieveNextPage() throws E {
        if (hasNextPage()) {
            AlertPagedDetails<T> nextPage = pageRetriever.retrieveNextPage(alertPagedDetails.getCurrentPage(), alertPagedDetails.getPageSize());
            return new StatefulAlertPage<>(nextPage, pageRetriever, hasNextPage);
        }
        return new StatefulAlertPage<>(AlertPagedDetails.emptyPage(), pageRetriever, page -> false);
    }

    public boolean hasNextPage() {
        return hasNextPage.test(alertPagedDetails);
    }

    public List<T> getCurrentModels() {
        return alertPagedDetails.getModels();
    }

}
