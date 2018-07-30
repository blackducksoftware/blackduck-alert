package com.blackducksoftware.integration.alert.provider.blackduck;

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
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckEntity;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckRestModel;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfig;
import com.google.gson.Gson;

// TODO Make these tests more useful once provider descriptors are fully implemented
public class BlackDuckDescriptorTest {

    private final MockGlobalBlackDuckEntity mockHubEntity = new MockGlobalBlackDuckEntity();
    private final MockGlobalBlackDuckRestModel mockHubRestModel = new MockGlobalBlackDuckRestModel();

    @Test
    public void testRepositoryCalls() {
        final GlobalBlackDuckConfigEntity entity = mockHubEntity.createGlobalEntity();

        final GlobalBlackDuckRepository globalHubRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        Mockito.when(globalHubRepository.findAll()).thenReturn(Arrays.asList(entity));
        Mockito.when(globalHubRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(globalHubRepository.save(Mockito.any())).thenReturn(entity);
        Mockito.doNothing().when(globalHubRepository).deleteById(Mockito.anyLong());
        final BlackDuckRepositoryAccessor blackDuckRepositoryAccessor = new BlackDuckRepositoryAccessor(globalHubRepository);

        final BlackDuckDescriptor hubDescriptor = new BlackDuckDescriptor(null, blackDuckRepositoryAccessor, null);

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
        final BlackDuckContentConverter hubContentConverter = new BlackDuckContentConverter(contentConverter);

        final BlackDuckDescriptor hubDescriptor = new BlackDuckDescriptor(hubContentConverter, null, null);

        final GlobalBlackDuckConfigEntity hubEntity = mockHubEntity.createGlobalEntity();
        final GlobalBlackDuckConfig hubRestModel = mockHubRestModel.createGlobalRestModel();

        final Config restModel = hubDescriptor.getGlobalContentConverter().populateRestModelFromDatabaseEntity(hubEntity);
        final DatabaseEntity entity = hubDescriptor.getGlobalContentConverter().populateDatabaseEntityFromRestModel(hubRestModel);
        final Config jsonRestModel = hubDescriptor.getGlobalContentConverter().getRestModelFromJson(gson.toJson(hubEntity));

        assertEquals(String.valueOf(hubEntity.getId()), restModel.getId());
        assertEquals(hubRestModel.getId(), String.valueOf(entity.getId()));
        assertEquals(hubRestModel.getId(), jsonRestModel.getId());
    }
}
