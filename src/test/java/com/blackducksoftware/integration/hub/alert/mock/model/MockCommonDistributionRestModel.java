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
package com.blackducksoftware.integration.hub.alert.mock.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.mock.NotificationTypeMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.ProjectMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.hub.throwaway.NotificationCategoryEnum;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MockCommonDistributionRestModel extends MockRestModelUtil<CommonDistributionConfigRestModel> {
    private String id;
    private String distributionConfigId;
    private String distributionType;
    private String name;
    private String frequency;
    private String filterByProject;
    private List<String> configuredProjects;
    private List<NotificationCategoryEnum> notificationTypes;
    private String lastRan;
    private String status;

    protected static final ProjectMockUtils projectMock = new ProjectMockUtils();

    protected static final NotificationTypeMockUtils notificationTypeMock = new NotificationTypeMockUtils();

    public MockCommonDistributionRestModel() {
        this("1");
    }

    private MockCommonDistributionRestModel(final String distributionConfigId) {
        this("2", distributionConfigId, SupportedChannels.HIPCHAT.toString(), "Name", DigestTypeEnum.REAL_TIME.name(), "true", projectMock.createProjectListing(), notificationTypeMock.createNotificiationTypeListing(), null, null);
    }

    private MockCommonDistributionRestModel(final String id, final String distributionConfigId, final String distributionType, final String name, final String frequency, final String filterByProject, final List<String> configuredProjects,
            final List<NotificationCategoryEnum> notificationTypes, final String lastRan, final String status) {
        super();
        this.id = id;
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
        this.configuredProjects = configuredProjects;
        this.notificationTypes = notificationTypes;
        this.lastRan = lastRan;
        this.status = status;
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

    public List<NotificationCategoryEnum> getNotifications() {
        return notificationTypes;
    }

    public List<String> getNotificationsAsStrings() {
        return notificationTypes.stream().map(notificationType -> notificationType.name()).collect(Collectors.toList());
    }

    public String getLastRan() {
        return lastRan;
    }

    public String getStatus() {
        return status;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setDistributionConfigId(final String distributionConfigId) {
        this.distributionConfigId = distributionConfigId;
    }

    public void setDistributionType(final String distributionType) {
        this.distributionType = distributionType;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setFrequency(final String frequency) {
        this.frequency = frequency;
    }

    public void setFilterByProject(final String filterByProject) {
        this.filterByProject = filterByProject;
    }

    public void setConfiguredProjects(final List<String> configuredProjects) {
        this.configuredProjects = configuredProjects;
    }

    public void setNotificationTypes(final List<NotificationCategoryEnum> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    public void setLastRan(final String lastRan) {
        this.lastRan = lastRan;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public CommonDistributionConfigRestModel createEmptyRestModel() {
        return new CommonDistributionConfigRestModel();
    }

    @Override
    public CommonDistributionConfigRestModel createRestModel() {
        return new CommonDistributionConfigRestModel(id, distributionConfigId, distributionType, name, frequency, filterByProject, projectMock.createProjectListing(), notificationTypeMock.createNotificiationTypeListingAsStrings());
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("distributionConfigId", distributionConfigId);
        json.addProperty("distributionType", distributionType);
        json.addProperty("name", name);
        json.addProperty("frequency", frequency);
        json.addProperty("filterByProject", filterByProject);
        json.add("configuredProjects", projectMock.getProjectListingJson());
        json.add("notificationTypes", notificationTypeMock.getNotificationListingJson());
        json.addProperty("id", id);
        return json.toString();
    }

    public String combineWithRestModelJson(final JsonObject jsonObject) {
        final String distributionJson = getRestModelJson();
        final JsonParser jsonParser = new JsonParser();
        final JsonObject newJson = jsonParser.parse(distributionJson).getAsJsonObject();

        for (final Map.Entry<String, JsonElement> entry : newJson.entrySet()) {
            jsonObject.add(entry.getKey(), entry.getValue());
        }

        return jsonObject.toString();
    }

}
