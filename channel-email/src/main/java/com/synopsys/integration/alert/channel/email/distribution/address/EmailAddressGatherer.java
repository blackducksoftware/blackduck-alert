/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution.address;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailAddressGatherer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JobAccessor jobAccessor;
    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public EmailAddressGatherer(JobAccessor jobAccessor, ProviderDataAccessor providerDataAccessor) {
        this.jobAccessor = jobAccessor;
        this.providerDataAccessor = providerDataAccessor;
    }

    public Set<String> gatherEmailAddresses(EmailJobDetailsModel emailJobDetails, Collection<String> projectHrefs) {
        Set<String> emailAddresses = new HashSet<>();

        boolean projectOwnerOnly = emailJobDetails.isProjectOwnerOnly();
        if (!projectOwnerOnly) {
            emailAddresses.addAll(emailJobDetails.getAdditionalEmailAddresses());
        }

        boolean additionalEmailAddressesOnly = emailJobDetails.isAdditionalEmailAddressesOnly();
        if (!additionalEmailAddressesOnly) {
            Optional<Long> optionalBlackDuckGlobalConfigId = jobAccessor.getJobById(emailJobDetails.getJobId())
                .map(DistributionJobModel::getBlackDuckGlobalConfigId);
            if (optionalBlackDuckGlobalConfigId.isPresent()) {
                Set<String> providerEmailAddresses = gatherProviderEmailAddresses(projectOwnerOnly, projectHrefs, optionalBlackDuckGlobalConfigId.get());
                emailAddresses.addAll(providerEmailAddresses);
            }
        }
        return emailAddresses;
    }

    private Set<String> gatherProviderEmailAddresses(boolean projectOwnerOnly, Collection<String> projectHrefs, Long blackDuckGlobalConfigId) {
        Set<String> providerEmailAddresses = new HashSet<>();

        Set<String> projectEmailAddresses = gatherProjectEmailAddresses(projectOwnerOnly, blackDuckGlobalConfigId, projectHrefs);
        providerEmailAddresses.addAll(projectEmailAddresses);

        if (providerEmailAddresses.isEmpty()) {
            retrieveProviderConfigEmailAddress(blackDuckGlobalConfigId)
                .ifPresent(projectEmailAddresses::add);
        }
        return providerEmailAddresses;
    }

    private Set<String> gatherProjectEmailAddresses(boolean projectOwnerOnly, Long providerConfigId, Collection<String> projectHrefs) {
        if (projectOwnerOnly) {
            return retrieveProjectOwnerEmailAddresses(providerConfigId, projectHrefs);
        } else {
            return retrieveProjectUserEmailAddresses(providerConfigId, projectHrefs);
        }
    }

    private Set<String> retrieveProjectOwnerEmailAddresses(Long providerConfigId, Collection<String> projectHrefs) {
        Set<String> projectOwnerEmailAddresses = new HashSet<>();
        for (String href : projectHrefs) {
            providerDataAccessor.getProjectByHref(providerConfigId, href)
                .map(ProviderProject::getProjectOwnerEmail)
                .ifPresent(projectOwnerEmailAddresses::add);
        }
        return projectOwnerEmailAddresses;
    }

    private Set<String> retrieveProjectUserEmailAddresses(Long providerConfigId, Collection<String> projectHrefs) {
        Set<String> projectUserEmailAddresses = new HashSet<>();
        for (String href : projectHrefs) {
            Set<String> emailsForProject = providerDataAccessor.getEmailAddressesForProjectHref(providerConfigId, href);
            projectUserEmailAddresses.addAll(emailsForProject);
        }
        return projectUserEmailAddresses;
    }

    private Optional<String> retrieveProviderConfigEmailAddress(Long providerConfigId) {
        try {
            ProviderUserModel providerConfigUser = providerDataAccessor.getProviderConfigUserById(providerConfigId);
            return Optional.of(providerConfigUser.getEmailAddress());
        } catch (AlertConfigurationException e) {
            logger.warn("Failed to retrieve provider config user", e);
            return Optional.empty();
        }
    }

}
