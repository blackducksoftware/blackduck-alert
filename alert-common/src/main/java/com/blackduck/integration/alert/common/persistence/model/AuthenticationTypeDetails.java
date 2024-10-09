package com.blackduck.integration.alert.common.persistence.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class AuthenticationTypeDetails extends AlertSerializableModel {
    private final Long id;
    private final String name;

    public AuthenticationTypeDetails(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
