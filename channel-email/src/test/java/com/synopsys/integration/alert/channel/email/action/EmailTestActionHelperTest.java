package com.synopsys.integration.alert.channel.email.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.api.descriptor.EmailChannelKey;

@ExtendWith(SpringExtension.class)
public class EmailTestActionHelperTest {
    private final List<ProviderProject> providerProjects = createProviderProjects();
    private final String projectOwnerEmailAddress = "project-owner@synopsys.com";
    private final String additionalEmailAddress = "additional-email@synopsy.com";

    @Mock
    private ProviderDataAccessor providerDataAccessor;

    @Test
    void verifyEmailProjectOwnerOnly() throws AlertException {
        List<ProviderProject> singleProject = List.of(new ProviderProject("name", "description", "href", projectOwnerEmailAddress));
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.anyLong())).thenReturn(singleProject);

        DistributionJobModel distributionJobModel = createDistributionJobModelBuilder(createDefaultEmailJobDetails(
            true,
            false,
            List.of(additionalEmailAddress)
        ))
            .projectNamePattern("name")
            .filterByProject(true)
            .build();

        EmailTestActionHelper emailTestActionHelper = new EmailTestActionHelper(providerDataAccessor);
        Set<String> emailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(distributionJobModel);

        assertEquals(1, emailAddresses.size());
        assertTrue(emailAddresses.contains(projectOwnerEmailAddress));
    }

    @Test
    void verifyAdditionalEmailAddressesOnly() throws AlertException {
        List<ProviderProject> singleProject = List.of(new ProviderProject("name", "description", "href", projectOwnerEmailAddress));
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.anyLong())).thenReturn(singleProject);

        DistributionJobModel distributionJobModel = createDistributionJobModelBuilder(createDefaultEmailJobDetails(
            false,
            true,
            List.of(additionalEmailAddress, additionalEmailAddress)
        )).build();

        EmailTestActionHelper emailTestActionHelper = new EmailTestActionHelper(providerDataAccessor);
        Set<String> emailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(distributionJobModel);

        assertEquals(1, emailAddresses.size());
        assertTrue(emailAddresses.contains(additionalEmailAddress));
    }

    @Test
    void verifyEmailOwnerAndAdditional() throws AlertException {
        Long blackduckGlobalConfigId = new Random().nextLong();
        List<ProviderProject> singleProject = List.of(new ProviderProject("name", "description", "href", projectOwnerEmailAddress));
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.anyLong())).thenReturn(singleProject);
        Mockito.when(providerDataAccessor.getEmailAddressesForProjectHref(Mockito.eq(blackduckGlobalConfigId), Mockito.anyString()))
            .thenAnswer(i -> Set.of(projectOwnerEmailAddress));

        DistributionJobModel distributionJobModel = createDistributionJobModelBuilder(createDefaultEmailJobDetails(
            false,
            false,
            List.of(additionalEmailAddress, additionalEmailAddress)
        )).blackDuckGlobalConfigId(blackduckGlobalConfigId).build();

        EmailTestActionHelper emailTestActionHelper = new EmailTestActionHelper(providerDataAccessor);
        Set<String> emailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(distributionJobModel);

        assertEquals(2, emailAddresses.size());
        assertTrue(emailAddresses.contains(projectOwnerEmailAddress));
        assertTrue(emailAddresses.contains(additionalEmailAddress));
    }

    @Test
    void verifyEmptyProjectEmailThrowsException() {
        List<ProviderProject> singleProject = List.of(new ProviderProject("name", "description", "href", ""));
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.anyLong())).thenReturn(singleProject);

        DistributionJobModel distributionJobModel = createDistributionJobModelBuilder(createDefaultEmailJobDetails(true, false, List.of(additionalEmailAddress)))
            .projectNamePattern("na.*")
            .filterByProject(true)
            .build();

        EmailTestActionHelper emailTestActionHelper = new EmailTestActionHelper(providerDataAccessor);

        assertThrows(AlertException.class, () -> emailTestActionHelper.createUpdatedEmailAddresses(distributionJobModel));
    }

    @Test
    void verifyNoMatchingPattern() throws AlertException {
        List<ProviderProject> singleProject = List.of(new ProviderProject("name", "description", "href", projectOwnerEmailAddress));
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.anyLong())).thenReturn(singleProject);

        DistributionJobModel distributionJobModel = createDistributionJobModelBuilder(createDefaultEmailJobDetails(
            true,
            false,
            List.of(additionalEmailAddress)
        ))
            .projectNamePattern("pattern")
            .filterByProject(true)
            .build();

        EmailTestActionHelper emailTestActionHelper = new EmailTestActionHelper(providerDataAccessor);
        Set<String> emailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(distributionJobModel);

        assertTrue(emailAddresses.isEmpty());
    }

    @Test
    void verifyAllProjectsProperlyRetrieved() throws AlertException {
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.anyLong())).thenReturn(providerProjects);
        Mockito.when(providerDataAccessor.getEmailAddressesForProjectHref(Mockito.anyLong(), Mockito.anyString())).thenAnswer(i -> Set.of(UUID.randomUUID().toString()));

        EmailTestActionHelper emailTestActionHelper = new EmailTestActionHelper(providerDataAccessor);

        DistributionJobModel distributionJobModel = createDefaultDistributionJobModel();
        Set<String> emailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(distributionJobModel);

        assertEquals(providerProjects.size(), emailAddresses.size());
    }

    @Test
    void verifyProjectsRetrievedWithOnlyVersionPattern() throws AlertException {
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.anyLong())).thenReturn(providerProjects);
        Mockito.when(providerDataAccessor.getEmailAddressesForProjectHref(Mockito.anyLong(), Mockito.anyString())).thenAnswer(i -> Set.of(UUID.randomUUID().toString()));
        Mockito.when(providerDataAccessor.getProjectVersionNamesByHref(Mockito.anyLong(), Mockito.anyString(), Mockito.anyInt())).thenReturn(getProjectVersions());

        EmailTestActionHelper emailTestActionHelper = new EmailTestActionHelper(providerDataAccessor);

        DistributionJobModel distributionJobModel = createDistributionJobModel(
            createDefaultEmailJobDetails(false, false, List.of()),
            null,
            "1.0.*",
            List.of()
        );
        Set<String> emailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(distributionJobModel);

        assertEquals(providerProjects.size(), emailAddresses.size());
    }

    private List<ProviderProject> createProviderProjects() {
        return List.of(
            new ProviderProject("name", "description", "href", "ownerEmail"),
            new ProviderProject("name2", "description2", "href2", "ownerEmail2"),
            new ProviderProject("name3", "description3", "href3", "ownerEmail3"),
            new ProviderProject("name4", "description4", "href4", "ownerEmail4"),
            new ProviderProject("name5", "description5", "href5", "ownerEmail5"),
            new ProviderProject("name6", "description6", "href6", "ownerEmail6")
        );
    }

    private AlertPagedModel<String> getProjectVersions() {
        List<String> versions = List.of(
            "1.0.1",
            "6.9",
            UUID.randomUUID().toString()
        );
        return new AlertPagedModel<>(1, 1, 100, versions);
    }

    private DistributionJobModel createDefaultDistributionJobModel() {
        return createDistributionJobModelBuilder(createDefaultEmailJobDetails(false, false, List.of())).build();
    }

    private EmailJobDetailsModel createDefaultEmailJobDetails(boolean projectOwnerOnly, boolean additionalEmailAddressesOnly, List<String> additionalEmailAddresses) {
        return new EmailJobDetailsModel(
            UUID.randomUUID(),
            null,
            projectOwnerOnly,
            additionalEmailAddressesOnly,
            "none",
            additionalEmailAddresses
        );
    }

    private DistributionJobModelBuilder createDistributionJobModelBuilder(EmailJobDetailsModel emailJobDetailsModel) {
        return new DistributionJobModelBuilder()
            .name("name")
            .notificationTypes(List.of("LICENSE_LIMIT"))
            .blackDuckGlobalConfigId(1L)
            .createdAt(OffsetDateTime.now())
            .lastUpdated(OffsetDateTime.now())
            .distributionFrequency(FrequencyType.REAL_TIME)
            .channelDescriptorName(new EmailChannelKey().getUniversalKey())
            .distributionJobDetails(emailJobDetailsModel)
            .processingType(ProcessingType.DEFAULT);
    }

    private DistributionJobModel createDistributionJobModel(
        EmailJobDetailsModel emailJobDetailsModel,
        String projectNamePattern,
        String projectVersionNamePattern,
        List<BlackDuckProjectDetailsModel> projectDetailsModels
    ) {
        return createDistributionJobModelBuilder(emailJobDetailsModel)
            .filterByProject(true)
            .projectNamePattern(projectNamePattern)
            .projectVersionNamePattern(projectVersionNamePattern)
            .projectFilterDetails(projectDetailsModels)
            .build();
    }
}
