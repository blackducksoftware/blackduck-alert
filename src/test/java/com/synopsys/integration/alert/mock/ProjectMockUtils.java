/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.mock;

import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.synopsys.integration.alert.database.entity.ConfiguredProjectEntity;
import com.synopsys.integration.alert.database.relation.DistributionProjectRelation;

public class ProjectMockUtils {
    private final String projectOne;
    private final String projectTwo;
    private final String projectThree;
    private final String projectFour;

    public ProjectMockUtils() {
        this("Project one", "Project two", "Project three", "Project four");
    }

    public ProjectMockUtils(final String projectOne, final String projectTwo, final String projectThree, final String projectFour) {
        super();
        this.projectOne = projectOne;
        this.projectTwo = projectTwo;
        this.projectThree = projectThree;
        this.projectFour = projectFour;
    }

    public ConfiguredProjectEntity getProjectOneEntity() {
        return new ConfiguredProjectEntity(projectOne);
    }

    public ConfiguredProjectEntity getProjectTwoEntity() {
        return new ConfiguredProjectEntity(projectTwo);
    }

    public ConfiguredProjectEntity getProjectThreeEntity() {
        return new ConfiguredProjectEntity(projectThree);
    }

    public ConfiguredProjectEntity getProjectFourEntity() {
        return new ConfiguredProjectEntity(projectFour);
    }

    public String getProjectOne() {
        return projectOne;
    }

    public String getProjectTwo() {
        return projectTwo;
    }

    public String getProjectThree() {
        return projectThree;
    }

    public String getProjectFour() {
        return projectFour;
    }

    public List<DistributionProjectRelation> getProjectRelations() {
        final DistributionProjectRelation projectRelation1 = new DistributionProjectRelation(1L, 1L);
        final DistributionProjectRelation projectRelation2 = new DistributionProjectRelation(1L, 2L);
        final DistributionProjectRelation projectRelation3 = new DistributionProjectRelation(1L, 3L);
        final DistributionProjectRelation projectRelation4 = new DistributionProjectRelation(1L, 4L);
        return Arrays.asList(projectRelation1, projectRelation2, projectRelation3, projectRelation4);
    }

    public List<String> createProjectListing() {
        return Arrays.asList(projectOne, projectTwo, projectThree, projectFour);
    }

    public JsonArray getProjectListingJson() {
        final JsonArray json = new JsonArray();
        json.add(projectOne);
        json.add(projectTwo);
        json.add(projectThree);
        json.add(projectFour);
        return json;
    }
}
