/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.repository.SettingRepository;
import com.blackducksoftware.integration.hub.alert.ui.model.SettingModel;

@Component
public class DatabaseLoader implements CommandLineRunner {
    private final SettingRepository repository;

    @Autowired
    public DatabaseLoader(final SettingRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(final String... args) throws Exception {
        this.repository.save(new SettingModel("test", "true", "boolean"));
    }

}
