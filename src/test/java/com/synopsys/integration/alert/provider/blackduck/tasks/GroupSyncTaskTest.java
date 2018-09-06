package com.synopsys.integration.alert.provider.blackduck.tasks;

import org.junit.Test;

public class GroupSyncTaskTest extends SyncTaskSharedTest {

    @Test
    public void testRunInitial() throws Exception {
        //        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        //        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        //
        //        final HubService hubService = createMockHubService(blackDuckProperties);
        //
        //        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(null);
        //
        //        final GroupSyncTask groupSyncTask = new GroupSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor);
        //        groupSyncTask.run();
        //
        //        assertEquals(3, blackDuckUserRepositoryAccessor.readEntities().size());
    }

    @Test
    public void testRunAdd() throws Exception {

    }

    @Test
    public void testRunDelete() throws Exception {

    }
}
