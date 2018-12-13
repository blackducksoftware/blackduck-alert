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
package com.synopsys.integration.alert.mock.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.mock.NotificationTypeMockUtils;
import com.synopsys.integration.alert.mock.ProjectMockUtils;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class MockCommonDistributionRestModel extends MockRestModelUtil<CommonDistributionConfig> {
    protected static final ProjectMockUtils projectMock = new ProjectMockUtils();
    protected static final NotificationTypeMockUtils notificationTypeMock = new NotificationTypeMockUtils();
    private String id;
    private String distributionConfigId;
    private String distributionType;
    private String name;
    private final String providerName;
    private String frequency;
    private String filterByProject;
    private String projectNamePattern;
    private List<String> configuredProjects;
    private List<NotificationType> notificationTypes;
    private String lastRan;
    private String status;
    private String formatType;

    public MockCommonDistributionRestModel() {
        this("1");
    }

    private MockCommonDistributionRestModel(final String distributionConfigId) {
        this("2", distributionConfigId, HipChatChannel.COMPONENT_NAME.toString(), "Name", "provider_blackduck", FrequencyType.REAL_TIME.name(), "true", "projectNamePattern", projectMock.createProjectListing(),
            notificationTypeMock.createNotificiationTypeListing(),
            null, null, FormatType.DEFAULT.name());
    }

    private MockCommonDistributionRestModel(final String id, final String distributionConfigId, final String distributionType, final String name, final String providerName, final String frequency, final String filterByProject,
        final String projectNamePattern, final List<String> configuredProjects, final List<NotificationType> notificationTypes, final String lastRan, final String status, final String formatType) {
        super();
        this.id = id;
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.providerName = providerName;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.configuredProjects = configuredProjects;
        this.notificationTypes = notificationTypes;
        this.lastRan = lastRan;
        this.status = status;
        this.formatType = formatType;
    }

    public String getDistributionConfigId() {
        return distributionConfigId;
    }

    public void setDistributionConfigId(final String distributionConfigId) {
        this.distributionConfigId = distributionConfigId;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(final String distributionType) {
        this.distributionType = distributionType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(final String frequency) {
        this.frequency = frequency;
    }

    public String getFilterByProject() {
        return filterByProject;
    }

    public void setFilterByProject(final String filterByProject) {
        this.filterByProject = filterByProject;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public void setProjectNamePattern(final String projectNamePattern) {
        this.projectNamePattern = projectNamePattern;
    }

    public List<String> getProjects() {
        return configuredProjects;
    }

    public List<NotificationType> getNotifications() {
        return notificationTypes;
    }

    public List<String> getNotificationsAsStrings() {
        return notificationTypes.stream().map(notificationType -> notificationType.name()).collect(Collectors.toList());
    }

    public String getLastRan() {
        return lastRan;
    }

    public void setLastRan(final String lastRan) {
        this.lastRan = lastRan;
    }

    public String getStatus() {
        return status;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setConfiguredProjects(final List<String> configuredProjects) {
        this.configuredProjects = configuredProjects;
    }

    public void setNotificationTypes(final List<NotificationType> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    public void setFormatType(final String formatType) {
        this.formatType = formatType;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public CommonDistributionConfig createEmptyRestModel() {
        return new CommonDistributionConfig();
    }

    @Override
    public CommonDistributionConfig createRestModel() {
        return new CommonDistributionConfig(id, distributionConfigId, distributionType, name, providerName, frequency, filterByProject, projectNamePattern, projectMock.createProjectListing(),
            notificationTypeMock.createNotificiationTypeListingAsStrings(),
            formatType);
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("distributionConfigId", distributionConfigId);
        json.addProperty("distributionType", distributionType);
        json.addProperty("name", name);
        json.addProperty("providerName", providerName);
        json.addProperty("frequency", frequency);
        json.addProperty("filterByProject", filterByProject);
        json.addProperty("projectNamePattern", projectNamePattern);
        json.add("configuredProjects", projectMock.getProjectListingJson());
        json.add("notificationTypes", notificationTypeMock.getNotificationListingJson());
        json.addProperty("formatType", formatType);
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
