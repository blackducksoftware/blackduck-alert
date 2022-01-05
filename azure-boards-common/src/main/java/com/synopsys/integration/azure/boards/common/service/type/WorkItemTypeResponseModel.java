/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.type;

import java.util.List;
import java.util.Map;

import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemIconModel;

public class WorkItemTypeResponseModel {
    private String color;
    private String description;
    private List<WorkItemTypeFieldInstanceModel> fieldInstances;
    private List<WorkItemTypeFieldInstanceModel> fields;
    private WorkItemIconModel icon;
    private Boolean disabled;
    private String name;
    private String referenceName;
    private List<WorkItemTypeStateResponseModel> states;
    private Map<String, List<WorkItemTypeTransitionModel>> transitions;
    private String xmlForm;
    private String url;
    private Map<String, ReferenceLinkModel> _links;

    public WorkItemTypeResponseModel() {
        // For serialization
    }

    public WorkItemTypeResponseModel(String color, String description, List<WorkItemTypeFieldInstanceModel> fieldInstances, List<WorkItemTypeFieldInstanceModel> fields,
        WorkItemIconModel icon, Boolean disabled, String name, String referenceName, List<WorkItemTypeStateResponseModel> states,
        Map<String, List<WorkItemTypeTransitionModel>> transitions, String xmlForm, String url, Map<String, ReferenceLinkModel> _links) {
        this.color = color;
        this.description = description;
        this.fieldInstances = fieldInstances;
        this.fields = fields;
        this.icon = icon;
        this.disabled = disabled;
        this.name = name;
        this.referenceName = referenceName;
        this.states = states;
        this.transitions = transitions;
        this.xmlForm = xmlForm;
        this.url = url;
        this._links = _links;
    }

    public String getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public List<WorkItemTypeFieldInstanceModel> getFieldInstances() {
        return fieldInstances;
    }

    public List<WorkItemTypeFieldInstanceModel> getFields() {
        return fields;
    }

    public WorkItemIconModel getIcon() {
        return icon;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public String getName() {
        return name;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public List<WorkItemTypeStateResponseModel> getStates() {
        return states;
    }

    public Map<String, List<WorkItemTypeTransitionModel>> getTransitions() {
        return transitions;
    }

    public String getXmlForm() {
        return xmlForm;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, ReferenceLinkModel> get_links() {
        return _links;
    }

}
