/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.common.rest.api.ReadPageController;

import net.minidev.json.annotate.JsonIgnore;

public class AlertPagedModel<M extends AlertSerializableModel> extends AlertPagedDetails<M> implements Serializable {
    public static final Integer DEFAULT_PAGE_NUMBER = Integer.valueOf(ReadPageController.DEFAULT_PAGE_NUMBER);
    public static final Integer DEFAULT_PAGE_SIZE = Integer.valueOf(ReadPageController.DEFAULT_PAGE_SIZE);
    
    public AlertPagedModel(int totalPages, int currentPage, int pageSize, List<M> models) {
        super(totalPages, currentPage, pageSize, models);
    }

    @Override
    @JsonIgnore
    public List<M> getModels() {
        return super.getModels();
    }
}
