/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.web;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public class NotificationFilterModelOptions extends AlertPagedModel<NotificationFilterModel> {
    public NotificationFilterModelOptions(int totalPages, int currentPage, int pageSize, List<NotificationFilterModel> options) {
        super(totalPages, currentPage, pageSize, options);
    }

    public List<NotificationFilterModel> getOptions() {
        return getModels();
    }

}
