/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.http;

import org.apache.commons.lang3.StringUtils;

public class AzureApiVersionAppender {
    public static final String AZURE_API_VERSION_QUERY_PARAM_NAME = "api-version";

    public static final String AZURE_API_VERSION_5_0 = "5.0";
    public static final String AZURE_API_VERSION_5_1 = "5.1";
    public static final String AZURE_API_VERSION_5_1_PREVIEW_1 = "5.1-preview.1";
    public static final String AZURE_API_VERSION_5_1_PREVIEW_2 = "5.1-preview.2";
    public static final String AZURE_API_VERSION_5_1_PREVIEW_3 = "5.1-preview.3";

    public String appendApiVersion(String spec, String apiVersion) {
        char queryParamSeparator = StringUtils.contains(spec, '?') ? '&' : '?';

        StringBuilder appendedSpec = new StringBuilder(spec);
        appendedSpec.append(queryParamSeparator);
        appendedSpec.append(AZURE_API_VERSION_QUERY_PARAM_NAME);
        appendedSpec.append("=");
        appendedSpec.append(apiVersion);

        return appendedSpec.toString();
    }

    public String appendApiVersion5_0(String spec) {
        return appendApiVersion(spec, AZURE_API_VERSION_5_0);
    }

    public String appendApiVersion5_1(String spec) {
        return appendApiVersion(spec, AZURE_API_VERSION_5_1);
    }

    public String appendApiVersion5_1_Preview_1(String spec) {
        return appendApiVersion(spec, AZURE_API_VERSION_5_1_PREVIEW_1);
    }

    public String appendApiVersion5_1_Preview_2(String spec) {
        return appendApiVersion(spec, AZURE_API_VERSION_5_1_PREVIEW_2);
    }

    public String appendApiVersion5_1_Preview_3(String spec) {
        return appendApiVersion(spec, AZURE_API_VERSION_5_1_PREVIEW_3);
    }

}
