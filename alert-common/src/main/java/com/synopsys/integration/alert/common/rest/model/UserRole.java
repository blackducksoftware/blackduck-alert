package com.synopsys.integration.alert.common.rest.model;

import java.util.Set;

public class UserRole extends AlertSerializableModel {
    private final String name;
    private final Set<RoleTask> tasks;

    public UserRole(final String name, final Set<RoleTask> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    public String getName() {
        return name;
    }

    public Set<RoleTask> getTasks() {
        return tasks;
    }
}
