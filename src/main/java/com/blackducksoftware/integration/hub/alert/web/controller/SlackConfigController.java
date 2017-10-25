/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.SlackConfigRestModel;

public class SlackConfigController implements ConfigController<SlackConfigEntity, SlackConfigRestModel> {

    @Override
    public List<SlackConfigRestModel> getConfig(final Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> postConfig(final SlackConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> putConfig(final SlackConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> deleteConfig(final SlackConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> testConfig(final SlackConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SlackConfigEntity restModelToDatabaseModel(final SlackConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SlackConfigRestModel databaseModelToRestModel(final SlackConfigEntity databaseModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SlackConfigRestModel> databaseModelsToRestModels(final List<SlackConfigEntity> databaseModels) {
        // TODO Auto-generated method stub
        return null;
    }

}
