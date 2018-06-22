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
package com.blackducksoftware.integration.hub.alert.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@RequestMapping(GlobalConfigController.GLOBAL_PATH)
public abstract class GlobalConfigController<R extends ConfigRestModel> extends ConfigController<R> {
    public static final String GLOBAL_PATH = ConfigController.CONFIGURATION_PATH + "/global";
    public static final String CHANNEL_PATH = ConfigController.CONFIGURATION_PATH + "/channel";
    public static final String PROVIDER_PATH = ConfigController.CONFIGURATION_PATH + "/provider";

}
