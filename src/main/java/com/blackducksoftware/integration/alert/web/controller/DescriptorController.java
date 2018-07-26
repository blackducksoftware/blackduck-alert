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
package com.blackducksoftware.integration.alert.web.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.common.descriptor.Descriptor;
import com.blackducksoftware.integration.alert.common.descriptor.DescriptorMap;

@RestController
@RequestMapping(DescriptorController.DESCRIPTOR_PATH + "/{descriptorType}")
public class DescriptorController extends BaseController {
    public static final String DESCRIPTOR_PATH = BaseController.BASE_PATH + "/descriptors";

    private final DescriptorMap descriptorMap;

    @Autowired
    public DescriptorController(final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    @GetMapping
    public List<Descriptor> getDescriptors(@RequestParam(value = "descriptorName", required = false) final String descriptorName, @PathVariable final String descriptorType) {
        if (StringUtils.isNotBlank(descriptorName)) {
            return Arrays.asList(descriptorMap.getDescriptor(descriptorName));
        }

        return descriptorMap.getDescriptorMap().values()
                .stream()
                .filter(descriptorVals -> descriptorVals.getType().name().equalsIgnoreCase(descriptorType))
                .collect(Collectors.toList());
    }
}
