package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalHubEntity;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class AccumulatorProcessorTestIT {

    @Test
    public void testProcess() throws Exception {
        final TestProperties testProperties = new TestProperties();
        final MockGlobalHubEntity mockEntity = new MockGlobalHubEntity();
        mockEntity.setHubApiKey(testProperties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
        final GlobalHubConfigEntity entity = mockEntity.createGlobalEntity();
        final GlobalHubRepositoryWrapper globalHubRepositoryWrapper = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final GlobalSchedulingRepositoryWrapper globalSchedulingRepositoryWrapper = Mockito.mock(GlobalSchedulingRepositoryWrapper.class);
        Mockito.when(globalHubRepositoryWrapper.findAll()).thenReturn(Arrays.asList(entity));
        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalHubRepositoryWrapper, globalSchedulingRepositoryWrapper);
        globalProperties.setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
        globalProperties.setHubApiKey(testProperties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
        globalProperties.setHubTrustCertificate(true);
        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties);

        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        projectVersionModel.setProjectLink("New project link");
        final String componentName = "notification test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentVersionUrl = "sss";
        final String componentIssueUrl = "ddd";

        final NotificationContentItem notificationContentItem = new NotificationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentVersionUrl, componentIssueUrl);
        final SortedSet<NotificationContentItem> notificationSet = new TreeSet<>();
        notificationSet.add(notificationContentItem);

        final NotificationResults notificationData = Mockito.mock(NotificationResults.class);
        Mockito.when(notificationData.getNotificationContentItems()).thenReturn(notificationSet);

        final DBStoreEvent storeEvent = accumulatorProcessor.process(notificationData);

        assertNotNull(storeEvent);
    }
}
