/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.email;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressEntity;
import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressRepository;

@Component
public class EmailJobDetailsAccessor {
    private final EmailJobDetailsRepository emailJobDetailsRepository;
    private final EmailJobAdditionalEmailAddressRepository additionalEmailAddressRepository;

    @Autowired
    public EmailJobDetailsAccessor(EmailJobDetailsRepository emailJobDetailsRepository, EmailJobAdditionalEmailAddressRepository additionalEmailAddressRepository) {
        this.emailJobDetailsRepository = emailJobDetailsRepository;
        this.additionalEmailAddressRepository = additionalEmailAddressRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public EmailJobDetailsEntity saveEmailJobDetails(UUID jobId, EmailJobDetailsModel emailJobDetails) {
        EmailJobDetailsEntity jobDetailsToSave = new EmailJobDetailsEntity(
            jobId,
            emailJobDetails.getSubjectLine(),
            emailJobDetails.isProjectOwnerOnly(),
            emailJobDetails.isAdditionalEmailAddressesOnly(),
            emailJobDetails.getAttachmentFileType()
        );
        EmailJobDetailsEntity savedJobDetails = emailJobDetailsRepository.save(jobDetailsToSave);

        additionalEmailAddressRepository.deleteByJobId(jobId);
        List<EmailJobAdditionalEmailAddressEntity> additionalEmailAddressEntitiesToSave = emailJobDetails.getAdditionalEmailAddresses()
                                                                                              .stream()
                                                                                              .map(emailAddress -> new EmailJobAdditionalEmailAddressEntity(jobId, emailAddress))
                                                                                              .collect(Collectors.toList());
        List<EmailJobAdditionalEmailAddressEntity> savedAdditionalEmailAddressEntities = additionalEmailAddressRepository.saveAll(additionalEmailAddressEntitiesToSave);
        savedJobDetails.setEmailJobAdditionalEmailAddresses(savedAdditionalEmailAddressEntities);

        return savedJobDetails;
    }

    public List<String> retrieveAdditionalEmailAddressesForJob(UUID jobId) {
        return additionalEmailAddressRepository.findByJobId(jobId)
                   .stream()
                   .map(EmailJobAdditionalEmailAddressEntity::getEmailAddress)
                   .collect(Collectors.toList());
    }

}
