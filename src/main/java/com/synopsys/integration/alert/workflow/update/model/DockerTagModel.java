package com.synopsys.integration.alert.workflow.update.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DockerTagModel extends AlertSerializableModel {
    private String name;
    @SerializedName("full_size")
    private Long fullSize;
    private List<Object> images;
    private Long id;
    private Long repository;
    private Long creator;
    @SerializedName("last_updater")
    private Long lastUpdater;
    @SerializedName("last_updated")
    private String lastUpdated;
    private boolean v2;

    public DockerTagModel() {
    }

    public String getName() {
        return name;
    }

    public Long getFullSize() {
        return fullSize;
    }

    // If necessary, this can return a list of more specific Objects.
    public List<Object> getImages() {
        return images;
    }

    public Long getId() {
        return id;
    }

    public Long getRepository() {
        return repository;
    }

    public Long getCreator() {
        return creator;
    }

    public Long getLastUpdater() {
        return lastUpdater;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public boolean isV2() {
        return v2;
    }

}
