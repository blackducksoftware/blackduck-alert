package com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global;

import org.mockito.Mockito;

import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalRepositoryWrapperTest;

public class GlobalHipChatRepositoryWrapperTest extends GlobalRepositoryWrapperTest<GlobalHipChatConfigEntity> {

    @Override
    public MockHipChatGlobalEntity getMockUtil() {
        return new MockHipChatGlobalEntity();
    }

    @Override
    public GlobalHipChatRepositoryWrapper getRepositoryWrapper() {
        final GlobalHipChatRepository repository = Mockito.mock(GlobalHipChatRepository.class);
        final GlobalHipChatRepositoryWrapper globalHipChatRepositoryWrapper = new GlobalHipChatRepositoryWrapper(repository);
        return globalHipChatRepositoryWrapper;
    }

    @Override
    public GlobalHipChatConfigEntity encryptedEntity() throws EncryptionException {
        final MockHipChatGlobalEntity mockHipChatGlobalEntity = new MockHipChatGlobalEntity();
        mockHipChatGlobalEntity.setApiKey(PasswordEncrypter.encrypt(mockHipChatGlobalEntity.getApiKey()));
        return mockHipChatGlobalEntity.createGlobalEntity();
    }

    @Override
    public GlobalHipChatConfigEntity getEntityToDecrypt() throws EncryptionException {
        final MockHipChatGlobalEntity mockHipChatGlobalEntity = new MockHipChatGlobalEntity();
        final String encryptedPassword = PasswordEncrypter.encrypt(mockHipChatGlobalEntity.getApiKey());
        mockHipChatGlobalEntity.setApiKey(encryptedPassword);
        return mockHipChatGlobalEntity.createGlobalEntity();
    }

}
