/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.function.BiFunction;

import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.util.Stringable;

public class StatefulAlertPage<T extends Stringable> {
    private final BiFunction<Integer, Integer, AlertPagedDetails<T>> retrievePage;
    private final AlertPagedDetails alertPagedDetails;
    
    public StatefulAlertPage(AlertPagedDetails alertPagedDetails, BiFunction<Integer, Integer, AlertPagedDetails<T>> retrievePage) {
        this.alertPagedDetails = alertPagedDetails;
        this.retrievePage = retrievePage;
    }

    public boolean isEmpty() {
        return alertPagedDetails.getModels().isEmpty();
    }

    public StatefulAlertPage<T> retrieveNextPage() {
        AlertPagedDetails<T> nextPage = retrievePage.apply(alertPagedDetails.getCurrentPage() + 1, alertPagedDetails.getPageSize());
        return new StatefulAlertPage<>(nextPage, retrievePage);
    }

    public boolean hasNextPage() {
        return alertPagedDetails.getCurrentPage() < alertPagedDetails.getTotalPages() - 1;
    }

    public AlertPagedDetails<T> getCurrentPage() {
        return alertPagedDetails;
    }
}
