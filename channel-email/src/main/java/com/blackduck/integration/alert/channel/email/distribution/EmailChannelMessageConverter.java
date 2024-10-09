/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.distribution;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.convert.AbstractChannelMessageConverter;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailChannelMessageConverter extends AbstractChannelMessageConverter<EmailJobDetailsModel, EmailChannelMessageModel> {
    // RFC2822 suggests a max of 78 characters in an email subject line: https://tools.ietf.org/html/rfc2822#section-2.1.1
    public static final int SUBJECT_LINE_MAX_LENGTH = 78;

    @Autowired
    public EmailChannelMessageConverter(EmailChannelMessageFormatter emailChannelMessageFormatter) {
        super(emailChannelMessageFormatter);
    }

    @Override
    protected List<EmailChannelMessageModel> convertSimpleMessageToChannelMessages(EmailJobDetailsModel distributionDetails, SimpleMessage simpleMessage, List<String> messageChunks) {
        String subjectLinePrefix = createSubjectLinePrefix(distributionDetails);
        String subjectLine = String.format("%s%s", subjectLinePrefix, simpleMessage.getSummary());
        subjectLine = StringUtils.abbreviate(subjectLine, SUBJECT_LINE_MAX_LENGTH);
        String messageContent = StringUtils.join(messageChunks, "");

        LinkableItem provider = simpleMessage.getProvider();
        String providerName = provider.getValue();
        String providerUrl = provider.getUrl().orElse("#");

        Optional<ProjectMessage> optionalSource = simpleMessage.getSource();

        EmailChannelMessageModel model;
        if (optionalSource.isPresent()) {
            model = EmailChannelMessageModel.simpleProject(subjectLine, messageContent, providerName, providerUrl, optionalSource.get());
        } else {
            model = EmailChannelMessageModel.simple(subjectLine, messageContent, providerName, providerUrl);
        }
        return List.of(model);
    }

    @Override
    protected List<EmailChannelMessageModel> convertProjectMessageToChannelMessages(EmailJobDetailsModel distributionDetails, ProjectMessage projectMessage, List<String> messageChunks) {
        String subjectLine = createSubjectLine(distributionDetails, projectMessage);
        String messageContent = StringUtils.join(messageChunks, "");
        LinkableItem provider = projectMessage.getProvider();

        EmailChannelMessageModel model = EmailChannelMessageModel.standardProject(subjectLine, messageContent, provider.getValue(), provider.getUrl().orElse("#"), projectMessage);
        return List.of(model);
    }

    private String createSubjectLine(EmailJobDetailsModel distributionDetails, ProjectMessage projectMessage) {
        LinkableItem project = projectMessage.getProject();
        String subjectLinePrefix = createSubjectLinePrefix(distributionDetails);
        String subjectLine = String.format("%s%s | %s", subjectLinePrefix, projectMessage.getMessageReason().name(), project.getValue());

        Optional<String> projectVersionName = projectMessage.getProjectVersion()
                                                  .map(LinkableItem::getValue);
        if (projectVersionName.isPresent()) {
            subjectLine += String.format("[%s]", projectVersionName.get());
        }

        return StringUtils.abbreviate(subjectLine, SUBJECT_LINE_MAX_LENGTH);
    }

    private String createSubjectLinePrefix(EmailJobDetailsModel distributionDetails) {
        return distributionDetails.getSubjectLine().map(txt -> String.format("%s | ", txt)).orElse("");
    }

}
