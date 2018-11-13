package com.synopsys.integration.alert.channel.hipchat.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;

public class GlobalHipChatRepositoryIT extends AlertIntegrationTest {
    @Autowired
    private HipChatGlobalRepository repository;

    @Test
    public void testSaveEntity() {
        // make sure all the test data is deleted
        repository.deleteAll();
        final String apiKey = "api_key";
        final HipChatGlobalConfigEntity entity = new HipChatGlobalConfigEntity(apiKey, "");
        final HipChatGlobalConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final HipChatGlobalConfigEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(apiKey, foundEntity.getApiKey());
    }
}
