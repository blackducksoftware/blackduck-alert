package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class JiraIssueAlertPropertiesUrlCorrector {
    public static Optional<String> correctUrl(String originalUrl) {
        if (StringUtils.isNotBlank(originalUrl)) {
            String correctedUrl = originalUrl.trim();
            if (!correctedUrl.endsWith("/")) {
                correctedUrl += "/";
            }
            return Optional.of(correctedUrl);
        }
        return Optional.empty();
    }

    private JiraIssueAlertPropertiesUrlCorrector() {
    }

}
