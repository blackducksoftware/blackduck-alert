package com.synopsys.integration.alert.database.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class DistributionJobRepositoryTestIT {
    @Autowired
    private DistributionJobRepository distributionJobRepository;

    @AfterEach
    public void cleanup() {
        distributionJobRepository.deleteAllInBatch();
    }

    @Test
    public void findAndSortEnabledJobsMatchingFilters_ValidSyntaxTest() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<DistributionJobEntity> foundJobs = distributionJobRepository.findAndSortEnabledJobsMatchingFilters(0L, Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), pageRequest);
        assertEquals(0, foundJobs.getTotalElements());
    }

}
