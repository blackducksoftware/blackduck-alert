/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public class ComponentConcernConverter {
    private static final String TRIPLE_STRING_REPLACEMENT = "%s%s%s";

    private static final String VERB_ADDED = " Added";
    private static final String VERB_UPDATED = " Updated";
    private static final String VERB_REMOVED = " Removed";
    private static final String COLON_SPACE = ": ";
    private static final String SPACE_OPEN_PAREN = " (";
    private static final String SPACE_CLOSE_PAREN = ")";
    private static final String VULNERABILITIES_STRING = "Vulnerabilities";
    private static final String VULNERABILITY_COUNT_STRING = "Vulnerability counts";
    private static final String SPACE_DASH_SPACE = "-";
    private static final String BRACKET_LEFT = "[";
    private static final String BRACKET_RIGHT = "]";
    private static final String TEXT_COMPONENT_DELETE = "Component was removed or the version was set.";

    private final ChannelMessageFormatter formatter;

    private final String formattedVerbAdded;
    private final String formattedVerbUpdated;
    private final String formattedVerbRemoved;
    private final String formattedColonSpace;
    private final String formattedOpenParen;
    private final String formattedCloseParen;
    private final String formattedVulnerabilitiesString;
    private final String formattedVulnerabilityCountsString;
    private final String formattedDash;
    private final String formattedBracketLeft;
    private final String formattedBracketRight;

    public ComponentConcernConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;

        this.formattedVerbAdded = formatter.encode(VERB_ADDED);
        this.formattedVerbUpdated = formatter.encode(VERB_UPDATED);
        this.formattedVerbRemoved = formatter.encode(VERB_REMOVED);
        this.formattedColonSpace = formatter.encode(COLON_SPACE);
        this.formattedOpenParen = formatter.encode(SPACE_OPEN_PAREN);
        this.formattedCloseParen = formatter.encode(SPACE_CLOSE_PAREN);
        this.formattedVulnerabilitiesString = formatter.encode(VULNERABILITIES_STRING);
        this.formattedVulnerabilityCountsString = formatter.encode(VULNERABILITY_COUNT_STRING);
        this.formattedDash = formatter.encode(SPACE_DASH_SPACE);
        this.formattedBracketLeft = formatter.encode(BRACKET_LEFT);
        this.formattedBracketRight = formatter.encode(BRACKET_RIGHT);
    }

    public List<String> gatherComponentConcernSectionPieces(List<ComponentConcern> componentConcerns) {
        List<String> componentConcernSectionPieces = new LinkedList<>();
        List<ComponentConcern> vulnerabilityConcerns = new LinkedList<>();
        List<ComponentConcern> estimatedRiskConcerns = new LinkedList<>();

        Set<ComponentConcern> sortedConcerns = new TreeSet<>(componentConcerns);
        for (ComponentConcern componentConcern : sortedConcerns) {
            if (ComponentConcernType.POLICY.equals(componentConcern.getType())) {
                String policySectionPiece = createPolicySectionPiece(componentConcern);
                componentConcernSectionPieces.add(policySectionPiece);
                componentConcernSectionPieces.add(formatter.getLineSeparator());
            } else if (ComponentConcernType.UNKNOWN_VERSION.equals(componentConcern.getType())) {
                estimatedRiskConcerns.add(componentConcern);
            } else {
                vulnerabilityConcerns.add(componentConcern);
            }
        }

        List<String> vulnerabilitySectionPieces = createVulnerabilitySectionPieces(vulnerabilityConcerns);
        List<String> estimatedRiskSectionPieces = createEstimatedRiskSectionPieces(estimatedRiskConcerns);
        componentConcernSectionPieces.addAll(vulnerabilitySectionPieces);
        componentConcernSectionPieces.addAll(estimatedRiskSectionPieces);

        return componentConcernSectionPieces;
    }

    private String createPolicySectionPiece(ComponentConcern policyConcern) {
        String verb = createItemOperationVerb(policyConcern.getOperation());
        ComponentConcernSeverity policySeverity = policyConcern.getSeverity();
        String severityString = "";
        if (!ComponentConcernSeverity.UNSPECIFIED_UNKNOWN.equals(policySeverity)) {
            severityString = String.format(TRIPLE_STRING_REPLACEMENT, formattedOpenParen, formatter.encode(policySeverity.getPolicyLabel()), formattedCloseParen);
        }
        return String.format("%s%s%s%s%s",
            policyConcern.getType().getDisplayName(),
            verb,
            severityString,
            formattedColonSpace,
            formatter.encode(policyConcern.getName())
        );
    }

    private List<String> createVulnerabilitySectionPieces(List<ComponentConcern> vulnerabilityConcerns) {
        if (vulnerabilityConcerns.isEmpty()) {
            return List.of();
        }

        List<ComponentConcern> addedVulnerabilityConcerns = new LinkedList<>();
        List<ComponentConcern> updatedVulnerabilityConcerns = new LinkedList<>();
        List<ComponentConcern> deletedVulnerabilityConcerns = new LinkedList<>();

        for (ComponentConcern vulnerabilityConcern : vulnerabilityConcerns) {
            ItemOperation operation = vulnerabilityConcern.getOperation();
            if (ItemOperation.ADD.equals(operation)) {
                addedVulnerabilityConcerns.add(vulnerabilityConcern);
            } else if (ItemOperation.DELETE.equals(operation)) {
                deletedVulnerabilityConcerns.add(vulnerabilityConcern);
            } else {
                updatedVulnerabilityConcerns.add(vulnerabilityConcern);
            }
        }

        List<String> vulnerabilitySectionPieces = new LinkedList<>();
        vulnerabilitySectionPieces.add(formatter.getSectionSeparator());
        vulnerabilitySectionPieces.add(formatter.getLineSeparator());

        // Add Sections
        List<String> vulnerabilitiesAddedPieces = createVulnerabilityConcernsForOperation(formattedVerbAdded, addedVulnerabilityConcerns);
        vulnerabilitySectionPieces.addAll(vulnerabilitiesAddedPieces);

        List<String> vulnerabilitiesUpdatedPieces = createVulnerabilityConcernsForOperation(formattedVerbUpdated, updatedVulnerabilityConcerns);
        vulnerabilitySectionPieces.addAll(vulnerabilitiesUpdatedPieces);

        List<String> vulnerabilitiesDeletedPieces = createVulnerabilityConcernsForOperation(formattedVerbRemoved, deletedVulnerabilityConcerns);
        vulnerabilitySectionPieces.addAll(vulnerabilitiesDeletedPieces);

        return vulnerabilitySectionPieces;
    }

    private List<String> createVulnerabilityConcernsForOperation(String verb, List<ComponentConcern> vulnerabilityConcerns) {
        if (vulnerabilityConcerns.isEmpty()) {
            return List.of();
        }

        List<String> vulnerabilitiesForOperationSectionPieces = new LinkedList<>();

        vulnerabilitiesForOperationSectionPieces.add(String.format(TRIPLE_STRING_REPLACEMENT, formattedVulnerabilitiesString, verb, formattedColonSpace));

        ComponentConcernSeverity severity = ComponentConcernSeverity.BLOCKER;
        for (ComponentConcern vulnerabilityConcern : vulnerabilityConcerns) {
            ComponentConcernSeverity concernSeverity = vulnerabilityConcern.getSeverity();
            if (!severity.equals(concernSeverity)) {
                severity = concernSeverity;
                vulnerabilitiesForOperationSectionPieces.add(formatter.getLineSeparator());
                vulnerabilitiesForOperationSectionPieces
                    .add(String.format("%s%s%s%s%s", formatter.getNonBreakingSpace(), formattedDash, formatter.getNonBreakingSpace(), formatter.encode(concernSeverity.getVulnerabilityLabel()), formattedColonSpace));
            }
            String vulnerabilityConcernString = createVulnerabilityConcernString(vulnerabilityConcern);
            vulnerabilitiesForOperationSectionPieces.add(vulnerabilityConcernString);
            vulnerabilitiesForOperationSectionPieces.add(formatter.getNonBreakingSpace());
        }

        vulnerabilitiesForOperationSectionPieces.add(formatter.getLineSeparator());
        return vulnerabilitiesForOperationSectionPieces;
    }

    private String createVulnerabilityConcernString(ComponentConcern vulnerabilityConcern) {
        String vulnerabilityConcernString = createComponentNameLinkIfPresent(vulnerabilityConcern);
        return String.format(TRIPLE_STRING_REPLACEMENT, formattedBracketLeft, vulnerabilityConcernString, formattedBracketRight);
    }

    private List<String> createEstimatedRiskSectionPieces(List<ComponentConcern> estimatedRiskConcerns) {
        if (estimatedRiskConcerns.isEmpty()) {
            return List.of();
        }
        boolean isDelete = estimatedRiskConcerns.stream()
            .allMatch(componentConcern -> ItemOperation.DELETE.equals(componentConcern.getOperation()));
        List<String> estimatedRiskForOperationSectionPieces = new LinkedList<>();
        estimatedRiskForOperationSectionPieces.add(formatter.getSectionSeparator());
        estimatedRiskForOperationSectionPieces.add(formatter.getLineSeparator());

        if (isDelete) {
            estimatedRiskForOperationSectionPieces.add(formatter.encode(TEXT_COMPONENT_DELETE));
            estimatedRiskForOperationSectionPieces.add(formatter.getLineSeparator());
        } else {
            estimatedRiskForOperationSectionPieces.add(String.format("%s%s", formattedVulnerabilityCountsString, formattedColonSpace));
            estimatedRiskForOperationSectionPieces.add(formatter.getLineSeparator());
            estimatedRiskForOperationSectionPieces.add(formatter.getLineSeparator());

            for (ComponentConcern componentConcern : estimatedRiskConcerns) {
                estimatedRiskForOperationSectionPieces.add(createEstimatedRiskConcernString(componentConcern));
                estimatedRiskForOperationSectionPieces.add(formatter.getLineSeparator());
            }
        }

        return estimatedRiskForOperationSectionPieces;
    }

    private String createEstimatedRiskConcernString(ComponentConcern estimatedRiskConcern) {
        String severity = formatter.encode(estimatedRiskConcern.getSeverity().getVulnerabilityLabel());
        String countString = String.format(TRIPLE_STRING_REPLACEMENT, formattedOpenParen, formatter.encode(estimatedRiskConcern.getNumericValue().toString()), formattedCloseParen);
        String componentName = createComponentNameLinkIfPresent(estimatedRiskConcern);
        return String.format("%s%s%s %s", severity, formattedColonSpace, countString, componentName);
    }

    private String createComponentNameLinkIfPresent(ComponentConcern componentConcern) {
        String encodedName = formatter.encode(componentConcern.getName());
        String componentNameString = encodedName;

        Optional<String> optionalUrl = componentConcern.getUrl();
        if (optionalUrl.isPresent()) {
            String encodedUrl = formatter.encode(optionalUrl.get());
            componentNameString = formatter.createLink(encodedName, encodedUrl);
        }

        return componentNameString;
    }

    private String createItemOperationVerb(ItemOperation itemOperation) {
        switch (itemOperation) {
            case ADD:
                return formattedVerbAdded;
            case DELETE:
                return formattedVerbRemoved;
            default:
                return formattedVerbUpdated;
        }
    }

}
