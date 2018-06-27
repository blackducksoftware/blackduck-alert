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
package com.blackducksoftware.integration.hub.alert.web.test.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.blackducksoftware.integration.hub.alert.descriptor.Descriptor;
import com.blackducksoftware.integration.hub.alert.web.controller.BaseController;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@RequestMapping(UniversalConfigController.UNIVERSAL_PATH)
public abstract class UniversalConfigController<D extends Descriptor> extends BaseController {
    public static final String UNIVERSAL_PATH = ConfigController.CONFIGURATION_PATH + "/{descriptorName}";

    private final List<D> descriptors;

    public UniversalConfigController(final List<D> descriptors) {
        this.descriptors = descriptors;
    }

    @GetMapping
    public abstract <R extends ConfigRestModel> List<R> getConfig(final Long id, @PathVariable final String descriptorName);

    @PostMapping
    public abstract ResponseEntity<String> postConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName);

    @PutMapping
    public abstract ResponseEntity<String> putConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName);

    @PostMapping(value = "/validate")
    public abstract ResponseEntity<String> validateConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName);

    @DeleteMapping
    public abstract ResponseEntity<String> deleteConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName);

    @PostMapping(value = "/test")
    public abstract ResponseEntity<String> testConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName);

    public D getDescriptor(final String descriptorName) {
        for (final D descriptor : descriptors) {
            if (descriptorName.equals(descriptor.getName())) {
                return descriptor;
            }
        }

        return null;
    }
}
