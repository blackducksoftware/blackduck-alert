/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.web;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public class NotificationFilterModelOptions extends AlertPagedModel<NotificationFilterModel> {
    public NotificationFilterModelOptions(int totalPages, int currentPage, int pageSize, List<NotificationFilterModel> options) {
        super(totalPages, currentPage, pageSize, options);
    }

    public List<NotificationFilterModel> getOptions() {
        return getModels();
    }

}
