package com.synopsys.integration.azure.boards.common.service.project;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;

public class ProjectWorkItemFieldResponseModel {
    private Map<String, ReferenceLinkModel> _links;
    private Boolean canSortBy;
    private String description;
    private Boolean isIdentity;
    private Boolean isPicklist;
    private Boolean isPicklistSuggested;
    private Boolean isQueryable;
    private String name;
    private String picklistId;
    private Boolean readOnly;
    private String referenceName;
    private List<JsonObject> supportedOperations;
    private String type;
    private String url;

    public ProjectWorkItemFieldResponseModel() {
        // For serialization
    }

    public ProjectWorkItemFieldResponseModel(Map<String, ReferenceLinkModel> _links, Boolean canSortBy, String description, Boolean isIdentity, Boolean isPicklist, Boolean isPicklistSuggested, Boolean isQueryable, String name,
        String picklistId, Boolean readOnly, String referenceName, List<JsonObject> supportedOperations, String type, String url) {
        this._links = _links;
        this.canSortBy = canSortBy;
        this.description = description;
        this.isIdentity = isIdentity;
        this.isPicklist = isPicklist;
        this.isPicklistSuggested = isPicklistSuggested;
        this.isQueryable = isQueryable;
        this.name = name;
        this.picklistId = picklistId;
        this.readOnly = readOnly;
        this.referenceName = referenceName;
        this.supportedOperations = supportedOperations;
        this.type = type;
        this.url = url;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

    public Boolean getCanSortBy() {
        return canSortBy;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getIdentity() {
        return isIdentity;
    }

    public Boolean getPicklist() {
        return isPicklist;
    }

    public Boolean getPicklistSuggested() {
        return isPicklistSuggested;
    }

    public Boolean getQueryable() {
        return isQueryable;
    }

    public String getName() {
        return name;
    }

    public String getPicklistId() {
        return picklistId;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public List<JsonObject> getSupportedOperations() {
        return supportedOperations;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

}
