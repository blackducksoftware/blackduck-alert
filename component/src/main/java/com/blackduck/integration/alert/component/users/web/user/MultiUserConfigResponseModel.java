package com.blackduck.integration.alert.component.users.web.user;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.MultiResponseModel;

public class MultiUserConfigResponseModel extends MultiResponseModel<UserConfig> {
    public MultiUserConfigResponseModel(final List<UserConfig> models) {
        super(models);
    }

    public List<UserConfig> getUsers() {
        return getModels();
    }
}
