/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email2.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailAddressHandler;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailTestActionHelper {
    private final EmailAddressHandler emailAddressHandler;
    private final ProviderDataAccessor providerDataAccessor;

    public EmailTestActionHelper(EmailAddressHandler emailAddressHandler, ProviderDataAccessor providerDataAccessor) {
        this.emailAddressHandler = emailAddressHandler;
        this.providerDataAccessor = providerDataAccessor;
    }

    public Set<String> createUpdatedEmailAddresses(DistributionJobModel distributionJobModel, @Nullable String destination) throws AlertException {
        Set<String> emailAddresses = new HashSet<>();
        if (StringUtils.isNotBlank(destination)) {
            emailAddresses.add(destination);
        }

        DistributionJobDetailsModel distributionJobDetails = distributionJobModel.getDistributionJobDetails();
        EmailJobDetailsModel emailJobDetails = distributionJobDetails.getAs(DistributionJobDetailsModel.EMAIL);

        Long providerConfigId = distributionJobModel.getBlackDuckGlobalConfigId();
        boolean onlyAdditionalEmails = emailJobDetails.isAdditionalEmailAddressesOnly();

        if (null != providerConfigId && !onlyAdditionalEmails) {
            Set<ProviderProject> providerProjects = retrieveProviderProjects(distributionJobModel, providerConfigId);
            if (CollectionUtils.isNotEmpty(providerProjects)) {
                Set<String> providerEmailAddresses = addEmailAddresses(providerConfigId, providerProjects, distributionJobModel, emailJobDetails);
                emailAddresses.addAll(providerEmailAddresses);
            }
        }
        return emailAddresses;
    }

    private boolean doesProjectMatchConfiguration(String currentProjectName, String projectNamePattern, Set<String> configuredProjectNames) {
        return currentProjectName.matches(projectNamePattern) || configuredProjectNames.contains(currentProjectName);
    }

    private Set<ProviderProject> retrieveProviderProjects(DistributionJobModel distributionJobModel, Long providerConfigId) {
        List<ProviderProject> providerProjects = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId);
        if (distributionJobModel.isFilterByProject()) {
            Set<String> configuredProjects = distributionJobModel.getProjectFilterDetails()
                                                 .stream()
                                                 .map(BlackDuckProjectDetailsModel::getName)
                                                 .collect(Collectors.toSet());
            String projectNamePattern = distributionJobModel.getProjectNamePattern().orElse("");
            return providerProjects
                       .stream()
                       .filter(providerProject -> doesProjectMatchConfiguration(providerProject.getName(), projectNamePattern, configuredProjects))
                       .collect(Collectors.toSet());
        }
        return new HashSet<>(providerProjects);
    }

    private Set<String> addEmailAddresses(Long providerConfigId, Set<ProviderProject> providerProjects, DistributionJobModel distributionJobModel, EmailJobDetailsModel emailJobDetails) throws AlertFieldException {
        boolean projectOwnerOnly = emailJobDetails.isProjectOwnerOnly();

        Set<String> emailAddresses = new HashSet<>();
        Set<String> projectsWithoutEmails = new HashSet<>();
        for (ProviderProject project : providerProjects) {
            Set<String> emailsForProject = emailAddressHandler.getEmailAddressesForProject(providerConfigId, project, projectOwnerOnly);
            if (emailsForProject.isEmpty()) {
                projectsWithoutEmails.add(project.getName());
            }
            emailAddresses.addAll(emailsForProject);
        }
        if (!projectsWithoutEmails.isEmpty()) {
            String projects = StringUtils.join(projectsWithoutEmails, ", ");
            String errorMessage;
            if (projectOwnerOnly) {
                errorMessage = String.format("Could not find Project owners for the projects: %s", projects);
            } else {
                errorMessage = String.format("Could not find any email addresses for the projects: %s", projects);
            }
            String errorField = ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT;
            boolean filterByProject = distributionJobModel.isFilterByProject();
            if (!filterByProject) {
                errorField = ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT;
            }
            throw AlertFieldException.singleFieldError(errorField, errorMessage);
        }
        return emailAddresses;
    }

}
