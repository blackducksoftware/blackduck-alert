package com.synopsys.integration.alert.database.entity.repository.global;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;

public class GlobalRepositoryIT extends AlertIntegrationTest {
    @Autowired
    private GlobalBlackDuckRepository repository;

    @Test
    public void testSaveEntity() {
        repository.deleteAll();
        final Integer hubTimeout = 300;
        final String hubApiKey = "hub_api_key";
        final String hubUrl = "hub_url";
        final GlobalBlackDuckConfigEntity entity = new GlobalBlackDuckConfigEntity(hubTimeout, hubApiKey, hubUrl);
        final GlobalBlackDuckConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final Optional<GlobalBlackDuckConfigEntity> foundEntity = repository.findById(savedEntity.getId());
        assertEquals(hubTimeout, foundEntity.get().getBlackDuckTimeout());
        assertEquals(hubApiKey, foundEntity.get().getBlackDuckApiKey());
        assertEquals(hubUrl, foundEntity.get().getBlackDuckUrl());
    }
}
