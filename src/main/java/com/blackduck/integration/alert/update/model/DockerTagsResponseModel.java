/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.update.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

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

    public DockerTagsResponseModel(int count, String next, String previous, List<DockerTagModel> results) {
        this.count = count;
        this.next = next;
        this.previous = previous;
        this.results = results;
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
