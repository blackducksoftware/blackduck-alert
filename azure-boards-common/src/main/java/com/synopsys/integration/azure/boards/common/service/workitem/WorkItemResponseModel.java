package com.synopsys.integration.azure.boards.common.service.workitem;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class WorkItemResponseModel {
    public static final List<AzureFieldDefinition> FIELD_DEFINITIONS = WorkItemResponseFields.list();

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

    public Integer getId() {
        return id;
    }

    public Integer getRev() {
        return rev;
    }

    public JsonObject getFields() {
        return fields;
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

    public Map<String, ReferenceLinkModel> get_links() {
        return _links;
    }

}
