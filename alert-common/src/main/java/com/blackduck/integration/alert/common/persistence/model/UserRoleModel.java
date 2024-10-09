package com.blackduck.integration.alert.common.persistence.model;

import java.util.Map;
import java.util.Objects;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class UserRoleModel extends AlertSerializableModel {
    private final Long id;
    private final String name;
    private final Boolean custom;
    private final PermissionMatrixModel permissions;

    public UserRoleModel(Long id, String name, Boolean custom, PermissionMatrixModel permissions) {
        this.id = id;
        this.name = name;
        this.custom = custom;
        this.permissions = permissions;
    }

    public static final UserRoleModel of(String name) {
        return of(name, false);
    }

    public static final UserRoleModel of(String name, Boolean custom) {
        Objects.requireNonNull(name);
        return new UserRoleModel(null, name, custom, new PermissionMatrixModel(Map.of()));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCustom() {
        return custom;
    }

    public PermissionMatrixModel getPermissions() {
        return permissions;
    }

}
