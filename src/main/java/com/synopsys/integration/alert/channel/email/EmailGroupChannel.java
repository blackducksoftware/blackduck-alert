/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.email;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.channel.model.EmailGlobalConfig;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component(value = EmailGroupChannel.COMPONENT_NAME)
public class EmailGroupChannel extends DistributionChannel<EmailGlobalConfigEntity> {
    public final static String COMPONENT_NAME = "channel_email";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // FIXME This will no longer be needed with our new DB Field tables. We'll want to remove these and be sent all the data we need.
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;

    // TODO we will pass a ValueId through the email configuration. This will reference all emails that should be sent in this case. We'll Access the DB here to retrieve this info.

    @Autowired
    public EmailGroupChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditUtility auditUtility, final EmailGlobalRepository emailRepository,
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor) {
        super(gson, alertProperties, blackDuckProperties, auditUtility, emailRepository);

        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
    }

    @Override
    public String getDistributionType() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public void sendMessage(final DistributionEvent event) throws IntegrationException {
        final EmailGlobalConfigEntity globalConfigEntity = getGlobalConfigEntity();
        if (!isValidGlobalConfigEntity(globalConfigEntity)) {
            throw new AlertException("ERROR: Missing global config.");
        }

        final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) event.getCommonDistributionConfig();
        final Set<String> emailAddresses = populateEmails(emailDistributionConfig, event.getContent().getValue());
        emailDistributionConfig.setEmailAddresses(emailAddresses);
        final EmailProperties emailProperties = new EmailProperties(globalConfigEntity);
        sendMessage(emailProperties, emailDistributionConfig.getEmailAddresses(), emailDistributionConfig.getEmailSubjectLine(), event.getProvider(), event.getFormatType(), event.getContent(), "ProjectName");
    }

    private Set<String> populateEmails(final EmailDistributionConfig emailDistributionConfig, final String projectName) {
        Set<String> emailAddresses = emailDistributionConfig.getEmailAddresses();
        if (null != emailAddresses && !emailAddresses.isEmpty()) {
            return emailAddresses;
        }
        final BlackDuckProjectEntity projectEntity = blackDuckProjectRepositoryAccessor.findByName(projectName);
        emailAddresses = getEmailAddressesForProject(projectEntity, emailDistributionConfig.getProjectOwnerOnly());
        if (emailAddresses.isEmpty()) {
            logger.error("Could not find any email addresses for project: {}. Job: {}", projectName, emailDistributionConfig.getName());
        }
        return emailAddresses;
    }

    @Override
    public String testGlobalConfig(final TestConfigModel testConfig) throws IntegrationException {
        Set<String> emailAddresses = null;
        final String testEmailAddress = testConfig.getDestination().orElse(null);
        if (StringUtils.isNotBlank(testEmailAddress)) {
            emailAddresses = Collections.singleton(testEmailAddress);
        }
        final EmailProperties emailProperties = new EmailProperties((EmailGlobalConfig) testConfig.getRestModel());
        final AggregateMessageContent messageContent = new AggregateMessageContent("Message Content", "Test from Alert", Collections.emptyList());
        sendMessage(emailProperties, emailAddresses, "Test from Alert", "Global Configuration", "", messageContent, "N/A");
        return "Success!";
    }

    public void sendMessage(final EmailProperties emailProperties, final Set<String> emailAddresses, final String subjectLine, final String provider, final String formatType, final AggregateMessageContent content,
        final String blackDuckProjectName) throws IntegrationException {
        if (null == emailAddresses || emailAddresses.isEmpty()) {
            throw new AlertException("ERROR: Could not determine what email addresses to send this content to.");
        }
        try {
            final EmailMessagingService emailService = new EmailMessagingService(getAlertProperties(), emailProperties);

            final HashMap<String, Object> model = new HashMap<>();

            final String contentTitle = provider;
            model.put("content", content);
            model.put("contentTitle", contentTitle);
            model.put("emailCategory", formatType);
            model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), subjectLine);
            final Optional<String> optionalBlackDuckUrl = getBlackDuckProperties().getBlackDuckUrl();
            if (optionalBlackDuckUrl.isPresent()) {
                model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_SERVER_URL.getPropertyKey(), StringUtils.trimToEmpty(optionalBlackDuckUrl.get()));
            }
            model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_PROJECT_NAME.getPropertyKey(), blackDuckProjectName);

            model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
            model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));

            for (final String emailAddress : emailAddresses) {
                final EmailTarget emailTarget = new EmailTarget(emailAddress, "message_content.ftl", model);
                emailService.sendEmailMessage(emailTarget);
            }
        } catch (final IOException ex) {
            throw new AlertException(ex);
        }
    }

    private boolean isValidGlobalConfigEntity(final EmailGlobalConfigEntity globalConfigEntity) {
        return globalConfigEntity != null && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpHost()) && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpFrom());
    }

    public Set<String> getEmailAddressesForProject(final BlackDuckProjectEntity blackDuckProjectEntity, final boolean projectOwnerOnly) {
        if (null == blackDuckProjectEntity) {
            return Collections.emptySet();
        }
        final Set<String> emailAddresses;
        if (projectOwnerOnly) {
            emailAddresses = new HashSet<>();
            if (StringUtils.isNotBlank(blackDuckProjectEntity.getProjectOwnerEmail())) {
                emailAddresses.add(blackDuckProjectEntity.getProjectOwnerEmail());
            }
        } else {
            final List<UserProjectRelation> userProjectRelations = userProjectRelationRepositoryAccessor.findByBlackDuckProjectId(blackDuckProjectEntity.getId());
            emailAddresses = userProjectRelations
                                 .stream()
                                 .map(userProjectRelation -> blackDuckUserRepositoryAccessor.readEntity(userProjectRelation.getBlackDuckUserId()))
                                 .filter(userEntity -> userEntity.isPresent())
                                 .map(databaseEntity -> (BlackDuckUserEntity) databaseEntity.get())
                                 .filter(userEntity -> StringUtils.isNotBlank(userEntity.getEmailAddress()))
                                 .map(userEntity -> userEntity.getEmailAddress())
                                 .collect(Collectors.toSet());
        }
        return emailAddresses;
    }

    public boolean doesProjectNameMatchThePattern(final String currentProjectName, final String projectNamePattern) {
        return currentProjectName.matches(projectNamePattern);
    }

    public boolean doesProjectNameMatchAConfiguredProject(final String currentProjectName, final List<String> configuredProjectNames) {
        return configuredProjectNames.contains(currentProjectName);
    }

}
