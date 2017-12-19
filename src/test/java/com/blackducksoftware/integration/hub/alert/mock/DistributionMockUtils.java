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
package com.blackducksoftware.integration.hub.alert.mock;

import java.util.List;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.google.gson.JsonObject;

public class DistributionMockUtils {
    private final String commonId;
    private final String distributionConfigId;
    private final String distributionType;
    private final String name;
    private final String frequency;
    private final String filterByProject;
    private final List<String> configuredProjects;

    protected static final ProjectMockUtils projectMock = new ProjectMockUtils();

    public DistributionMockUtils() {
        this("1");
    }

    public DistributionMockUtils(final String distributionConfigId) {
        this("2", distributionConfigId, "test_type", "Name", "1 1 1 1 1 1", "true", projectMock.createProjectListing());
    }

    public DistributionMockUtils(final String id, final String distributionConfigId, final String distributionType, final String name, final String frequency, final String filterByProject, final List<String> configuredProjects) {
        super();
        this.commonId = id;
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
        this.configuredProjects = configuredProjects;
    }

    public String getCommonId() {
        return commonId;
    }

    public String getDistributionConfigId() {
        return distributionConfigId;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public String getName() {
        return name;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getFilterByProject() {
        return filterByProject;
    }

    public List<String> getProjects() {
        return configuredProjects;
    }

    public JsonObject getEmptyDistributionRestModelJson(final JsonObject json) {
        json.add("distributionConfigId", null);
        json.add("distributionType", null);
        json.add("name", null);
        json.add("frequency", null);
        json.add("filterByProject", null);
        json.add("configuredProjects", null);
        json.add("id", null);
        return json;
    }

    public JsonObject getDistributionRestModelJson(final JsonObject json) {
        json.addProperty("distributionConfigId", distributionConfigId);
        json.addProperty("distributionType", distributionType);
        json.addProperty("name", name);
        json.addProperty("frequency", frequency);
        json.addProperty("filterByProject", filterByProject);
        json.add("configuredProjects", projectMock.getProjectListingJson());
        json.addProperty("id", commonId);
        return json;
    }

    public CommonDistributionConfigEntity createDistributionConfigEntity() {
        final CommonDistributionConfigEntity configEntity = new CommonDistributionConfigEntity(Long.valueOf(distributionConfigId), distributionType, name, frequency, Boolean.valueOf(filterByProject));
        configEntity.setId(Long.valueOf(commonId));
        return configEntity;
    }

}
