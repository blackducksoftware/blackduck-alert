package com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;

public abstract class GlobalRepositoryWrapperTest<E extends DatabaseEntity> {

    public abstract MockGlobalEntityUtil<E> getMockUtil();

    public abstract SimpleKeyRepositoryWrapper<E, ?> getRepositoryWrapper();

    @Test
    public void testEmptyEncryption() throws EncryptionException {
        final E entity = getMockUtil().createEmptyGlobalEntity();
        final E encryptedEntity = getRepositoryWrapper().encryptSensitiveData(entity);

        assertEquals(entity, encryptedEntity);
    }

    @Test
    public void testEncryptSensitiveData() throws EncryptionException {
        final E entity = getMockUtil().createGlobalEntity();
        final E encryptedEntity = getRepositoryWrapper().encryptSensitiveData(entity);

        assertEquals(encryptedEntity(), encryptedEntity);
    }

    public abstract E encryptedEntity() throws EncryptionException;

    @Test
    public void testEmptyDecryption() throws EncryptionException {
        final E entity = getMockUtil().createEmptyGlobalEntity();
        final E decryptedEntity = getRepositoryWrapper().decryptSensitiveData(entity);

        assertEquals(entity, decryptedEntity);
    }

    @Test
    public void testDecryptSensitiveData() throws EncryptionException {
        final E entityToDecrypt = getEntityToDecrypt();
        final E entity = getMockUtil().createGlobalEntity();
        final E decryptedEntity = getRepositoryWrapper().decryptSensitiveData(entityToDecrypt);

        assertEquals(entity, decryptedEntity);
    }

    public abstract E getEntityToDecrypt() throws EncryptionException;
}
