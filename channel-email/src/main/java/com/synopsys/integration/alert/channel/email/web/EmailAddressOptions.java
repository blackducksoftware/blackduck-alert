/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public class EmailAddressOptions extends AlertPagedModel<EmailAddressSelectOption> {
    public EmailAddressOptions(int totalPages, int currentPage, int pageSize, List<EmailAddressSelectOption> options) {
        super(totalPages, currentPage, pageSize, options);
    }

    public List<EmailAddressSelectOption> getOptions() {
        return getModels();
    }

}
