package com.blackducksoftware.integration.hub.alert.datasource.entity.repository;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockVulnerabilityEntity;

public class VulnerabiltyRespositoryWrapperTest extends RepositoryWrapperTest<VulnerabilityEntity, VulnerabilityRepository, VulnerabilityRepositoryWrapper> {

    @Override
    public VulnerabilityRepositoryWrapper getRepositoryObjectWrapper(final VulnerabilityRepository repository) {
        final VulnerabilityRepositoryWrapper vulnerabilityRepositoryWrapper = new VulnerabilityRepositoryWrapper(repository);
        return vulnerabilityRepositoryWrapper;
    }

    @Override
    public VulnerabilityRepository getMockedRepository() {
        final VulnerabilityRepository vulnerabilityRepository = Mockito.mock(VulnerabilityRepository.class);
        return vulnerabilityRepository;
    }

    @Override
    public MockVulnerabilityEntity getMockEntityUtil() {
        return new MockVulnerabilityEntity();
    }

    @Test
    public void testFindById() throws IOException, EncryptionException {
        final VulnerabilityRepositoryWrapper wrapper = getExceptionThrowingRepositoryWrapper();

        final VulnerabilityEntity entity = wrapper.findById(1L);

        assertExceptionThrown(entity);
    }

}
