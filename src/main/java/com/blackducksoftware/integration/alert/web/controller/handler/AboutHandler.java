/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.alert.web.controller.handler;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.model.AboutModel;
import com.blackducksoftware.integration.alert.web.actions.AboutActions;
import com.blackducksoftware.integration.alert.web.model.AboutRestModel;

@Component
public class AboutHandler extends ControllerHandler {
    private final AboutActions aboutActions;

    @Autowired
    public AboutHandler(final ContentConverter contentConverter, final AboutActions aboutActions) {
        super(contentConverter);
        this.aboutActions = aboutActions;
    }

    public ResponseEntity<String> getAboutData() {
        final Optional<AboutModel> optionalModel = aboutActions.getAboutModel();
        if (optionalModel.isPresent()) {
            final AboutModel model = optionalModel.get();
            final AboutRestModel restModel = new AboutRestModel(model.getVersion(), model.getDescription(), model.getProjectUrl());
            return new ResponseEntity<>(getContentConverter().getJsonString(restModel), HttpStatus.OK);
        }
        return new ResponseEntity<>("Could not find the About model.", HttpStatus.NOT_FOUND);
    }
}
