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
package com.synopsys.integration.alert.web.api.about;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.common.action.ActionResponse;

@Component
public class AboutActions {
    private final AboutReader aboutReader;

    @Autowired
    public AboutActions(AboutReader aboutReader) {
        this.aboutReader = aboutReader;
    }

    public ActionResponse<AboutModel> getAboutModel() {
        return aboutReader.getAboutModel()
                   .map(content -> new ActionResponse<>(HttpStatus.OK, content))
                   .orElse(new ActionResponse<>(HttpStatus.NOT_FOUND));
    }

}
