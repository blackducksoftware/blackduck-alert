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
package com.synopsys.integration.alert;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.web.model.AboutModel;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.ResourceUtil;

@Component
public class AboutReader {
    public static final String PRODUCT_VERSION_UNKNOWN = "unknown";
    private final Logger logger = LoggerFactory.getLogger(AboutReader.class);
    private final Gson gson;
    private final SystemStatusUtility systemStatusUtility;

    @Autowired
    public AboutReader(Gson gson, SystemStatusUtility systemStatusUtility) {
        this.gson = gson;
        this.systemStatusUtility = systemStatusUtility;
    }

    public AboutModel getAboutModel() {
        try {
            String aboutJson = ResourceUtil.getResourceAsString(getClass(), "/about.txt", StandardCharsets.UTF_8.toString());
            AboutModel aboutModel = gson.fromJson(aboutJson, AboutModel.class);
            String startupDate = systemStatusUtility.getStartupTime() != null ? DateUtils.formatDate(systemStatusUtility.getStartupTime(), RestConstants.JSON_DATE_FORMAT) : "";
            return new AboutModel(aboutModel.getVersion(), aboutModel.getCreated(), aboutModel.getDescription(), aboutModel.getProjectUrl(), systemStatusUtility.isSystemInitialized(), startupDate);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public String getProductVersion() {
        AboutModel aboutModel = getAboutModel();
        if (aboutModel != null) {
            return aboutModel.getVersion();
        } else {
            return PRODUCT_VERSION_UNKNOWN;
        }
    }

}
