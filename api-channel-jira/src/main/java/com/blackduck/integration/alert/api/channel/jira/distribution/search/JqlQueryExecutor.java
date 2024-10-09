package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;

public interface JqlQueryExecutor {
    List<JiraSearcherResponseModel> executeQuery(String jql) throws AlertException;

}
