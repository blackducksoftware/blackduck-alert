/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.database.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.email.database.job.EmailJobDetailsEntity;
import com.blackduck.integration.alert.channel.email.database.job.EmailJobDetailsRepository;
import com.blackduck.integration.alert.channel.email.database.job.additional.EmailJobAdditionalEmailAddressEntity;
import com.blackduck.integration.alert.channel.email.database.job.additional.EmailJobAdditionalEmailAddressRepository;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

class EmailJobDetailsAccessorTest {
    private static final String ADDITIONAL_EMAIL_ADDRESS = "fake@blackduck.com";

    private final EmailChannelKey channelKey = ChannelKeys.EMAIL;
    private EmailJobDetailsRepository emailJobDetailsRepository;
    private EmailJobAdditionalEmailAddressRepository additionalEmailAddressRepository;

    private DefaultEmailJobDetailsAccessor emailJobDetailsAccessor;

    @BeforeEach
    public void init() {
        emailJobDetailsRepository = Mockito.mock(EmailJobDetailsRepository.class);
        additionalEmailAddressRepository = Mockito.mock(EmailJobAdditionalEmailAddressRepository.class);

        emailJobDetailsAccessor = new DefaultEmailJobDetailsAccessor(channelKey, emailJobDetailsRepository, additionalEmailAddressRepository);
    }

    @Test
    void getChannelKeyTest() {
        assertEquals(channelKey, emailJobDetailsAccessor.getDescriptorKey());
    }

    @Test
    void saveEmailJobDetailsTest() {
        UUID jobId = UUID.randomUUID();

        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(null, null, false, false, null, List.of(ADDITIONAL_EMAIL_ADDRESS));
        EmailJobDetailsEntity emailJobDetailsEntity = new EmailJobDetailsEntity(jobId, null, false, false, null);
        EmailJobAdditionalEmailAddressEntity emailJobAdditionalEmailAddressEntity = new EmailJobAdditionalEmailAddressEntity(jobId, ADDITIONAL_EMAIL_ADDRESS);

        Mockito.when(emailJobDetailsRepository.save(Mockito.any())).thenReturn(emailJobDetailsEntity);
        Mockito.when(additionalEmailAddressRepository.saveAll(Mockito.any())).thenReturn(List.of(emailJobAdditionalEmailAddressEntity));
        Mockito.when(additionalEmailAddressRepository.findByJobId(Mockito.any())).thenReturn(List.of(emailJobAdditionalEmailAddressEntity));

        EmailJobDetailsModel newEmailJobDetails = emailJobDetailsAccessor.saveJobDetails(jobId, emailJobDetailsModel);

        Mockito.verify(additionalEmailAddressRepository).bulkDeleteByJobId(Mockito.any());
        assertEquals(jobId, newEmailJobDetails.getJobId());
        assertEquals(1, newEmailJobDetails.getAdditionalEmailAddresses().size());
        assertEquals(ADDITIONAL_EMAIL_ADDRESS, newEmailJobDetails.getAdditionalEmailAddresses().get(0));
    }

    @Test
    void retrieveDetailsTest() {
        UUID jobId = UUID.randomUUID();

        EmailJobDetailsEntity emailJobDetailsEntity = new EmailJobDetailsEntity(jobId, null, false, false, null);
        EmailJobAdditionalEmailAddressEntity emailJobAdditionalEmailAddressEntity = new EmailJobAdditionalEmailAddressEntity(jobId, ADDITIONAL_EMAIL_ADDRESS);

        Mockito.when(emailJobDetailsRepository.findById(Mockito.any())).thenReturn(Optional.of(emailJobDetailsEntity));
        Mockito.when(additionalEmailAddressRepository.findByJobId(Mockito.any())).thenReturn(List.of(emailJobAdditionalEmailAddressEntity));
        EmailJobDetailsModel foundJobDetailsModel = emailJobDetailsAccessor.retrieveDetails(jobId).orElse(null);
        assertNotNull(foundJobDetailsModel);
        assertEquals(jobId, foundJobDetailsModel.getJobId());
        assertEquals(1, foundJobDetailsModel.getAdditionalEmailAddresses().size());
        assertEquals(ADDITIONAL_EMAIL_ADDRESS, foundJobDetailsModel.getAdditionalEmailAddresses().get(0));
    }

    @Test
    void retrieveDetailsUnknownIdTest() {
        UUID jobId = UUID.randomUUID();

        EmailJobAdditionalEmailAddressEntity emailJobAdditionalEmailAddressEntity = new EmailJobAdditionalEmailAddressEntity(jobId, ADDITIONAL_EMAIL_ADDRESS);

        Mockito.when(emailJobDetailsRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(additionalEmailAddressRepository.findByJobId(Mockito.any())).thenReturn(List.of(emailJobAdditionalEmailAddressEntity));
        Optional<EmailJobDetailsModel> foundJobDetailsModel = emailJobDetailsAccessor.retrieveDetails(jobId);
        assertTrue(foundJobDetailsModel.isEmpty());
    }

    @Test
    void retrieveAdditionalEmailAddressesForJobTest() {
        UUID jobId = UUID.randomUUID();

        EmailJobAdditionalEmailAddressEntity emailJobAdditionalEmailAddressEntity = new EmailJobAdditionalEmailAddressEntity(jobId, ADDITIONAL_EMAIL_ADDRESS);

        Mockito.when(additionalEmailAddressRepository.findByJobId(jobId)).thenReturn(List.of(emailJobAdditionalEmailAddressEntity));

        List<String> newEmailAddresses = emailJobDetailsAccessor.retrieveAdditionalEmailAddressesForJob(jobId);

        assertEquals(1, newEmailAddresses.size());
        String newEmailAddress = newEmailAddresses.get(0);
        assertEquals(ADDITIONAL_EMAIL_ADDRESS, newEmailAddress);
    }

}
