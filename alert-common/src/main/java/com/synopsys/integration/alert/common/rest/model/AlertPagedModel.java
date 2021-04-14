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

public class AlertPagedModel<M extends AlertSerializableModel> extends AlertPagedDetails<M> implements Serializable {
    public AlertPagedModel(int totalPages, int currentPage, int pageSize, List<M> models) {
        super(totalPages, currentPage, pageSize, models);
    }

}
