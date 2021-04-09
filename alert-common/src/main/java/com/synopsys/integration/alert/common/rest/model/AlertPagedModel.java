/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

import com.synopsys.integration.alert.common.rest.api.ReadPageController;

import net.minidev.json.annotate.JsonIgnore;

public class AlertPagedModel<M extends AlertSerializableModel> extends AlertSerializableModel {
    public static final Integer DEFAULT_PAGE_NUMBER = Integer.valueOf(ReadPageController.DEFAULT_PAGE_NUMBER);
    public static final Integer DEFAULT_PAGE_SIZE = Integer.valueOf(ReadPageController.DEFAULT_PAGE_SIZE);

    // FIXME we should use terminology based on "offset" and "limit" which are standard REST API paging terms
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;
    private final List<M> models;

    public static <M extends AlertSerializableModel> AlertPagedModel<M> empty() {
        return new AlertPagedModel<>(0, 0, 0, List.of());
    }

    public AlertPagedModel(int totalPages, int currentPage, int pageSize, List<M> models) {
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

    @JsonIgnore
    public List<M> getModels() {
        return models;
    }

}
