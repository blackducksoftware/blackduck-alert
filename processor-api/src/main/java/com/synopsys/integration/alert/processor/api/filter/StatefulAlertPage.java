/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.function.BiFunction;

import com.synopsys.integration.alert.common.rest.model.AlertPagedBase;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.util.Stringable;

public class StatefulAlertPage<T extends Stringable> extends AlertPagedBase<T> {
    private final BiFunction<Integer, Integer, AlertPagedBase<T>> retrievePage;

    public static <T extends AlertSerializableModel> StatefulAlertPage<T> EMPTY_STATEFUL_PAGE() {
        return new StatefulAlertPage<>(0, 0, 0, List.of(), (x, y) -> AlertPagedBase.EMPTY_PAGE());
    }

    public StatefulAlertPage(int totalPages, int pageNumber, int pageSize, List<T> currentModels, BiFunction<Integer, Integer, AlertPagedBase<T>> retrievePage) {
        super(totalPages, pageNumber, pageSize, currentModels);
        this.retrievePage = retrievePage;
    }

    public boolean hasModels() {
        return !getModels().isEmpty();
    }

    public StatefulAlertPage<T> retrieveNextPage() {
        AlertPagedBase<T> nextPage = retrievePage.apply(getCurrentPage() + 1, getPageSize());
        return new StatefulAlertPage<>(getTotalPages(), getCurrentPage() + 1, getPageSize(), nextPage.getModels(), retrievePage);
    }

    public boolean hasNextPage() {
        return getCurrentPage() < getTotalPages() - 1;
    }
}
