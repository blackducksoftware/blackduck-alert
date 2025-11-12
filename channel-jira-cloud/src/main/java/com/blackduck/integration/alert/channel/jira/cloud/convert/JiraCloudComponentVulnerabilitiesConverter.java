/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class JiraCloudComponentVulnerabilitiesConverter {
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

    public JiraCloudComponentVulnerabilitiesConverter(ChannelMessageFormatter formatter) {
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

    public void createComponentVulnerabilitiesSectionPieces(ComponentVulnerabilities componentVulnerabilities, AtlassianDocumentBuilder documentBuilder) {
        documentBuilder.addTextNode(encodedLabelVulnerabilitiesSection);

        if (componentVulnerabilities.hasVulnerabilities()) {
            documentBuilder.addTextNode(formatter.getLineSeparator());
            createSeveritySection(documentBuilder, encodedLabelCritical, componentVulnerabilities.getCritical());
            documentBuilder.addTextNode(formatter.getLineSeparator());

            createSeveritySection(documentBuilder, encodedLabelHigh, componentVulnerabilities.getHigh());
            documentBuilder.addTextNode(formatter.getLineSeparator());

            createSeveritySection(documentBuilder, encodedLabelMedium, componentVulnerabilities.getMedium());
            documentBuilder.addTextNode(formatter.getLineSeparator());

            createSeveritySection(documentBuilder, encodedLabelLow, componentVulnerabilities.getLow());

        } else {
            documentBuilder.addTextNode(encodedValueNoCurrentVulnerabilities);
        }
    }

    private void createSeveritySection(AtlassianDocumentBuilder documentBuilder, String encodedLabel, List<LinkableItem> vulnerabilities) {
        if (vulnerabilities.isEmpty()) {
            return;
        }

        documentBuilder.addTextNode(encodedLabel);

        vulnerabilities
            .forEach(item -> convertVulnerabilityToString(documentBuilder, item));
    }

    private void convertVulnerabilityToString(AtlassianDocumentBuilder documentBuilder, LinkableItem vulnerability) {
        Optional<String> url = vulnerability.getUrl();

        String encodedUrl = url.orElse(null);
        String text = String.format("%s%s%s", encodedLeftBracket, formatter.encode(vulnerability.getValue()), encodedRightBracket);
        documentBuilder.addTextNode(text, encodedUrl);
    }

    private static String createEncodedLabel(ChannelMessageFormatter formatter, String label) {
        return String.format("%s-%s%s:%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), label, formatter.getNonBreakingSpace());
    }

}
