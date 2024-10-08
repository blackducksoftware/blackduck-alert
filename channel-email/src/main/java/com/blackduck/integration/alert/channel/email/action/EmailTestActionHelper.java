/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

@Component
public class EmailTestActionHelper {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProviderDataAccessor providerDataAccessor;

    public EmailTestActionHelper(ProviderDataAccessor providerDataAccessor) {
        this.providerDataAccessor = providerDataAccessor;
    }

    public Set<String> createUpdatedEmailAddresses(DistributionJobModel distributionJobModel) throws AlertException {
        DistributionJobDetailsModel distributionJobDetails = distributionJobModel.getDistributionJobDetails();
        EmailJobDetailsModel emailJobDetails = distributionJobDetails.getAs(DistributionJobDetailsModel.EMAIL);

        if (emailJobDetails.isAdditionalEmailAddressesOnly()) {
            return new HashSet<>(emailJobDetails.getAdditionalEmailAddresses());
        }

        Set<String> emailAddresses = new HashSet<>(getProviderEmailAddresses(emailJobDetails, distributionJobModel));

        if (!emailJobDetails.isProjectOwnerOnly()) {
            emailAddresses.addAll(emailJobDetails.getAdditionalEmailAddresses());
        }

        return emailAddresses;
    }

    private List<String> getProviderEmailAddresses(EmailJobDetailsModel emailJobDetails, DistributionJobModel distributionJobModel)
        throws AlertFieldException {
        Long providerConfigId = distributionJobModel.getBlackDuckGlobalConfigId();
        Set<ProviderProject> providerProjects = retrieveProviderProjects(distributionJobModel, providerConfigId);
        if (CollectionUtils.isNotEmpty(providerProjects)) {
            return new ArrayList<>(addEmailAddresses(providerConfigId, providerProjects, distributionJobModel, emailJobDetails));
        } else {
            return Collections.emptyList();
        }
    }

    private boolean doesProjectMatchConfiguration(
        Long providerConfigId,
        ProviderProject currentProject,
        String projectNamePattern,
        String projectVersionNamePattern,
        Set<String> configuredProjectNames
    ) {
        String currentProjectName = currentProject.getName();
        if (currentProjectName.matches(projectNamePattern) || configuredProjectNames.contains(currentProjectName)) {
            return true;
        }

        if (StringUtils.isBlank(projectVersionNamePattern)) {
            return false;
        }

        Pattern compiledVersionPattern = Pattern.compile(projectVersionNamePattern);
        int currentPage = 0;
        AlertPagedModel<String> projectVersionNamesByHref = providerDataAccessor.getProjectVersionNamesByHref(providerConfigId, currentProject.getHref(), currentPage);
        while (currentPage < projectVersionNamesByHref.getTotalPages()) {
            for (String version : projectVersionNamesByHref.getModels()) {
                if (compiledVersionPattern.matcher(version).matches()) {
                    return true;
                }
            }
            currentPage++;
            projectVersionNamesByHref = providerDataAccessor.getProjectVersionNamesByHref(providerConfigId, currentProject.getHref(), currentPage);
        }

        return false;
    }

    private Set<ProviderProject> retrieveProviderProjects(DistributionJobModel distributionJobModel, Long providerConfigId) {
        List<ProviderProject> providerProjects = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId);
        if (distributionJobModel.isFilterByProject()) {
            Set<String> configuredProjects = distributionJobModel.getProjectFilterDetails()
                .stream()
                .map(BlackDuckProjectDetailsModel::getName)
                .collect(Collectors.toSet());
            String projectNamePattern = distributionJobModel.getProjectNamePattern().orElse("");
            String projectVersionNamePattern = distributionJobModel.getProjectVersionNamePattern().orElse("");
            return providerProjects
                .stream()
                .filter(providerProject -> doesProjectMatchConfiguration(providerConfigId, providerProject, projectNamePattern, projectVersionNamePattern, configuredProjects))
                .collect(Collectors.toSet());
        }
        return new HashSet<>(providerProjects);
    }

    private Set<String> addEmailAddresses(Long providerConfigId, Set<ProviderProject> providerProjects, DistributionJobModel distributionJobModel, EmailJobDetailsModel emailJobDetails) throws AlertFieldException {
        boolean projectOwnerOnly = emailJobDetails.isProjectOwnerOnly();

        Set<String> emailAddresses = new HashSet<>();
        Set<String> projectsWithoutEmails = new HashSet<>();
        for (ProviderProject project : providerProjects) {
            Set<String> emailsForProject = retrieveEmailAddressesForProject(providerConfigId, project, projectOwnerOnly);
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
            String errorField = ProviderDescriptor.KEY_CONFIGURED_PROJECT;
            boolean filterByProject = distributionJobModel.isFilterByProject();
            if (!filterByProject) {
                errorField = ProviderDescriptor.KEY_FILTER_BY_PROJECT;
            }
            throw AlertFieldException.singleFieldError(errorField, errorMessage);
        }
        return emailAddresses;
    }

    private Set<String> retrieveEmailAddressesForProject(Long providerConfigId, ProviderProject project, boolean projectOwnerOnly) {
        Set<String> emailAddresses;
        if (projectOwnerOnly) {
            String projectOwnerEmail = project.getProjectOwnerEmail();
            if (StringUtils.isNotBlank(projectOwnerEmail)) {
                emailAddresses = Set.of(projectOwnerEmail);
            } else {
                emailAddresses = Set.of();
            }
        } else {
            emailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(providerConfigId, project.getHref());
        }
        if (emailAddresses.isEmpty()) {
            logger.error("Could not find any email addresses for project: {}", project.getName());
        }
        return emailAddresses;
    }

}
