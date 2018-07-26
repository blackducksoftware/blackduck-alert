package com.blackducksoftware.integration.alert.provider.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubEntity;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfig;
import com.google.gson.Gson;

// TODO Make these tests more useful once provider descriptors are fully implemented
public class HubDescriptorTest {

    private final MockGlobalHubEntity mockHubEntity = new MockGlobalHubEntity();
    private final MockGlobalHubRestModel mockHubRestModel = new MockGlobalHubRestModel();

    @Test
    public void testRepositoryCalls() {
        final GlobalHubConfigEntity entity = mockHubEntity.createGlobalEntity();

        final GlobalHubRepository globalHubRepository = Mockito.mock(GlobalHubRepository.class);
        Mockito.when(globalHubRepository.findAll()).thenReturn(Arrays.asList(entity));
        Mockito.when(globalHubRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(globalHubRepository.save(Mockito.any())).thenReturn(entity);
        Mockito.doNothing().when(globalHubRepository).deleteById(Mockito.anyLong());
        final HubRepositoryAccessor hubRepositoryAccessor = new HubRepositoryAccessor(globalHubRepository);

        final HubDescriptor hubDescriptor = new HubDescriptor(null, hubRepositoryAccessor, null);

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
    public void testTransformerCalls() throws AlertException {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final HubContentConverter hubContentConverter = new HubContentConverter(contentConverter);

        final HubDescriptor hubDescriptor = new HubDescriptor(hubContentConverter, null, null);

        final GlobalHubConfigEntity hubEntity = mockHubEntity.createGlobalEntity();
        final GlobalHubConfig hubRestModel = mockHubRestModel.createGlobalRestModel();

        final Config restModel = hubDescriptor.getGlobalContentConverter().populateRestModelFromDatabaseEntity(hubEntity);
        final DatabaseEntity entity = hubDescriptor.getGlobalContentConverter().populateDatabaseEntityFromRestModel(hubRestModel);
        final Config jsonRestModel = hubDescriptor.getGlobalContentConverter().getRestModelFromJson(gson.toJson(hubEntity));

        assertEquals(String.valueOf(hubEntity.getId()), restModel.getId());
        assertEquals(hubRestModel.getId(), String.valueOf(entity.getId()));
        assertEquals(hubRestModel.getId(), jsonRestModel.getId());
    }
}
