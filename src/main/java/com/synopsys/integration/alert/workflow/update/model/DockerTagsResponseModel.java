package com.synopsys.integration.alert.workflow.update.model;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DockerTagsResponseModel extends AlertSerializableModel {
    public static final DockerTagsResponseModel EMPTY = new DockerTagsResponseModel();

    static {
        EMPTY.count = 0;
        EMPTY.next = null;
        EMPTY.previous = null;
        EMPTY.results = List.of();
    }

    private int count;
    private String next;
    private String previous;
    private List<DockerTagModel> results;

    public DockerTagsResponseModel() {
    }

    public int getCount() {
        return count;
    }

    public boolean hasNextPage() {
        return null != next;
    }

    public String getNextPageUrl() {
        return next;
    }

    public boolean hasPreviousPage() {
        return null != previous;
    }

    public String getPreviousPageUrl() {
        return previous;
    }

    public List<DockerTagModel> getResults() {
        return results;
    }

    public boolean isEmpty() {
        return count == 0 || results.isEmpty();
    }

}
