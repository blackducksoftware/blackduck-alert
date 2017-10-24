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

import com.blackducksoftware.integration.hub.alert.datasource.repository.ChannelDatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ChannelRestModel;

public interface ChannelController<D extends ChannelDatabaseEntity, R extends ChannelRestModel> {
    public List<R> getConfig(final Long id);

    public ResponseEntity<String> postConfig(R restModel);

    public ResponseEntity<String> putConfig(R restModel);

    // TODO TEST, DELETE

    public D restModelToDatabaseModel(R restModel);

    public R databaseModelToRestModel(D databaseModel);

    public List<R> databaseModelsToRestModels(final List<D> databaseModels);
}
