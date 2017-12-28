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
package com.blackducksoftware.integration.hub.alert.mock.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MockNotificationEntity implements MockEntityUtil<NotificationEntity> {
    private final String eventKey;
    private final Date createdAt;
    private final String notificationType;
    private final String projectName;
    private final String projectVersion;
    private final String componentName;
    private final String componentVersion;
    private final String policyRuleName;
    private final String person;
    private final String projectUrl;
    private final String projectVersionUrl;
    private final Collection<VulnerabilityEntity> vulnerabilityList;
    private final Long id;

    public MockNotificationEntity() {
        this("eventKey", new Date(400), "notificationType", "projectName", "projectVersion", "componentName", "componentVersion", "policyRuleName", "person", "projectUrl", "projectVersionUrl",
                Arrays.asList(new MockVulnerabilityEntity().createEntity()), 1L);
    }

    public MockNotificationEntity(final String eventKey, final Date createdAt, final String notificationType, final String projectName, final String projectVersion, final String componentName, final String componentVersion,
            final String policyRuleName, final String person, final String projectUrl, final String projectVersionUrl, final Collection<VulnerabilityEntity> vulnerabilityList, final Long id) {
        super();
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.notificationType = notificationType;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.policyRuleName = policyRuleName;
        this.person = person;
        this.projectUrl = projectUrl;
        this.projectVersionUrl = projectVersionUrl;
        this.vulnerabilityList = vulnerabilityList;
        this.id = id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getPolicyRuleName() {
        return policyRuleName;
    }

    public String getPerson() {
        return person;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }

    public Collection<VulnerabilityEntity> getVulnerabilityList() {
        return vulnerabilityList;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public NotificationEntity createEntity() {
        final NotificationEntity entity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person, vulnerabilityList);
        entity.setId(id);
        return entity;
    }

    @Override
    public NotificationEntity createEmptyEntity() {
        return new NotificationEntity();
    }

    @Override
    public String getEntityJson() {
        final Gson gson = new Gson();
        final JsonObject json = new JsonObject();
        json.addProperty("eventKey", eventKey);
        json.addProperty("createdAt", createdAt.toString());
        json.addProperty("notificationType", notificationType);
        json.addProperty("projectName", projectName);
        json.addProperty("projectVersion", projectVersion);
        json.addProperty("componentName", componentName);
        json.addProperty("componentVersion", componentVersion);
        json.addProperty("policyRuleName", policyRuleName);
        json.addProperty("person", person);
        json.addProperty("projectUrl", projectUrl);
        json.addProperty("projectVersionUrl", projectVersionUrl);
        final JsonElement array = gson.toJsonTree(vulnerabilityList);
        json.add("vulnerabilityList", array.getAsJsonArray());
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("eventKey", null);
        json.add("createdAt", null);
        json.add("notificationType", null);
        json.add("projectName", null);
        json.add("projectVersion", null);
        json.add("componentName", null);
        json.add("componentVersion", null);
        json.add("policyRuleName", null);
        json.add("person", null);
        json.add("projectUrl", null);
        json.add("projectVersionUrl", null);
        json.add("vulnerabilityList", null);
        json.add("id", null);
        return json.toString();
    }

}
