package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.ResourceLoader;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.service.CodeLocationService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.OldNotificationResults;
import com.blackducksoftware.integration.hub.throwaway.OldNotificationService;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.test.annotation.HubConnectionTest;

@Category(HubConnectionTest.class)
public class AccumulatorProcessorTestIT {
    private TestGlobalProperties globalProperties;
    private ProjectService projectService;
    private OldNotificationService notificationDataService;
    private HubService hubService;
    private CodeLocationService codeLocationService;

    private ProjectView project;

    @Before
    public void init() throws Exception {
        globalProperties = new TestGlobalProperties();
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryWithCredential(new PrintStreamIntLogger(System.out, LogLevel.TRACE));
        projectService = hubServicesFactory.createProjectService();
        notificationDataService = new OldNotificationService(hubServicesFactory.createHubService(), null);
        codeLocationService = hubServicesFactory.createCodeLocationService();
        hubService = hubServicesFactory.createHubService();
    }

    @After
    public void cleanup() throws IntegrationException {
        if (project != null) {
            projectService.deleteHubProject(project);
        }
    }

    @Test
    public void testProcess() throws Exception {
        final Long timestamp = (new Date()).getTime();
        final String testProjectName = "hub-Alert-NotificationAccumulatorTest-" + timestamp;
        final String testProjectVersionName = "1.0.0";

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.name = testProjectName;
        final String projectUrl = projectService.createHubProject(projectRequest);
        final ProjectView projectItem = hubService.getResponse(projectUrl, ProjectView.class);

        System.out.println("projectUrl: " + projectUrl);

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.distribution = ProjectVersionDistributionType.INTERNAL;
        projectVersionRequest.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest.versionName = testProjectVersionName;
        projectService.createHubVersion(projectItem, projectVersionRequest);

        uploadBdio("bdio/component-bdio.jsonld");

        TimeUnit.SECONDS.sleep(60);

        final OldNotificationResults notificationData = notificationDataService.getAllNotificationResults(new Date(System.currentTimeMillis() - 100000), new Date());

        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties);

        final DBStoreEvent storeEvent = accumulatorProcessor.process(notificationData);

        assertNotNull(storeEvent);

        final List<NotificationEvent> notificationEvents = storeEvent.getNotificationList();

        assertFalse(notificationEvents.isEmpty());
        assertEquals(storeEvent.getEventId().length(), 36);

        NotificationEvent apacheEvent = null;

        for (final NotificationEvent event : notificationEvents) {
            System.out.println(event);
            if ("Apache Commons FileUpload".equals(event.getDataSet().get("COMPONENT"))) {
                apacheEvent = event;
            }
        }

        assertNotNull(apacheEvent);

        final AccumulatorProcessor accumulatorProcessorNull = new AccumulatorProcessor(null);

        final DBStoreEvent storeEventNull = accumulatorProcessorNull.process(notificationData);
        assertNull(storeEventNull);
        final ProjectVersionWrapper projectWrapper = projectService.getProjectVersion(testProjectName, testProjectVersionName);
        projectService.deleteHubProject(projectWrapper.getProjectView());

    }

    private void uploadBdio(final String bdioFile) throws IntegrationException, URISyntaxException, IOException {
        final ResourceLoader resourceLoader = new ResourceLoader();
        final String bdioContent = resourceLoader.loadJsonResource(bdioFile);
        final File tempFile = File.createTempFile("tempBdio", ".jsonld");
        FileUtils.write(tempFile, bdioContent, "UTF-8");

        codeLocationService.importBomFile(tempFile);
        tempFile.delete();
    }
}
