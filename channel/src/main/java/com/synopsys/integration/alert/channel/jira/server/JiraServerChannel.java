/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageContentConverter;
import com.synopsys.integration.alert.common.channel.IssueTrackerChannel;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraServerChannel extends IssueTrackerChannel {
    private final JiraMessageContentConverter jiraContentConverter;
    private final JiraServerContextBuilder jiraServerContextBuilder;

    @Autowired
    public JiraServerChannel(Gson gson, AuditAccessor auditAccessor, JiraMessageContentConverter jiraContentConverter, EventManager eventManager,
        JiraServerContextBuilder jiraServerContextBuilder) {
        super(ChannelKey.JIRA_SERVER, gson, auditAccessor, eventManager);
        this.jiraContentConverter = jiraContentConverter;
        this.jiraServerContextBuilder = jiraServerContextBuilder;
    }

    @Override
    protected IssueTrackerContext getIssueTrackerContext(DistributionEvent event) {
        ConfigurationModel globalConfig = event.getChannelGlobalConfig()
                                              .filter(ConfigurationModel::isConfiguredFieldsNotEmpty)
                                              .orElseThrow(() -> new AlertRuntimeException(new AlertConfigurationException("Missing Jira Server global configuration")));
        return jiraServerContextBuilder.build(globalConfig, event.getDistributionJobModel());
    }

    @Override
    protected List<IssueTrackerRequest> createRequests(IssueTrackerContext context, DistributionEvent event) throws IntegrationException {
        return jiraContentConverter.createRequests(context.getIssueConfig(), event.getContent());
    }

    @Override
    public IssueTrackerResponse sendRequests(IssueTrackerContext context, List<IssueTrackerRequest> requests) throws IntegrationException {
        JiraServerRequestDelegator jiraServerService = new JiraServerRequestDelegator(getGson(), context);
        return jiraServerService.sendRequests(requests);
    }

}
