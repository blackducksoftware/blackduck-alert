/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

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
