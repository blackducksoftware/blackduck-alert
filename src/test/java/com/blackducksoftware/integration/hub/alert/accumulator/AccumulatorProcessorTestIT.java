package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
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
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.alert.processor.policy.PolicyNotificationTypeProcessor;
import com.blackducksoftware.integration.hub.alert.processor.vulnerability.VulnerabilityNotificationTypeProcessor;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.service.CodeLocationService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.test.annotation.HubConnectionTest;

@Category(HubConnectionTest.class)
public class AccumulatorProcessorTestIT {
    private TestGlobalProperties globalProperties;
    private ProjectService projectService;
    private NotificationService notificationDataService;
    private HubService hubService;
    private CodeLocationService codeLocationService;

    private ProjectView project;

    @Before
    public void init() throws Exception {
        globalProperties = new TestGlobalProperties();
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryWithCredential(new PrintStreamIntLogger(System.out, LogLevel.TRACE));
        projectService = hubServicesFactory.createProjectService();
        notificationDataService = hubServicesFactory.createNotificationService();
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
        final int attempt = 1;
        DBStoreEvent storeEvent = null;
        NotificationDetailResults notificationData = null;
        while (attempt < 10) {
            // try for at most 120 seconds (2 minutes)
            TimeUnit.SECONDS.sleep(12);
            final HubBucket hubBucket = new HubBucket();
            notificationData = notificationDataService.getAllNotificationDetailResultsPopulated(hubBucket, new Date(System.currentTimeMillis() - 100000), new Date());
            final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
            final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
            final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
            final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties, processorList);

            storeEvent = accumulatorProcessor.process(notificationData);

            if (storeEvent != null) {
                // found notification data exit the loop
                break;
            }
        }
        assertNotNull(storeEvent);

        final List<NotificationModel> notifications = storeEvent.getNotificationList();

        assertFalse(notifications.isEmpty());
        assertEquals(storeEvent.getEventId().length(), 36);

        NotificationModel apacheModel = null;

        for (final NotificationModel model : notifications) {
            System.out.println(model);
            if ("Apache Commons FileUpload".equals(model.getComponentName())) {
                apacheModel = model;
            }
        }

        assertNotNull(apacheModel);

        final AccumulatorProcessor accumulatorProcessorNull = new AccumulatorProcessor(globalProperties, null);

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
