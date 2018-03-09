package com.blackducksoftware.integration.hub.alert.mock.model;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.blackducksoftware.integration.hub.alert.web.model.ComponentRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.NotificationRestModel;
import com.blackducksoftware.integration.hub.notification.NotificationCategoryEnum;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MockNotificationRestModel extends MockRestModelUtil<NotificationRestModel> {
    private String eventKey;
    private String createdAt;
    private Set<String> notificationTypes;
    private String projectName;
    private String projectVersion;
    private Set<ComponentRestModel> components;
    private String projectUrl;
    private String projectVersionUrl;
    private String id;

    @SuppressWarnings("deprecation")
    public MockNotificationRestModel() {
        this("_eventKey_", new Date(400).toLocaleString(), Stream.of(NotificationCategoryEnum.POLICY_VIOLATION.name()).collect(Collectors.toSet()), "projectName", "projectVersion",
                Stream.of(new ComponentRestModel(), new ComponentRestModel()).collect(Collectors.toSet()), "projectUrl", "projectVersionUrl", "1");
    }

    private MockNotificationRestModel(final String eventKey, final String createdAt, final Set<String> notificationTypes, final String projectName, final String projectVersion, final Set<ComponentRestModel> components,
            final String projectUrl, final String projectVersionUrl, final String id) {
        super();
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.notificationTypes = notificationTypes;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.components = components;
        this.projectUrl = projectUrl;
        this.projectVersionUrl = projectVersionUrl;
        this.id = id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(final String eventKey) {
        this.eventKey = eventKey;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public Set<String> getNotificationTypes() {
        return notificationTypes;
    }

    public void setNotificationTypes(final Set<String> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(final String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public Set<ComponentRestModel> getComponents() {
        return components;
    }

    public void setComponents(final Set<ComponentRestModel> components) {
        this.components = components;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(final String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }

    public void setProjectVersionUrl(final String projectVersionUrl) {
        this.projectVersionUrl = projectVersionUrl;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public NotificationRestModel createRestModel() {
        return new NotificationRestModel(id, eventKey, createdAt, notificationTypes, projectName, projectVersion, components, projectUrl, projectVersionUrl);
    }

    @Override
    public NotificationRestModel createEmptyRestModel() {
        return new NotificationRestModel();
    }

    @Override
    public String getRestModelJson() {
        final Gson gson = new Gson();
        final JsonObject json = new JsonObject();
        json.addProperty("eventKey", eventKey);
        json.addProperty("createdAt", createdAt);
        final JsonElement notificationArray = gson.toJsonTree(notificationTypes);
        json.add("notificationTypes", notificationArray.getAsJsonArray());
        json.addProperty("projectName", projectName);
        json.addProperty("projectVersion", projectVersion);
        json.addProperty("projectUrl", projectUrl);
        json.addProperty("projectVersionUrl", projectVersionUrl);
        final JsonElement componentsArray = gson.toJsonTree(components);
        json.add("components", componentsArray.getAsJsonArray());
        json.addProperty("id", id);
        return json.toString();
    }

}
