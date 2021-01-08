package com.synopsys.integration.alert.database.job.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressEntity;
import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressRepository;

public class EmailJobDetailsAccessorTest {
    private EmailJobDetailsRepository emailJobDetailsRepository;
    private EmailJobAdditionalEmailAddressRepository additionalEmailAddressRepository;

    private EmailJobDetailsAccessor emailJobDetailsAccessor;

    @BeforeEach
    public void init() {
        emailJobDetailsRepository = Mockito.mock(EmailJobDetailsRepository.class);
        additionalEmailAddressRepository = Mockito.mock(EmailJobAdditionalEmailAddressRepository.class);

        emailJobDetailsAccessor = new EmailJobDetailsAccessor(emailJobDetailsRepository, additionalEmailAddressRepository);
    }

    @Test
    public void saveEmailJobDetailsTest() {
        UUID jobId = UUID.randomUUID();
        String additionalEmailAddress = "fake@synopsys.com";

        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(null, false, false, null, List.of(additionalEmailAddress));
        EmailJobDetailsEntity emailJobDetailsEntity = new EmailJobDetailsEntity(jobId, null, false, false, null);
        EmailJobAdditionalEmailAddressEntity emailJobAdditionalEmailAddressEntity = new EmailJobAdditionalEmailAddressEntity(jobId, additionalEmailAddress);

        Mockito.when(emailJobDetailsRepository.save(Mockito.any())).thenReturn(emailJobDetailsEntity);
        Mockito.when(additionalEmailAddressRepository.saveAll(Mockito.any())).thenReturn(List.of(emailJobAdditionalEmailAddressEntity));

        EmailJobDetailsEntity newEmailJobDetailsEntity = emailJobDetailsAccessor.saveEmailJobDetails(jobId, emailJobDetailsModel);

        Mockito.verify(additionalEmailAddressRepository).deleteByJobId(Mockito.any());
        assertEquals(jobId, newEmailJobDetailsEntity.getJobId());
        assertEquals(1, newEmailJobDetailsEntity.getEmailJobAdditionalEmailAddresses().size());
        assertEquals(additionalEmailAddress, newEmailJobDetailsEntity.getEmailJobAdditionalEmailAddresses().get(0).getEmailAddress());
    }

    @Test
    public void retrieveAdditionalEmailAddressesForJobTest() {
        UUID jobId = UUID.randomUUID();
        String additionalEmailAddress = "fake@synopsys.com";

        EmailJobAdditionalEmailAddressEntity emailJobAdditionalEmailAddressEntity = new EmailJobAdditionalEmailAddressEntity(jobId, additionalEmailAddress);

        Mockito.when(additionalEmailAddressRepository.findByJobId(Mockito.eq(jobId))).thenReturn(List.of(emailJobAdditionalEmailAddressEntity));

        List<String> newEmailAddresses = emailJobDetailsAccessor.retrieveAdditionalEmailAddressesForJob(jobId);

        assertEquals(1, newEmailAddresses.size());
        String newEmailAddress = newEmailAddresses.get(0);
        assertEquals(additionalEmailAddress, newEmailAddress);
    }
}
