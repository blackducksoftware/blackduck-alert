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
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

@Component
public class JobEmailAddressValidator {
    private final JobAccessor jobAccessor;
    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public JobEmailAddressValidator(JobAccessor jobAccessor, ProviderDataAccessor providerDataAccessor) {
        this.jobAccessor = jobAccessor;
        this.providerDataAccessor = providerDataAccessor;
    }

    public ValidatedEmailAddresses validate(UUID jobId, Collection<String> emailAddresses) {
        return jobAccessor.getJobById(jobId)
                   .map(DistributionJobModel::getBlackDuckGlobalConfigId)
                   .map(aLong -> validate(aLong, emailAddresses))
                   .orElseGet(() -> new ValidatedEmailAddresses(Set.of(), new HashSet<>(emailAddresses)));
    }

    private ValidatedEmailAddresses validate(Long providerConfigId, Collection<String> emailAddresses) {
        Set<String> validEmailAddresses = new HashSet<>();
        Set<String> invalidEmailAddresses = new HashSet<>();

        for (String emailAddress : emailAddresses) {
            Optional<ProviderUserModel> optionalUser = providerDataAccessor.findFirstUserByEmailAddress(providerConfigId, emailAddress);
            if (optionalUser.isPresent()) {
                validEmailAddresses.add(emailAddress);
            } else {
                invalidEmailAddresses.add(emailAddress);
            }
        }
        return new ValidatedEmailAddresses(validEmailAddresses, invalidEmailAddresses);
    }

}
