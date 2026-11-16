/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;

class JobNotificationFilterUtilsTest {
    @Test
    void verifyJobDoesNotFilterByProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel filteredDistributionJobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            false,
            false,
            "",
            ""
        );

        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(filteredDistributionJobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void verifyJobDoesNotMatchProjectAndPatternsEmptySimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel filteredDistributionJobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "",
            ""
        );

        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(filteredDistributionJobResponseModel, "projectName", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void verifyJobDoesNotMatchProjectNamePatternSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel filteredDistributionJobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "BM.*",
            "^a\\d$"
        );

        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(filteredDistributionJobResponseModel, "Won'tMatch", "a1");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectNameMatchWithProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            true,
            "projectN*",
            ""
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectNameMatchNoProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "projectN.*",
            ""
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchSelectedProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            true,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameNotMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "fakeNews");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            true,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithProjectPatternMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "projectN.*",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithInvalidProjectPatternMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(true, false, "projectN.*", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "wrong", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameNotMatchWithValidProjectPatternMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(true, false, "projectN.*", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "wrong");

        assertFalse(doesProjectApplyToJob);
    }

    private SimpleFilteredDistributionJobResponseModel createSimpleFilteredDistributionJobResponseModel(
        boolean filterByProject,
        boolean hasProjects,
        String projectNamePattern,
        String projectNameVersionPattern
    ) {
        return new SimpleFilteredDistributionJobResponseModel(
            1L,
            UUID.randomUUID(),
            filterByProject,
            projectNamePattern,
            projectNameVersionPattern,
            hasProjects
        );
    }

}
