package com.blackducksoftware.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckEntity;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckRestModel;
import com.blackducksoftware.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfig;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.google.gson.Gson;

// TODO Make these tests more useful once provider descriptors are fully implemented
public class BlackDuckDescriptorTest {

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

        final BlackDuckDescriptor hubDescriptor = new BlackDuckDescriptor(null, hubRepositoryAccessor, null, null);

        final List<? extends DatabaseEntity> entities = hubDescriptor.getGlobalRepositoryAccessor().readEntities();
        final Optional<? extends DatabaseEntity> foundEntity = hubDescriptor.getGlobalRepositoryAccessor().readEntity(1);
        final DatabaseEntity savedEntity = hubDescriptor.getGlobalRepositoryAccessor().saveEntity(entity);
        hubDescriptor.getGlobalRepositoryAccessor().deleteEntity(1);

        assertEquals(1, entities.size());
        assertEquals(entity, entities.get(0));

        assertTrue(foundEntity.isPresent());
        assertTrue(savedEntity != null);

        assertEquals(entity, foundEntity.get());
        assertEquals(entity, savedEntity);
    }

    @Test
    public void testTransformerCalls() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final BlackDuckContentConverter hubContentConverter = new BlackDuckContentConverter(contentConverter);

        final BlackDuckDescriptor hubDescriptor = new BlackDuckDescriptor(hubContentConverter, null, null, null);

        final GlobalBlackDuckConfigEntity hubEntity = mockBlackDuckEntity.createGlobalEntity();
        final GlobalBlackDuckConfig hubRestModel = mockBlackDuckRestModel.createGlobalRestModel();

        final Config restModel = hubDescriptor.getGlobalContentConverter().populateRestModelFromDatabaseEntity(hubEntity);
        final DatabaseEntity entity = hubDescriptor.getGlobalContentConverter().populateDatabaseEntityFromRestModel(hubRestModel);
        final Config jsonRestModel = hubDescriptor.getGlobalContentConverter().getRestModelFromJson(gson.toJson(hubEntity));

        assertEquals(String.valueOf(hubEntity.getId()), restModel.getId());
        assertEquals(hubRestModel.getId(), String.valueOf(entity.getId()));
        assertEquals(hubRestModel.getId(), jsonRestModel.getId());
    }

    @Test
    public void testGetProvider() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final BlackDuckContentConverter hubContentConverter = new BlackDuckContentConverter(contentConverter);
        final BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(hubContentConverter, null, null, provider);
        assertEquals(provider, descriptor.getProvider());
    }

    @Test
    public void testValidateGlobalConfig() {
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null);
        final BlackDuckDescriptor spiedDescriptor = Mockito.spy(descriptor);
        final Config model = Mockito.mock(Config.class);
        final Map<String, String> fieldErrors = Mockito.mock(Map.class);
        spiedDescriptor.validateGlobalConfig(model, fieldErrors);
        Mockito.verify(spiedDescriptor).validateGlobalConfig(Mockito.any(Config.class), Mockito.anyMap());
    }

    @Test
    public void testTestGlobalConfigMethod() {
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null);
        final BlackDuckDescriptor spiedDescriptor = Mockito.spy(descriptor);
        spiedDescriptor.testGlobalConfig(null);
        Mockito.verify(spiedDescriptor).testGlobalConfig(Mockito.any());
    }

    @Test
    public void testGetNotificationTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, provider);
        final Set<String> expectedNotificationTypes = Arrays.stream(NotificationType.values()).map(NotificationType::name).collect(Collectors.toSet());
        final Set<String> providerNotificationTypes = descriptor.getNotificationTypes();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }
}
