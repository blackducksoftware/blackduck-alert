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
package com.synopsys.integration.azure.boards.common.service.state;

public class WorkItemTypeProcessStateResponseModel {
    private String id;
    private String name;
    private Integer order;
    private String stateCategory;
    private String color;
    private WorkItemTypeStateCustomizationTypeModel customizationType;
    private Boolean hidden;
    private String url;

    public WorkItemTypeProcessStateResponseModel() {
        // For serialization
    }

    public WorkItemTypeProcessStateResponseModel(String id, String name, Integer order, String stateCategory, String color, WorkItemTypeStateCustomizationTypeModel customizationType, Boolean hidden, String url) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.stateCategory = stateCategory;
        this.color = color;
        this.customizationType = customizationType;
        this.hidden = hidden;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    public String getStateCategory() {
        return stateCategory;
    }

    public String getColor() {
        return color;
    }

    public WorkItemTypeStateCustomizationTypeModel getCustomizationType() {
        return customizationType;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public String getUrl() {
        return url;
    }

}
