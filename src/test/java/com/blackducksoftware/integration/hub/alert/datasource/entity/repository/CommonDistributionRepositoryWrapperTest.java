package com.blackducksoftware.integration.hub.alert.datasource.entity.repository;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;

public class CommonDistributionRepositoryWrapperTest extends RepositoryWrapperTest<CommonDistributionConfigEntity, CommonDistributionRepository, CommonDistributionRepositoryWrapper> {

    @Override
    public CommonDistributionRepositoryWrapper getRepositoryObjectWrapper(final CommonDistributionRepository repository) {
        return new CommonDistributionRepositoryWrapper(repository);
    }

    @Override
    public CommonDistributionRepository getMockedRepository() {
        final MockCommonDistributionEntity mockCommonDistributionEntity = getMockEntityUtil();
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);

        Mockito.when(commonDistributionRepository.findByDistributionConfigIdAndDistributionType(Mockito.anyLong(), Mockito.anyString())).thenReturn(mockCommonDistributionEntity.createEntity());

        return commonDistributionRepository;
    }

    @Override
    public MockCommonDistributionEntity getMockEntityUtil() {
        return new MockCommonDistributionEntity();
    }

    @Test
    public void testFindByDistributionConfigIdAndDistributionType() throws EncryptionException, IOException {
        final CommonDistributionRepositoryWrapper wrapper = getExceptionThrowingRepositoryWrapper();

        final CommonDistributionConfigEntity entity = wrapper.findByDistributionConfigIdAndDistributionType(1L, "any");
        assertExceptionThrown(entity);
    }

}
