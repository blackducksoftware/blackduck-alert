/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;

@RestController
@RequestMapping(DistributionController.JOB_AUDIT_PATH)
public class DistributionController {
    public static final String JOB_AUDIT_PATH = AlertRestConstants.BASE_PATH + "/distribution";

    private final DistributionActions distributionActions;

    @Autowired
    public DistributionController(DistributionActions distributionActions) {
        this.distributionActions = distributionActions;
    }

    /*
     * Acceptable sort values (Force specific values the user can sort by to avoid leaking DB data):
     *  channel
     *  frequency
     *  lastSent
     *  name
     *  status
     */
    @GetMapping("/audit-statuses")
    public AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortOrder,
        @RequestParam int pageSize,
        @RequestParam int pageNumber,
        @RequestParam(required = false) String searchTerm
    ) {
        return ResponseFactory.createContentResponseFromAction(distributionActions.retrieveJobWithAuditInfo(pageNumber, pageSize, sortBy, sortOrder, searchTerm));
    }

}
