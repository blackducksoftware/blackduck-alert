/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;

@Component
public class DistributionActions {
    private final DistributionAccessor distributionAccessor;

    @Autowired
    public DistributionActions(DistributionAccessor distributionAccessor) {
        this.distributionAccessor = distributionAccessor;
    }

    public ActionResponse<DistributionWithAuditInfo> retrieveJobWithAuditInfo() {
        DistributionWithAuditInfo distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo();
        return new ActionResponse<>(HttpStatus.ACCEPTED, distributionWithAuditInfo);
    }
}
