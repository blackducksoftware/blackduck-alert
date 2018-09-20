package com.synopsys.integration.alert.database.entity.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class NotificationContentRepositoryIT {

    @Autowired
    private NotificationContentRepository repository;

    @Before
    public void cleanup() {
        repository.deleteAll();
    }

    private NotificationContent createEntity(final String dateString) throws ParseException {
        final Date createdAt = RestConstants.parseDateString(dateString);
        final Date providerCreationTime = createdAt;
        final String provider = "provider_1";
        final String notificationType = "type_1";
        final String content = "NOTIFICATION CONTENT HERE";
        final NotificationContent entity = new MockNotificationContent(createdAt, provider, providerCreationTime, notificationType, content, null).createEntity();
        final NotificationContent savedEntity = repository.save(entity);
        return savedEntity;
    }

    @Test
    public void testSaveEntity() throws Exception {
        final NotificationContent entity = createEntity(RestConstants.formatDate(new Date()));
        final NotificationContent savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final Optional<NotificationContent> foundEntityOptional = repository.findById(savedEntity.getId());
        final NotificationContent foundEntity = foundEntityOptional.get();
        assertEquals(entity.getCreatedAt(), foundEntity.getCreatedAt());
        assertEquals(entity.getNotificationType(), foundEntity.getNotificationType());
        assertEquals(entity.getProvider(), foundEntity.getProvider());
        assertEquals(entity.getProviderCreationTime(), foundEntity.getProviderCreationTime());
        assertEquals(entity.getContent(), foundEntity.getContent());
    }

    @Test
    public void testFindByDate() throws Exception {
        final Set<String> validResultDates = new HashSet<>();
        NotificationContent savedEntity = createEntity("2017-10-15T1:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-21T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-22T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-23T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-30T14:00:00.000Z");
        validResultDates.add(RestConstants.formatDate(savedEntity.getCreatedAt()));

        createEntity("2017-10-10T16:00:00.000Z");
        createEntity("2017-10-31T15:00:00.000Z");
        createEntity("2017-10-31T16:00:00.000Z");
        createEntity("2017-10-31T17:00:00.000Z");
        createEntity("2017-10-31T18:00:00.000Z");
        final long count = repository.count();
        assertEquals(10, count);
        final Date startDate = RestConstants.parseDateString("2017-10-12T01:30:59.000Z");
        final Date endDate = RestConstants.parseDateString("2017-10-30T16:59:59.000Z");
        final List<NotificationContent> foundEntityList = repository.findByCreatedAtBetween(startDate, endDate);
        assertEquals(5, foundEntityList.size());

        foundEntityList.forEach(entity -> {
            final String createdAtString = RestConstants.formatDate(entity.getCreatedAt());
            assertTrue(validResultDates.contains(createdAtString));
        });
    }
}
