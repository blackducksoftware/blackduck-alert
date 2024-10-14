/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;

public interface JqlQueryExecutor {
    List<JiraSearcherResponseModel> executeQuery(String jql) throws AlertException;

}
