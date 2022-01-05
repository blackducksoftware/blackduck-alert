/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.action;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class IssueTrackerTestActionFieldStatusCreator {
    private static final String FIELD_PLACEHOLDER = "none";

    //TODO: This addresses that hasErrors is set in the UI but this should be improved upon since it is not associated with a field
    public List<AlertFieldStatus> createWithoutField(String errorMessage) {
        return List.of(AlertFieldStatus.error(FIELD_PLACEHOLDER, errorMessage));
    }

}
