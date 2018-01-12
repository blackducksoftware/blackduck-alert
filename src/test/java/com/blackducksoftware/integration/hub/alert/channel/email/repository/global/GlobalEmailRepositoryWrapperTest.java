package com.blackducksoftware.integration.hub.alert.channel.email.repository.global;

import org.mockito.Mockito;

import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalRepositoryWrapperTest;

public class GlobalEmailRepositoryWrapperTest extends GlobalRepositoryWrapperTest<GlobalEmailConfigEntity> {

    @Override
    public MockEmailGlobalEntity getMockUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public GlobalEmailRepositoryWrapper getRepositoryWrapper() {
        final GlobalEmailRepository repository = Mockito.mock(GlobalEmailRepository.class);
        final GlobalEmailRepositoryWrapper globalEmailRepositoryWrapper = new GlobalEmailRepositoryWrapper(repository);
        return globalEmailRepositoryWrapper;
    }

    @Override
    public GlobalEmailConfigEntity encryptedEntity() throws EncryptionException {
        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
        mockEmailGlobalEntity.setMailSmtpPassword(PasswordEncrypter.encrypt(mockEmailGlobalEntity.getMailSmtpPassword()));
        return mockEmailGlobalEntity.createGlobalEntity();
    }

    @Override
    public GlobalEmailConfigEntity getEntityToDecrypt() throws EncryptionException {
        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
        final String encryptedPassword = PasswordEncrypter.encrypt(mockEmailGlobalEntity.getMailSmtpPassword());
        mockEmailGlobalEntity.setMailSmtpPassword(encryptedPassword);
        return mockEmailGlobalEntity.createGlobalEntity();
    }

}
