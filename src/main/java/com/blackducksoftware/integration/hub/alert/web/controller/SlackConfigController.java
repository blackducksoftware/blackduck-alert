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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.web.model.SlackDistributionRestModel;

public class SlackConfigController extends ConfigController<SlackDistributionRestModel> {
    private final CommonConfigController commonConfigController;

    @Autowired
    public SlackConfigController(final CommonConfigController commonConfigController) {
        this.commonConfigController = commonConfigController;
    }

    @Override
    public List<SlackDistributionRestModel> getConfig(final Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> postConfig(final SlackDistributionRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> putConfig(final SlackDistributionRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> validateConfig(final SlackDistributionRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> deleteConfig(final SlackDistributionRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> testConfig(final SlackDistributionRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

}
