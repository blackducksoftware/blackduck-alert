package com.blackducksoftware.integration.alert.provider.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubEntity;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubRepository;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
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

        final HubDescriptor hubDescriptor = new HubDescriptor(globalHubRepository, null, null);

        final List<? extends DatabaseEntity> entities = hubDescriptor.readGlobalEntities();
        final Optional<? extends DatabaseEntity> foundEntity = hubDescriptor.readGlobalEntity(1);
        final Optional<? extends DatabaseEntity> savedEntity = hubDescriptor.saveGlobalEntity(entity);
        hubDescriptor.deleteGlobalEntity(1);

        assertEquals(1, entities.size());
        assertEquals(entity, entities.get(0));

        assertTrue(foundEntity.isPresent());
        assertTrue(savedEntity.isPresent());

        assertEquals(entity, foundEntity.get());
        assertEquals(entity, savedEntity.get());
    }

    @Test
    public void testTransformerCalls() throws AlertException {
        final Gson gson = new Gson();
        final ObjectTransformer objectTransformer = new ObjectTransformer();

        final HubDescriptor hubDescriptor = new HubDescriptor(null, gson, objectTransformer);

        final GlobalHubConfigEntity hubEntity = mockHubEntity.createGlobalEntity();
        final GlobalHubConfigRestModel hubRestModel = mockHubRestModel.createGlobalRestModel();

        final ConfigRestModel restModel = hubDescriptor.convertFromGlobalEntityToGlobalRestModel(hubEntity);
        final DatabaseEntity entity = hubDescriptor.convertFromGlobalRestModelToGlobalConfigEntity(hubRestModel);
        final ConfigRestModel jsonRestModel = hubDescriptor.convertFromStringToGlobalRestModel(gson.toJson(hubEntity));

        assertEquals(String.valueOf(hubEntity.getId()), restModel.getId());
        assertEquals(hubRestModel.getId(), String.valueOf(entity.getId()));
        assertEquals(hubRestModel.getId(), jsonRestModel.getId());
    }
}
