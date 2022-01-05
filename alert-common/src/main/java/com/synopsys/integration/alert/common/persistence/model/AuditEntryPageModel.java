/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public class AuditEntryPageModel extends AlertPagedModel<AuditEntryModel> {
    public AuditEntryPageModel(int totalPages, int currentPage, int pageSize, List<AuditEntryModel> content) {
        super(totalPages, currentPage, pageSize, content);
    }

    public List<AuditEntryModel> getContent() {
        return getModels();
    }

}
