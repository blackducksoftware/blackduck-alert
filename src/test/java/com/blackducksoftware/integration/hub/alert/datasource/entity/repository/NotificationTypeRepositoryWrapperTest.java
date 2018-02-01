package com.blackducksoftware.integration.hub.alert.datasource.entity.repository;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.mock.NotificationTypeMockUtils;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class NotificationTypeRepositoryWrapperTest extends RepositoryWrapperTest<NotificationTypeEntity, NotificationTypeRepository, NotificationTypeRepositoryWrapper> {

    @Override
    public NotificationTypeRepositoryWrapper getRepositoryObjectWrapper(final NotificationTypeRepository repository) {
        return new NotificationTypeRepositoryWrapper(repository);
    }

    @Override
    public NotificationTypeRepository getMockedRepository() {
        final NotificationTypeMockUtils mockNotificationEntity = getMockEntityUtil();
        final NotificationTypeRepository repository = Mockito.mock(NotificationTypeRepository.class);
        Mockito.when(repository.findByType(Mockito.any())).thenReturn(mockNotificationEntity.createEntity());
        return repository;
    }

    @Override
    public NotificationTypeMockUtils getMockEntityUtil() {
        return new NotificationTypeMockUtils();
    }

    @Test
    public void testFindByType() throws EncryptionException, IOException {
        final NotificationTypeRepositoryWrapper wrapper = getExceptionThrowingRepositoryWrapper();

        final NotificationTypeEntity entity = wrapper.findByType(NotificationCategoryEnum.HIGH_VULNERABILITY);

        assertExceptionThrown(entity);
    }

}
