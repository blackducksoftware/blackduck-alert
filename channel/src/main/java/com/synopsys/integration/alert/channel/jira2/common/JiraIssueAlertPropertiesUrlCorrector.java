/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.common;

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
