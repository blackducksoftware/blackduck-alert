package com.synopsys.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckProviderRestApi;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckEntity;
import com.synopsys.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckRestModel;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckConfig;

// TODO Make these tests more useful once provider descriptors are fully implemented
public class BlackDuckDescriptorConfigTest {

    private final MockGlobalBlackDuckEntity mockBlackDuckEntity = new MockGlobalBlackDuckEntity();
    private final MockGlobalBlackDuckRestModel mockBlackDuckRestModel = new MockGlobalBlackDuckRestModel();

    @Test
    public void testRepositoryCalls() {
        final GlobalBlackDuckConfigEntity entity = mockBlackDuckEntity.createGlobalEntity();
        final GlobalBlackDuckRepository globalBlackDuckRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        Mockito.when(globalBlackDuckRepository.findAll()).thenReturn(Arrays.asList(entity));
        Mockito.when(globalBlackDuckRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(globalBlackDuckRepository.save(Mockito.any())).thenReturn(entity);
        Mockito.doNothing().when(globalBlackDuckRepository).deleteById(Mockito.anyLong());
        final BlackDuckRepositoryAccessor hubRepositoryAccessor = new BlackDuckRepositoryAccessor(globalBlackDuckRepository);

        final BlackDuckProviderRestApi hubDescriptorConfig = new BlackDuckProviderRestApi(null, hubRepositoryAccessor, null, null);

        final List<? extends DatabaseEntity> entities = hubDescriptorConfig.getRepositoryAccessor().readEntities();
        final Optional<? extends DatabaseEntity> foundEntity = hubDescriptorConfig.getRepositoryAccessor().readEntity(1);
        final DatabaseEntity savedEntity = hubDescriptorConfig.getRepositoryAccessor().saveEntity(entity);
        hubDescriptorConfig.getRepositoryAccessor().deleteEntity(1);

        assertEquals(1, entities.size());
        assertEquals(entity, entities.get(0));

        assertTrue(foundEntity.isPresent());
        assertTrue(savedEntity != null);

        assertEquals(entity, foundEntity.get());
        assertEquals(entity, savedEntity);
    }

    // @Test
    // public void testTransformerCalls() {
    // final Gson gson = new Gson();
    // final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
    // final BlackDuckTypeConverter hubContentConverter = new BlackDuckTypeConverter(contentConverter, null, null);
    //
    // final BlackDuckProviderDescriptorConfig hubDescriptor = new BlackDuckProviderDescriptorConfig(hubContentConverter, null, null, null);
    //
    // final BlackDuckUserEntity hubEntity = mockBlackDuckEntity.createGlobalEntity();
    // final BlackDuckConfig hubRestModel = mockBlackDuckRestModel.createGlobalRestModel();
    //
    // final Config restModel = hubDescriptor.getTypeConverter().populateConfigFromEntity(hubEntity);
    // final DatabaseEntity entity = hubDescriptor.getTypeConverter().populateEntityFromConfig(hubRestModel);
    // final Config jsonRestModel = hubDescriptor.getTypeConverter().getConfigFromJson(gson.toJson(hubEntity));
    //
    // assertEquals(String.valueOf(hubEntity.getId()), restModel.getId());
    // assertEquals(hubRestModel.getId(), String.valueOf(entity.getId()));
    // assertEquals(hubRestModel.getId(), jsonRestModel.getId());
    // }

    @Test
    public void testValidateGlobalConfig() {
        final BlackDuckProviderRestApi hubDescriptor = new BlackDuckProviderRestApi(null, null, null, null);
        final BlackDuckProviderRestApi spiedDescriptor = Mockito.spy(hubDescriptor);
        final BlackDuckConfig model = Mockito.mock(BlackDuckConfig.class);
        final Map<String, String> fieldErrors = Mockito.mock(Map.class);
        spiedDescriptor.validateConfig(model, fieldErrors);
        Mockito.verify(spiedDescriptor).validateConfig(Mockito.any(Config.class), Mockito.anyMap());
    }

    // @Test
    // public void testTestGlobalConfigMethod() throws IntegrationException {
    // final BlackDuckProviderDescriptorConfig hubDescriptor = new BlackDuckProviderDescriptorConfig(null, null, null, null);
    // final BlackDuckProviderDescriptorConfig spiedDescriptor = Mockito.spy(hubDescriptor);
    // spiedDescriptor.testConfig(null);
    // Mockito.verify(spiedDescriptor).testConfig(Mockito.any());
    // }
}
