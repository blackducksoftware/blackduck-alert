package com.blackducksoftware.integration.hub.alert.digest.model;

import java.util.Collection;

import com.blackducksoftware.integration.hub.alert.model.Model;

public class DigestModel extends Model {
    private Collection<ProjectData> projectDataCollection;

    public DigestModel(Collection<ProjectData> projectDataCollection) {
        this.projectDataCollection = projectDataCollection;
    }

    public Collection<ProjectData> getProjectDataCollection() {
        return projectDataCollection;
    }

}
