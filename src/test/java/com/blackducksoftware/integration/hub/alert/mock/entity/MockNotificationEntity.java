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

import java.util.Date;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.throwaway.NotificationCategoryEnum;
import com.google.gson.JsonObject;

public class MockNotificationEntity extends MockEntityUtil<NotificationEntity> {
    private final String eventKey;
    private final Date createdAt;
    private final NotificationCategoryEnum notificationType;
    private final String projectName;
    private final String projectVersion;
    private final String componentName;
    private final String componentVersion;
    private final String policyRuleName;
    private final String policyRuleUser;
    private final String projectUrl;
    private final String projectVersionUrl;
    private final Long id;

    public MockNotificationEntity() {
        this("_event_key_", new Date(400), NotificationCategoryEnum.POLICY_VIOLATION, "projectName", "projectVersion", "componentName", "componentVersion", "policyRuleName", "policyRuleUser", "projectUrl", "projectVersionUrl", 1L);
    }

    private MockNotificationEntity(final String eventKey, final Date createdAt, final NotificationCategoryEnum notificationType, final String projectName, final String projectVersion, final String componentName,
            final String componentVersion, final String policyRuleName, final String policyRuleUser, final String projectUrl, final String projectVersionUrl, final Long id) {
        super();
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.notificationType = notificationType;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.policyRuleName = policyRuleName;
        this.policyRuleUser = policyRuleUser;
        this.projectUrl = projectUrl;
        this.projectVersionUrl = projectVersionUrl;
        this.id = id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public NotificationCategoryEnum getNotificationType() {
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

    public String getPolicyRuleUser() {
        return policyRuleUser;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public NotificationEntity createEntity() {
        final NotificationEntity entity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, policyRuleUser);
        entity.setId(id);
        return entity;
    }

    @Override
    public NotificationEntity createEmptyEntity() {
        return new NotificationEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("eventKey", eventKey);
        // Gson uses locale by default thus I need to use it here
        json.addProperty("createdAt", createdAt.toLocaleString());
        json.addProperty("notificationType", notificationType.name());
        json.addProperty("projectName", projectName);
        json.addProperty("projectVersion", projectVersion);
        json.addProperty("componentName", componentName);
        json.addProperty("componentVersion", componentVersion);
        json.addProperty("policyRuleName", policyRuleName);
        json.addProperty("policyRuleUser", policyRuleUser);
        json.addProperty("projectUrl", projectUrl);
        json.addProperty("projectVersionUrl", projectVersionUrl);
        json.addProperty("id", id);
        return json.toString();
    }

}
