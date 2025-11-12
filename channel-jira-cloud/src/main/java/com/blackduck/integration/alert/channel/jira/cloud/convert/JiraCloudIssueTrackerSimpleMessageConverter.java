/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.alert.api.channel.convert.SimpleMessageConverter;
import com.blackduck.integration.alert.api.channel.issue.tracker.convert.IssueTrackerMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.common.channel.message.ChunkedStringBuilderRechunker;
import com.blackduck.integration.alert.common.channel.message.RechunkedModel;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class JiraCloudIssueTrackerSimpleMessageConverter {
    private final IssueTrackerMessageFormatter formatter;
    private final JiraCloudSimpleMessageConverter simpleMessageConverter;

    public JiraCloudIssueTrackerSimpleMessageConverter(IssueTrackerMessageFormatter formatter) {
        this.formatter = formatter;
        this.simpleMessageConverter = new JiraCloudSimpleMessageConverter(formatter);
    }

    public IssueCreationModel convertToIssueCreationModel(SimpleMessage simpleMessage, String jobName, AtlassianDocumentBuilder documentBuilder) {
        LinkableItem provider = simpleMessage.getProvider();
        String rawTitle = String.format("%s[%s] | %s", provider.getLabel(), provider.getValue(), simpleMessage.getSummary());
        String truncatedTitle = StringUtils.truncate(rawTitle, formatter.getMaxTitleLength());

        simpleMessageConverter.convertToFormattedMessageChunks(simpleMessage, jobName, documentBuilder);

        AtlassianDocumentFormatModel description = documentBuilder.buildPrimaryDocument();
        List<AtlassianDocumentFormatModel> additionalComments = documentBuilder.buildAdditionalCommentDocuments();
        return IssueCreationModel.simple(truncatedTitle, simpleMessage.getProvider(), description, additionalComments);
    }

}
