/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem.response;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemCommentVersionRefModel;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemRelationModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldsExtractor;

public class WorkItemResponseModel {
    private Integer id;
    private Integer rev;
    private JsonObject fields;
    private List<WorkItemRelationModel> relations;
    private WorkItemCommentVersionRefModel commentVersionRef;
    private String url;
    private Map<String, ReferenceLinkModel> _links;

    public WorkItemResponseModel() {
        // For serialization
    }

    public WorkItemResponseModel(Integer id, Integer rev, JsonObject fields, List<WorkItemRelationModel> relations, WorkItemCommentVersionRefModel commentVersionRef, String url,
        Map<String, ReferenceLinkModel> _links) {
        this.id = id;
        this.rev = rev;
        this.fields = fields;
        this.relations = relations;
        this.commentVersionRef = commentVersionRef;
        this.url = url;
        this._links = _links;
    }

    public Integer getId() {
        return id;
    }

    public Integer getRev() {
        return rev;
    }

    public JsonObject getFields() {
        return fields;
    }

    public WorkItemFieldsWrapper createFieldsWrapper(Gson gson) {
        AzureFieldsExtractor azureFieldsExtractor = new AzureFieldsExtractor(gson);
        return new WorkItemFieldsWrapper(azureFieldsExtractor, fields);
    }

    public List<WorkItemRelationModel> getRelations() {
        return relations;
    }

    public WorkItemCommentVersionRefModel getCommentVersionRef() {
        return commentVersionRef;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

}
