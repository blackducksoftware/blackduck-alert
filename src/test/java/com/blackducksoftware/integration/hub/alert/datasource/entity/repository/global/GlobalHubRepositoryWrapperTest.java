package com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global;

import org.mockito.Mockito;

import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.hub.mock.MockGlobalHubEntity;

public class GlobalHubRepositoryWrapperTest extends GlobalRepositoryWrapperTest<GlobalHubConfigEntity> {

    @Override
    public MockGlobalHubEntity getMockUtil() {
        return new MockGlobalHubEntity();
    }

    @Override
    public GlobalHubRepositoryWrapper getRepositoryWrapper() {
        final GlobalHubRepository globalHubRepository = Mockito.mock(GlobalHubRepository.class);
        final GlobalHubRepositoryWrapper globalHubRepositoryWrapper = new GlobalHubRepositoryWrapper(globalHubRepository);
        return globalHubRepositoryWrapper;
    }

    @Override
    public GlobalHubConfigEntity encryptedEntity() throws EncryptionException {
        final MockGlobalHubEntity mockGlobalHubEntity = new MockGlobalHubEntity();
        mockGlobalHubEntity.setHubApiKey(PasswordEncrypter.encrypt(mockGlobalHubEntity.getHubApiKey()));
        return mockGlobalHubEntity.createGlobalEntity();
    }

    @Override
    public GlobalHubConfigEntity getEntityToDecrypt() throws EncryptionException {
        final MockGlobalHubEntity mockGlobalHubEntity = new MockGlobalHubEntity();
        final String encryptedApiKey = PasswordEncrypter.encrypt(mockGlobalHubEntity.getHubApiKey());
        mockGlobalHubEntity.setHubApiKey(encryptedApiKey);
        return mockGlobalHubEntity.createGlobalEntity();
    }

}
