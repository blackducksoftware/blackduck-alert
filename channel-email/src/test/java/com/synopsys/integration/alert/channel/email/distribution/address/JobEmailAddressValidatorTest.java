package com.synopsys.integration.alert.channel.email.distribution.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

public class JobEmailAddressValidatorTest {
    @Test
    public void validateTest() {
        UUID testJobId = UUID.randomUUID();
        Long testBlackDuckGlobalConfigId = 33L;

        String testValidEmailAddress1 = "valid1";
        String testValidEmailAddress2 = "valid2";
        String testValidEmailAddress3 = "valid3";
        String testInvalidEmailAddress1 = "invalid1";
        String testInvalidEmailAddress2 = "invalid2";
        Set<String> testEmailAddresses = Set.of(testValidEmailAddress1, testInvalidEmailAddress1, testValidEmailAddress2, testInvalidEmailAddress2, testValidEmailAddress3);

        DistributionJobModel distributionJobModel = Mockito.mock(DistributionJobModel.class);
        Mockito.when(distributionJobModel.getBlackDuckGlobalConfigId()).thenReturn(testBlackDuckGlobalConfigId);

        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(testJobId)).thenReturn(Optional.of(distributionJobModel));

        ProviderUserModel providerUserModel = Mockito.mock(ProviderUserModel.class);
        Optional<ProviderUserModel> optionalProviderUser = Optional.of(providerUserModel);

        ProviderDataAccessor providerDataAccessor = Mockito.mock(ProviderDataAccessor.class);
        Mockito.when(providerDataAccessor.findFirstUserByEmailAddress(testBlackDuckGlobalConfigId, testValidEmailAddress1)).thenReturn(optionalProviderUser);
        Mockito.when(providerDataAccessor.findFirstUserByEmailAddress(testBlackDuckGlobalConfigId, testValidEmailAddress2)).thenReturn(optionalProviderUser);
        Mockito.when(providerDataAccessor.findFirstUserByEmailAddress(testBlackDuckGlobalConfigId, testValidEmailAddress3)).thenReturn(optionalProviderUser);
        Mockito.when(providerDataAccessor.findFirstUserByEmailAddress(testBlackDuckGlobalConfigId, testInvalidEmailAddress1)).thenReturn(Optional.empty());
        Mockito.when(providerDataAccessor.findFirstUserByEmailAddress(testBlackDuckGlobalConfigId, testInvalidEmailAddress2)).thenReturn(Optional.empty());

        JobEmailAddressValidator validator = new JobEmailAddressValidator(jobAccessor, providerDataAccessor);
        ValidatedEmailAddresses validatedEmailAddresses = validator.validate(testJobId, testEmailAddresses);

        Set<String> validEmailAddresses = validatedEmailAddresses.getValidEmailAddresses();
        assertEquals(3, validEmailAddresses.size());
        assertValid(validEmailAddresses, testValidEmailAddress1);
        assertValid(validEmailAddresses, testValidEmailAddress2);
        assertValid(validEmailAddresses, testValidEmailAddress3);

        Set<String> invalidEmailAddresses = validatedEmailAddresses.getInvalidEmailAddresses();
        assertEquals(2, invalidEmailAddresses.size());
        assertInvalid(invalidEmailAddresses, testInvalidEmailAddress1);
        assertInvalid(invalidEmailAddresses, testInvalidEmailAddress2);
    }

    @Test
    public void validateMissingJobTest() {
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.empty());

        JobEmailAddressValidator validator = new JobEmailAddressValidator(jobAccessor, null);

        Set<String> emailsToValidate = Set.of("test_email");
        ValidatedEmailAddresses validatedEmails = validator.validate(UUID.randomUUID(), emailsToValidate);
        assertTrue(validatedEmails.getValidEmailAddresses().isEmpty(), "Expected no valid email addresses");
        assertEquals(emailsToValidate, validatedEmails.getInvalidEmailAddresses());
    }

    private void assertValid(Set<String> validEmailAddresses, String emailAddressToTest) {
        assertContains(validEmailAddresses, emailAddressToTest, String.format("Expected '%s' to be a valid email address", emailAddressToTest));
    }

    private void assertInvalid(Set<String> invalidEmailAddresses, String emailAddressToTest) {
        assertContains(invalidEmailAddresses, emailAddressToTest, String.format("Expected '%s' to be an invalid email address", emailAddressToTest));
    }

    private void assertContains(Set<String> emailAddresses, String emailAddressToTest, String failureMessage) {
        assertTrue(emailAddresses.contains(emailAddressToTest), failureMessage);
    }

}
