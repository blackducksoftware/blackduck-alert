/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.actions.AboutActions;
import com.synopsys.integration.alert.web.model.AboutModel;

@RestController
public class AboutController extends BaseController {
    public static final String ERROR_ABOUT_MODEL_NOT_FOUND = "Could not find the About model.";

    private final AboutActions aboutActions;
    private final ResponseFactory responseFactory;
    private final ContentConverter contentConverter;

    @Autowired
    public AboutController(final AboutActions aboutActions, final ResponseFactory responseFactory, final ContentConverter contentConverter) {
        this.aboutActions = aboutActions;
        this.responseFactory = responseFactory;
        this.contentConverter = contentConverter;
    }

    @GetMapping(value = "/about")
    public ResponseEntity<String> about() {
        final Optional<AboutModel> optionalModel = aboutActions.getAboutModel();
        if (optionalModel.isPresent()) {
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(optionalModel.get()));
        }
        return responseFactory.createMessageResponse(HttpStatus.NOT_FOUND, ERROR_ABOUT_MODEL_NOT_FOUND);
    }

}
