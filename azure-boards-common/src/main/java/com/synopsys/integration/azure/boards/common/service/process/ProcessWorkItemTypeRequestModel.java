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
package com.synopsys.integration.azure.boards.common.service.process;

public class ProcessWorkItemTypeRequestModel {
    private String inheritsFrom;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Boolean isDisabled;

    public static ProcessWorkItemTypeRequestModel copyWorkItem(ProcessWorkItemTypesResponseModel processWorkItemTypesResponseModel) {
        String inheritsFrom = processWorkItemTypesResponseModel.getReferenceName();
        String name = processWorkItemTypesResponseModel.getName();
        String description = processWorkItemTypesResponseModel.getDescription();
        String icon = processWorkItemTypesResponseModel.getIcon();
        String color = processWorkItemTypesResponseModel.getColor();
        Boolean isDisabled = processWorkItemTypesResponseModel.getDisabled();
        return new ProcessWorkItemTypeRequestModel(inheritsFrom, name, description, icon, color, isDisabled);
    }

    public ProcessWorkItemTypeRequestModel() {
        // For serialization
    }

    public ProcessWorkItemTypeRequestModel(String inheritsFrom, String name, String description, String icon, String color, Boolean isDisabled) {
        this.inheritsFrom = inheritsFrom;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.color = color;
        this.isDisabled = isDisabled;
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public Boolean getDisabled() {
        return isDisabled;
    }
}
