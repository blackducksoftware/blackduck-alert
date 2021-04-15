/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

public class AlertPagedDetails<M> {

    // FIXME we should use terminology based on "offset" and "limit" which are standard REST API paging terms
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;
    private final List<M> models;

    public static <M> AlertPagedDetails<M> EMPTY_PAGE() {
        return new AlertPagedDetails<>(0, 0, 0, List.of());
    }

    public AlertPagedDetails(int totalPages, int currentPage, int pageSize, List<M> models) {
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.models = models;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<M> getModels() {
        return models;
    }

}
