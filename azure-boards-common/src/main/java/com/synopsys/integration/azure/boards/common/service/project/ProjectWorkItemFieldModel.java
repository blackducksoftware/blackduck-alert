/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.project;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.query.SupportedQueryOperationModel;

public class ProjectWorkItemFieldModel {
    public static final SupportedQueryOperationModel SUPPORTS_EQUALS = new SupportedQueryOperationModel("=", "SupportedOperations.Equals");

    private String name;
    private String referenceName;
    private String description;
    private String type;
    private String usage;
    private Boolean readOnly;
    private Boolean canSortBy;
    private Boolean isQueryable;
    private List<SupportedQueryOperationModel> supportedOperations;
    private Boolean isIdentity;
    private Boolean isPicklist;
    private Boolean isPicklistSuggested;
    private String picklistId;
    private String url;
    private Map<String, ReferenceLinkModel> _links;

    public static ProjectWorkItemFieldModel workItemStringField(String name, @Nullable String description) {
        String nameWithoutSpaces = StringUtils.remove(name, ' ');
        String referenceName = String.format("Custom.%s", nameWithoutSpaces);
        return workItemStringField(name, referenceName, description);
    }

    public static ProjectWorkItemFieldModel workItemStringField(String name, String referenceName, @Nullable String description) {
        return new ProjectWorkItemFieldModel(
            name,
            referenceName,
            description,
            "string",
            "workItem",
            true,
            true,
            true,
            List.of(SUPPORTS_EQUALS),
            true,
            false,
            false,
            null,
            null,
            null
        );
    }

    public ProjectWorkItemFieldModel() {
        // For serialization
    }

    public ProjectWorkItemFieldModel(
        String name,
        String referenceName,
        String description,
        String type,
        String usage,
        Boolean readOnly,
        Boolean canSortBy,
        Boolean isQueryable,
        List<SupportedQueryOperationModel> supportedOperations,
        Boolean isIdentity,
        Boolean isPicklist,
        Boolean isPicklistSuggested,
        String picklistId,
        String url,
        Map<String, ReferenceLinkModel> _links
    ) {
        this._links = _links;
        this.canSortBy = canSortBy;
        this.description = description;
        this.isIdentity = isIdentity;
        this.isPicklist = isPicklist;
        this.isPicklistSuggested = isPicklistSuggested;
        this.isQueryable = isQueryable;
        this.name = name;
        this.usage = usage;
        this.picklistId = picklistId;
        this.readOnly = readOnly;
        this.referenceName = referenceName;
        this.supportedOperations = supportedOperations;
        this.type = type;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getUsage() {
        return usage;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public Boolean getCanSortBy() {
        return canSortBy;
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

    public String getPicklistId() {
        return picklistId;
    }

    public List<SupportedQueryOperationModel> getSupportedOperations() {
        return supportedOperations;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

}
