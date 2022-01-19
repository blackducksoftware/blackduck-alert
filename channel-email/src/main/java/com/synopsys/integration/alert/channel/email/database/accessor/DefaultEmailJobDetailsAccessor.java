/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.database.accessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.channel.email.database.job.EmailJobDetailsEntity;
import com.synopsys.integration.alert.channel.email.database.job.EmailJobDetailsRepository;
import com.synopsys.integration.alert.channel.email.database.job.additional.EmailJobAdditionalEmailAddressEntity;
import com.synopsys.integration.alert.channel.email.database.job.additional.EmailJobAdditionalEmailAddressRepository;
import com.synopsys.integration.alert.common.persistence.accessor.EmailJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DefaultEmailJobDetailsAccessor implements EmailJobDetailsAccessor {
    private final EmailChannelKey channelKey;
    private final EmailJobDetailsRepository emailJobDetailsRepository;
    private final EmailJobAdditionalEmailAddressRepository additionalEmailAddressRepository;

    @Autowired
    public DefaultEmailJobDetailsAccessor(EmailChannelKey channelKey, EmailJobDetailsRepository emailJobDetailsRepository,
        EmailJobAdditionalEmailAddressRepository additionalEmailAddressRepository) {
        this.channelKey = channelKey;
        this.emailJobDetailsRepository = emailJobDetailsRepository;
        this.additionalEmailAddressRepository = additionalEmailAddressRepository;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return channelKey;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailJobDetailsModel> retrieveDetails(UUID jobId) {
        return emailJobDetailsRepository.findById(jobId).map(this::convertToModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public EmailJobDetailsModel saveJobDetails(UUID jobId, DistributionJobDetailsModel jobDetailsModel) {
        EmailJobDetailsModel emailJobDetailsModel = jobDetailsModel.getAs(EmailJobDetailsModel.class);
        return saveConcreteJobDetails(jobId, emailJobDetailsModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public EmailJobDetailsModel saveConcreteJobDetails(UUID jobId, EmailJobDetailsModel jobDetails) {
        EmailJobDetailsEntity jobDetailsToSave = new EmailJobDetailsEntity(
            jobId,
            jobDetails.getSubjectLine().orElse(null),
            jobDetails.isProjectOwnerOnly(),
            jobDetails.isAdditionalEmailAddressesOnly(),
            jobDetails.getAttachmentFileType()
        );
        EmailJobDetailsEntity savedJobDetails = emailJobDetailsRepository.save(jobDetailsToSave);

        additionalEmailAddressRepository.bulkDeleteByJobId(jobId);
        List<EmailJobAdditionalEmailAddressEntity> additionalEmailAddressEntitiesToSave = jobDetails.getAdditionalEmailAddresses()
            .stream()
            .map(emailAddress -> new EmailJobAdditionalEmailAddressEntity(jobId, emailAddress))
            .collect(Collectors.toList());
        List<EmailJobAdditionalEmailAddressEntity> savedAdditionalEmailAddressEntities = additionalEmailAddressRepository.saveAll(additionalEmailAddressEntitiesToSave);
        savedJobDetails.setEmailJobAdditionalEmailAddresses(savedAdditionalEmailAddressEntities);

        return convertToModel(savedJobDetails);
    }

    public List<String> retrieveAdditionalEmailAddressesForJob(UUID jobId) {
        return additionalEmailAddressRepository.findByJobId(jobId)
                   .stream()
                   .map(EmailJobAdditionalEmailAddressEntity::getEmailAddress)
                   .collect(Collectors.toList());
    }

    private EmailJobDetailsModel convertToModel(EmailJobDetailsEntity details) {
        List<String> additionalEmailAddresses = additionalEmailAddressRepository.findByJobId(details.getJobId())
                                                    .stream()
                                                    .map(EmailJobAdditionalEmailAddressEntity::getEmailAddress)
                                                    .collect(Collectors.toList());
        return new EmailJobDetailsModel(
            details.getJobId(),
            details.getSubjectLine(),
            details.getProjectOwnerOnly(),
            details.getAdditionalEmailAddressesOnly(),
            details.getAttachmentFileType(),
            additionalEmailAddresses
        );
    }

}
