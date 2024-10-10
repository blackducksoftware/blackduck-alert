/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.web;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public class EmailAddressOptions extends AlertPagedModel<EmailAddressSelectOption> {
    public EmailAddressOptions(int totalPages, int currentPage, int pageSize, List<EmailAddressSelectOption> options) {
        super(totalPages, currentPage, pageSize, options);
    }

    public List<EmailAddressSelectOption> getOptions() {
        return getModels();
    }

}
