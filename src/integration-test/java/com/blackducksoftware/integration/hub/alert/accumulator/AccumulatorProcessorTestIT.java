package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.dataservice.component.ComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

public class AccumulatorProcessorTestIT {
    private TestGlobalProperties globalProperties;
    private ProjectService projectRequestService;
    private ProjectVersionService projectVersionService;
    private ComponentDataService componentDataService;
    private NotificationDataService notificationDataService;

    private ProjectView project;
    private UserView user;

    @Before
    public void init() throws Exception {
        final TestProperties testProperties = new TestProperties();
        globalProperties = new TestGlobalProperties();
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryWithCredential(new PrintStreamIntLogger(System.out, LogLevel.TRACE));
        projectRequestService = hubServicesFactory.createProjectService();
        projectVersionService = hubServicesFactory.createProjectVersionService();
        componentDataService = hubServicesFactory.createComponentDataService();
        notificationDataService = hubServicesFactory.createNotificationDataService();
        user = hubServicesFactory.createUserService().getUserByUserName(testProperties.getProperty(TestPropertyKey.TEST_USERNAME));
    }

    @After
    public void cleanup() throws IntegrationException {
        if (project != null) {
            projectRequestService.deleteHubProject(project);
        }
    }

    @Test
    public void testProcess() throws Exception {
        final Long timestamp = (new Date()).getTime();
        final String testProjectName = "hub-Alert-NotificationAccumulatorTest-" + timestamp;
        final String testProjectVersionName = "Version_1";

        final String projectUrl = projectRequestService.createHubProject(new ProjectRequest(testProjectName));
        System.out.println("projectUrl: " + projectUrl);

        project = projectRequestService.getView(projectUrl, ProjectView.class);
        projectVersionService.createHubVersion(project, new ProjectVersionRequest(ProjectVersionDistributionEnum.INTERNAL, ProjectVersionPhaseEnum.DEVELOPMENT, testProjectVersionName));
        final ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        final ExternalId apacheExternalId = externalIdFactory.createMavenExternalId("commons-fileupload", "commons-fileupload", "1.2.1");
        componentDataService.addComponentToProjectVersion(apacheExternalId, testProjectName, testProjectVersionName);

        TimeUnit.SECONDS.sleep(50);

        final NotificationResults notificationData = notificationDataService.getUserNotifications(new Date(System.currentTimeMillis() - 100000), new Date(), user);

        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties);

        final DBStoreEvent storeEvent = accumulatorProcessor.process(notificationData);

        final List<NotificationEvent> notificationEvents = storeEvent.getNotificationList();

        assertNotNull(storeEvent);
        assertTrue(!notificationEvents.isEmpty());
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
    }
}
