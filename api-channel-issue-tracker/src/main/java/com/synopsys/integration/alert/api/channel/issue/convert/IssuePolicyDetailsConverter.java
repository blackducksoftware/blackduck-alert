/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.convert;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;

public class IssuePolicyDetailsConverter {
    private static final String LABEL_POLICY = "Policy: ";
    private static final String LABEL_SEVERITY = "Severity: ";
    private static final String LABEL_DESCRIPTION = "Policy Description: ";

    private final ChannelMessageFormatter formatter;
    private final ComponentVulnerabilitiesConverter componentVulnerabilitiesConverter;

    public IssuePolicyDetailsConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
        this.componentVulnerabilitiesConverter = new ComponentVulnerabilitiesConverter(formatter);
    }

    public List<String> createPolicyDetailsSectionPieces(IssueBomComponentDetails bomComponentDetails, IssuePolicyDetails policyDetails) {
        List<String> policyDetailsSectionPieces = new LinkedList<>();

        policyDetailsSectionPieces.add(formatter.encode(LABEL_POLICY));
        policyDetailsSectionPieces.add(formatter.encode(policyDetails.getName()));
        policyDetailsSectionPieces.add(formatter.getLineSeparator());
        policyDetailsSectionPieces.add(formatter.encode(LABEL_SEVERITY));
        policyDetailsSectionPieces.add(formatter.encode(policyDetails.getSeverity().getPolicyLabel()));

        List<String> policyDescriptionSection = createPolicyDescription(bomComponentDetails, policyDetails);
        policyDetailsSectionPieces.addAll(policyDescriptionSection);

        List<String> additionalPolicyDetailsSections = createAdditionalPolicyDetailsSections(bomComponentDetails, policyDetails);
        if (!additionalPolicyDetailsSections.isEmpty()) {
            policyDetailsSectionPieces.add(formatter.getLineSeparator());
            policyDetailsSectionPieces.add(formatter.getSectionSeparator());
            policyDetailsSectionPieces.add(formatter.getLineSeparator());
            policyDetailsSectionPieces.addAll(additionalPolicyDetailsSections);
        }

        return policyDetailsSectionPieces;
    }

    private List<String> createAdditionalPolicyDetailsSections(IssueBomComponentDetails bomComponentDetails, IssuePolicyDetails policyDetails) {
        boolean isVulnerabilityPolicy = bomComponentDetails.getRelevantPolicies()
            .stream()
            .filter(policy -> policy.getPolicyName().equals(policyDetails.getName()))
            .anyMatch(ComponentPolicy::isVulnerabilityPolicy);
        if (isVulnerabilityPolicy) {
            return componentVulnerabilitiesConverter.createComponentVulnerabilitiesSectionPieces(bomComponentDetails.getComponentVulnerabilities());
        }
        return List.of();
    }

    private List<String> createPolicyDescription(IssueBomComponentDetails bomComponentDetails, IssuePolicyDetails policyDetails) {
        List<ComponentPolicy> policies = bomComponentDetails.getRelevantPolicies();
        if (policies.isEmpty()) {
            return List.of();
        }

        String policyName = policyDetails.getName();
        // Blackduck does not allow duplicate policy names, we only expect one policy to ever match
        return policies.stream()
            .filter(policy -> policyName.equals(policy.getPolicyName()))
            .findAny()
            .flatMap(ComponentPolicy::getDescription)
            .map(this::addPolicyDescriptionPieces)
            .orElse(List.of());
    }

    private List<String> addPolicyDescriptionPieces(String description) {
        List<String> descriptionPieces = new LinkedList<>();
        descriptionPieces.add(formatter.getLineSeparator());
        descriptionPieces.add(formatter.encode(LABEL_DESCRIPTION));
        descriptionPieces.add(formatter.encode(description));
        return descriptionPieces;
    }

}
