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
