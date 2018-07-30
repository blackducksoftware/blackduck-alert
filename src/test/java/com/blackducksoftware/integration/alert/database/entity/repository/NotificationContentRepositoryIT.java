package com.blackducksoftware.integration.alert.database.entity.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.Instant;
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

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
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
        final Date createdAt = RestConnection.parseDateString(dateString);
        final String provider = "provider_1";
        final String notificationType = "type_1";
        final String content = "NOTIFICATION CONTENT HERE";
        final NotificationContent entity = new NotificationContent(createdAt, provider, notificationType, content);
        final NotificationContent savedEntity = repository.save(entity);
        return savedEntity;
    }

    @Test
    public void testSaveEntity() throws Exception {
        final Date createdAt = Date.from(Instant.now());
        final NotificationContent entity = createEntity(RestConnection.formatDate(new Date()));
        final NotificationContent savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final Optional<NotificationContent> foundEntityOptional = repository.findById(savedEntity.getId());
        final NotificationContent foundEntity = foundEntityOptional.get();
        assertEquals(entity.getCreatedAt(), foundEntity.getCreatedAt());
        assertEquals(entity.getNotificationType(), foundEntity.getNotificationType());
        assertEquals(entity.getProvider(), foundEntity.getProvider());
        assertEquals(entity.getContent(), foundEntity.getContent());
    }

    @Test
    public void testFindByDate() throws Exception {
        final Set<String> validResultDates = new HashSet<>();
        NotificationContent savedEntity = createEntity("2017-10-15T1:00:00.000Z");
        validResultDates.add(RestConnection.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-21T14:00:00.000Z");
        validResultDates.add(RestConnection.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-22T14:00:00.000Z");
        validResultDates.add(RestConnection.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-23T14:00:00.000Z");
        validResultDates.add(RestConnection.formatDate(savedEntity.getCreatedAt()));
        savedEntity = createEntity("2017-10-30T14:00:00.000Z");
        validResultDates.add(RestConnection.formatDate(savedEntity.getCreatedAt()));

        createEntity("2017-10-10T16:00:00.000Z");
        createEntity("2017-10-31T15:00:00.000Z");
        createEntity("2017-10-31T16:00:00.000Z");
        createEntity("2017-10-31T17:00:00.000Z");
        createEntity("2017-10-31T18:00:00.000Z");
        final long count = repository.count();
        assertEquals(10, count);
        final Date startDate = RestConnection.parseDateString("2017-10-12T01:30:59.000Z");
        final Date endDate = RestConnection.parseDateString("2017-10-30T16:59:59.000Z");
        final List<NotificationContent> foundEntityList = repository.findByCreatedAtBetween(startDate, endDate);
        assertEquals(5, foundEntityList.size());

        foundEntityList.forEach(entity -> {
            final String createdAtString = RestConnection.formatDate(entity.getCreatedAt());
            assertTrue(validResultDates.contains(createdAtString));
        });
    }
}
