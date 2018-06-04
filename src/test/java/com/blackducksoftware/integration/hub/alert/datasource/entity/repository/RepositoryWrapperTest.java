package com.blackducksoftware.integration.hub.alert.datasource.entity.repository;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;

public abstract class RepositoryWrapperTest<E extends DatabaseEntity, R extends JpaRepository<E, Long>, W extends SimpleKeyRepositoryWrapper<E, R>> {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    public void assertExceptionThrown(final E entity) throws IOException {
        assertNull(entity);
        outputLogger.isLineContainingText("Error finding common distribution config");
    }

    @SuppressWarnings("unchecked")
    public W getExceptionThrowingRepositoryWrapper() throws EncryptionException {
        final R repository = getMockedRepository();
        final W wrapper = getRepositoryObjectWrapper(repository);
        final W wrapperSpy = Mockito.spy(wrapper);

        Mockito.doThrow(new EncryptionException()).when(wrapperSpy).decryptSensitiveData((E) Mockito.any(DatabaseEntity.class));
        Mockito.doThrow(new EncryptionException()).when(wrapperSpy).encryptSensitiveData((E) Mockito.any(DatabaseEntity.class));

        return wrapperSpy;
    }

    public abstract W getRepositoryObjectWrapper(R repository);

    public abstract R getMockedRepository();

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllNull() throws EncryptionException {
        final W wrapper = getExceptionThrowingRepositoryWrapper();

        Mockito.when(wrapper.getRepository().findAll()).thenReturn(null);
        Mockito.when(wrapper.getRepository().findAllById(Mockito.anyList())).thenReturn(null);

        final List<E> allEntities = wrapper.findAll();
        final List<E> allIdEntities = wrapper.findAll(Arrays.asList());

        assertEquals(Collections.emptyList(), allEntities);
        assertEquals(Collections.emptyList(), allIdEntities);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindAllException() throws EncryptionException, IOException {
        final W wrapper = getExceptionThrowingRepositoryWrapper();
        final MockEntityUtil<E> mockEntityUtil = getMockEntityUtil();

        Mockito.when(wrapper.getRepository().findAllById(Mockito.anyList())).thenReturn(Arrays.asList(mockEntityUtil.createEntity()));

        final List<E> emptyList = wrapper.findAll(Arrays.asList());

        assertEquals(Collections.emptyList(), emptyList);
        assertTrue(outputLogger.isLineContainingText("Error finding all entities"));
    }

    @Test
    public void testSave() throws EncryptionException, IOException {
        final W wrapper = getExceptionThrowingRepositoryWrapper();

        final E entity = wrapper.save(getMockEntityUtil().createEntity());

        assertNull(entity);
        assertTrue(outputLogger.isLineContainingText("Error saving entity"));

        final List<E> nullList = null;
        final List<E> entityNull = wrapper.save(nullList);

        assertEquals(Collections.emptyList(), entityNull);
    }

    @Test
    public void testDecrypt() throws EncryptionException {
        final W wrapper = getExceptionThrowingRepositoryWrapper();

        final List<E> nullList = null;
        final List<E> entityNull = wrapper.decryptSensitiveData(nullList);

        assertEquals(Collections.emptyList(), entityNull);
    }

    public abstract MockEntityUtil<E> getMockEntityUtil();

}
