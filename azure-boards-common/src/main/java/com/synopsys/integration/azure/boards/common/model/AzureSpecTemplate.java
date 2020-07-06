/**
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class AzureSpecTemplate {
    private final String specTemplate;
    private final Map<String, String> replacementMappings;

    public AzureSpecTemplate(String specTemplate) {
        this.specTemplate = specTemplate;
        this.replacementMappings = new HashMap<>();
    }

    private AzureSpecTemplate(String specTemplate, Map<String, String> replacementMappings) {
        this.specTemplate = specTemplate;
        this.replacementMappings = replacementMappings;
    }

    public AzureSpecTemplate defineReplacement(String specTemplateKey, String replacementString) {
        HashMap<String, String> populatedReplacementMappings = new HashMap<>(replacementMappings);
        populatedReplacementMappings.put(specTemplateKey, replacementString);
        return new AzureSpecTemplate(specTemplate, populatedReplacementMappings);
    }

    public String populateSpec() {
        String populatedSpec = specTemplate;
        for (Map.Entry<String, String> replacementEntry : replacementMappings.entrySet()) {
            populatedSpec = StringUtils.replaceOnce(populatedSpec, replacementEntry.getKey(), replacementEntry.getValue());
        }
        return populatedSpec;
    }

    public String populateSpecAndClearReplacements() {
        String populatedSpec = populateSpec();
        if (!replacementMappings.isEmpty()) {
            replacementMappings.clear();
        }
        return populatedSpec;
    }

}
