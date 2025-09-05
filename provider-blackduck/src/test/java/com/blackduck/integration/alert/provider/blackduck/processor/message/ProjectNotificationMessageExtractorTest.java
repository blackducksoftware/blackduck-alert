/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.project.MessageReason;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.blackduck.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.OperationType;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;

public class ProjectNotificationMessageExtractorTest {
    private static final String PROJECT_URL = "https://projectUrl";
    private static final String PROJECT = "ProjectName";

    private final BlackDuckProviderKey providerKey = new BlackDuckProviderKey();

    @Test
    public void extractTest() throws IntegrationException {
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(blackDuckHttpClient.getBlackDuckUrl()).thenReturn(new HttpUrl("https://a.blackduck.server.example.com"));
        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(blackDuckHttpClient);
        Mockito.when(servicesFactoryCache.retrieveBlackDuckServicesFactory(Mockito.anyLong())).thenReturn(blackDuckServicesFactory);

        ProjectNotificationContent projectNotificationContent = createProjectNotificationContent();
        NotificationContentWrapper notificationContentWrapper = createNotificationContentWrapper(projectNotificationContent);

        ProjectNotificationMessageExtractor extractor = new ProjectNotificationMessageExtractor(providerKey, servicesFactoryCache);
        ProviderMessageHolder providerMessageHolder = extractor.extract(notificationContentWrapper, projectNotificationContent);

        assertEquals(1, providerMessageHolder.getProjectMessages().size());
        assertEquals(0, providerMessageHolder.getSimpleMessages().size());
        ProjectMessage projectMessage = providerMessageHolder.getProjectMessages().get(0);

        assertEquals(MessageReason.PROJECT_STATUS, projectMessage.getMessageReason());
        assertTrue(projectMessage.getOperation().isPresent());
        assertEquals(ProjectOperation.CREATE, projectMessage.getOperation().get());
        assertTrue(projectMessage.getBomComponents().isEmpty());
        assertEquals(PROJECT, projectMessage.getProject().getValue());
        assertTrue(projectMessage.getProject().getUrl().isPresent());
        assertEquals(PROJECT_URL, projectMessage.getProject().getUrl().get());
        assertFalse(projectMessage.getProjectVersion().isPresent());
    }

    private NotificationContentWrapper createNotificationContentWrapper(ProjectNotificationContent notificationContentComponent) {
        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(1L,
            1L,
            "provider-test",
            "providerConfigName-test",
            "notificationType-test",
            "{content: \"content is here...\"}",
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
        return new NotificationContentWrapper(alertNotificationModel, notificationContentComponent, LicenseLimitNotificationContent.class);
    }

    private ProjectNotificationContent createProjectNotificationContent() {
        ProjectNotificationContent projectNotificationContent = new ProjectNotificationContent();
        projectNotificationContent.setProject(PROJECT_URL);
        projectNotificationContent.setProjectName(PROJECT);
        projectNotificationContent.setOperationType(OperationType.CREATE);

        return projectNotificationContent;
    }
}
