/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ProviderProjectSelectOption extends AlertSerializableModel {
    private final String name;
    private final String href;
    private final String projectOwnerEmail;
    private final String description;

    public ProviderProjectSelectOption(String name, String href, String projectOwnerEmail, String description) {
        this.name = name;
        this.href = href;
        this.projectOwnerEmail = projectOwnerEmail;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public String getProjectOwnerEmail() {
        return projectOwnerEmail;
    }

    public String getDescription() {
        return description;
    }

}
