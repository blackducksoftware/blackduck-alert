/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.util;

import org.apache.commons.lang3.StringUtils;

public class UrlUtils {
    public static String appendTrailingSlashIfNoneExists(String originalUrl) {
        String correctedUrl = "";
        if (StringUtils.isNotBlank(originalUrl)) {
            correctedUrl = originalUrl.trim();
            if (!correctedUrl.endsWith("/")) {
                correctedUrl += "/";
            }
        }
        return correctedUrl;
    }

}
