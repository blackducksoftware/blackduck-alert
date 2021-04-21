/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

public class ComponentVulnerabilitiesConverter {
    private static final String LABEL_VULNERABILITIES_SECTION = "Current Vulnerabilities: ";
    private static final String VALUE_NO_CURRENT_VULNERABILITIES = "None";

    private static final String LABEL_CRITICAL = "Critical: ";
    private static final String LABEL_HIGH = "High: ";
    private static final String LABEL_MEDIUM = "Medium: ";
    private static final String LABEL_LOW = "Low: ";

    private final ChannelMessageFormatter formatter;

    public ComponentVulnerabilitiesConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
        // TODO consider initializing encoded labels here
    }

    public List<String> createComponentVulnerabilitiesSectionPieces(ComponentVulnerabilities componentVulnerabilities) {
        List<String> componentVulnerabilitiesSectionPieces = new LinkedList<>();
        componentVulnerabilitiesSectionPieces.add(formatter.encode(LABEL_VULNERABILITIES_SECTION));

        if (componentVulnerabilities.hasVulnerabilities()) {
            componentVulnerabilitiesSectionPieces.add(formatter.getLineSeparator());

            List<String> criticalSection = createSeveritySection(LABEL_CRITICAL, componentVulnerabilities.getCritical());
            componentVulnerabilitiesSectionPieces.addAll(criticalSection);

            List<String> highSection = createSeveritySection(LABEL_HIGH, componentVulnerabilities.getHigh());
            componentVulnerabilitiesSectionPieces.addAll(highSection);

            List<String> mediumSection = createSeveritySection(LABEL_MEDIUM, componentVulnerabilities.getMedium());
            componentVulnerabilitiesSectionPieces.addAll(mediumSection);

            List<String> lowSection = createSeveritySection(LABEL_LOW, componentVulnerabilities.getLow());
            componentVulnerabilitiesSectionPieces.addAll(lowSection);

        } else {
            componentVulnerabilitiesSectionPieces.add(formatter.encode(VALUE_NO_CURRENT_VULNERABILITIES));
        }

        componentVulnerabilitiesSectionPieces.add(formatter.getLineSeparator());
        return componentVulnerabilitiesSectionPieces;
    }

    private List<String> createSeveritySection(String label, List<LinkableItem> vulnerabilities) {
        if (vulnerabilities.isEmpty()) {
            return List.of();
        }

        List<String> severitySectionPieces = new LinkedList<>();
        severitySectionPieces.add(formatter.encode(label));

        vulnerabilities
            .stream()
            .map(this::convertVulnerabilityToString)
            .forEach(severitySectionPieces::add);

        return severitySectionPieces;
    }

    private String convertVulnerabilityToString(LinkableItem vulnerability) {
        String encodedValue = formatter.encode(vulnerability.getValue());
        Optional<String> url = vulnerability.getUrl();

        String formattedVulnerability = encodedValue;
        if (url.isPresent()) {
            String encodedUrl = formatter.encode(url.get());
            formattedVulnerability = formatter.createLink(encodedValue, encodedUrl);
        }

        String encodedLeftBracket = formatter.encode("[ ");
        String encodedRightBracket = formatter.encode(" ] ");
        return String.format("%s%s%s", encodedLeftBracket, formattedVulnerability, encodedRightBracket);
    }

}
