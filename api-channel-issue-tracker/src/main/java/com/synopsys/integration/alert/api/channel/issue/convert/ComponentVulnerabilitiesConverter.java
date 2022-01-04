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
import java.util.Optional;

import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

public class ComponentVulnerabilitiesConverter {
    private static final String LABEL_VULNERABILITIES_SECTION = "Current Vulnerabilities: ";
    private static final String VALUE_NO_CURRENT_VULNERABILITIES = "None";

    private static final String LABEL_CRITICAL = "Critical";
    private static final String LABEL_HIGH = "High";
    private static final String LABEL_MEDIUM = "Medium";
    private static final String LABEL_LOW = "Low";

    private final ChannelMessageFormatter formatter;

    private final String encodedLabelVulnerabilitiesSection;
    private final String encodedValueNoCurrentVulnerabilities;
    private final String encodedLabelCritical;
    private final String encodedLabelHigh;
    private final String encodedLabelMedium;
    private final String encodedLabelLow;

    private final String encodedLeftBracket;
    private final String encodedRightBracket;

    public ComponentVulnerabilitiesConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;

        this.encodedLabelVulnerabilitiesSection = formatter.encode(LABEL_VULNERABILITIES_SECTION);
        this.encodedValueNoCurrentVulnerabilities = formatter.encode(VALUE_NO_CURRENT_VULNERABILITIES);
        this.encodedLabelCritical = createEncodedLabel(formatter, LABEL_CRITICAL);
        this.encodedLabelHigh = createEncodedLabel(formatter, LABEL_HIGH);
        this.encodedLabelMedium = createEncodedLabel(formatter, LABEL_MEDIUM);
        this.encodedLabelLow = createEncodedLabel(formatter, LABEL_LOW);

        this.encodedLeftBracket = formatter.encode("[ ");
        this.encodedRightBracket = formatter.encode(" ] ");
    }

    public List<String> createComponentVulnerabilitiesSectionPieces(ComponentVulnerabilities componentVulnerabilities) {
        List<String> componentVulnerabilitiesSectionPieces = new LinkedList<>();
        componentVulnerabilitiesSectionPieces.add(encodedLabelVulnerabilitiesSection);

        if (componentVulnerabilities.hasVulnerabilities()) {
            componentVulnerabilitiesSectionPieces.add(formatter.getLineSeparator());
            List<String> criticalSection = createSeveritySection(encodedLabelCritical, componentVulnerabilities.getCritical());
            componentVulnerabilitiesSectionPieces.addAll(criticalSection);
            componentVulnerabilitiesSectionPieces.add(formatter.getLineSeparator());

            List<String> highSection = createSeveritySection(encodedLabelHigh, componentVulnerabilities.getHigh());
            componentVulnerabilitiesSectionPieces.addAll(highSection);
            componentVulnerabilitiesSectionPieces.add(formatter.getLineSeparator());

            List<String> mediumSection = createSeveritySection(encodedLabelMedium, componentVulnerabilities.getMedium());
            componentVulnerabilitiesSectionPieces.addAll(mediumSection);
            componentVulnerabilitiesSectionPieces.add(formatter.getLineSeparator());

            List<String> lowSection = createSeveritySection(encodedLabelLow, componentVulnerabilities.getLow());
            componentVulnerabilitiesSectionPieces.addAll(lowSection);

        } else {
            componentVulnerabilitiesSectionPieces.add(encodedValueNoCurrentVulnerabilities);
        }
        return componentVulnerabilitiesSectionPieces;
    }

    private List<String> createSeveritySection(String encodedLabel, List<LinkableItem> vulnerabilities) {
        if (vulnerabilities.isEmpty()) {
            return List.of();
        }

        List<String> severitySectionPieces = new LinkedList<>();
        severitySectionPieces.add(encodedLabel);

        vulnerabilities
            .stream()
            .map(this::convertVulnerabilityToString)
            .forEach(severitySectionPieces::add);

        return severitySectionPieces;
    }

    private String convertVulnerabilityToString(LinkableItem vulnerability) {
        Optional<String> url = vulnerability.getUrl();

        String formattedVulnerability = formatter.encode(vulnerability.getValue());
        if (url.isPresent()) {
            String encodedUrl = formatter.encode(url.get());
            formattedVulnerability = formatter.createLink(formattedVulnerability, encodedUrl);
        }
        return String.format("%s%s%s", encodedLeftBracket, formattedVulnerability, encodedRightBracket);
    }

    private static String createEncodedLabel(ChannelMessageFormatter formatter, String label) {
        return String.format("%s-%s%s:%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), label, formatter.getNonBreakingSpace());
    }

}
