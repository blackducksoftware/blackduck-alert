package com.blackduck.integration.alert.api.processor.filter;

import java.util.List;
import java.util.function.Predicate;

import com.blackduck.integration.alert.common.rest.model.AlertPagedDetails;

public class StatefulAlertPage<T, E extends Exception> {
    private final AlertPagedDetails<T> alertPagedDetails;
    private final PageRetriever<T, E> pageRetriever;
    private final Predicate<AlertPagedDetails<T>> hasNextPage;

    public StatefulAlertPage(AlertPagedDetails<T> alertPagedDetails, PageRetriever<T, E> pageRetriever, Predicate<AlertPagedDetails<T>> hasNextPage) {
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
