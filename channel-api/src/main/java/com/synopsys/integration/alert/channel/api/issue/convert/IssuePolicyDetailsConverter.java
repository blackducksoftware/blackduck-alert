/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.convert;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.channel.api.convert.ChannelMessageFormatter;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;

public class IssuePolicyDetailsConverter {
    private static final String LABEL_POLICY = "Policy: ";
    private static final String LABEL_SEVERITY = "Severity: ";

    private final ChannelMessageFormatter formatter;

    public IssuePolicyDetailsConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public List<String> createPolicyDetailsSectionPieces(IssuePolicyDetails policyDetails) {
        List<String> policyDetailsSectionPieces = new LinkedList<>();

        policyDetailsSectionPieces.add(formatter.encode(LABEL_POLICY));
        policyDetailsSectionPieces.add(formatter.encode(policyDetails.getName()));
        policyDetailsSectionPieces.add(formatter.getLineSeparator());
        policyDetailsSectionPieces.add(formatter.encode(LABEL_SEVERITY));
        policyDetailsSectionPieces.add(formatter.encode(policyDetails.getSeverity().name()));

        return policyDetailsSectionPieces;
    }

}
