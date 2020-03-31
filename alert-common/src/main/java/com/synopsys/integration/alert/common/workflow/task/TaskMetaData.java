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
package com.synopsys.integration.alert.common.workflow.task;

import java.util.Map;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class TaskMetaData extends AlertSerializableModel {
    private static final long serialVersionUID = -3249768131233749231L;
    private String taskName;
    private String type;
    private String fullyQualifiedType;
    private String nextRunTime;
    private Map<String, String> properties;

    public TaskMetaData(String taskName, String type, String fullyQualifiedType, String nextRunTime, Map<String, String> properties) {
        this.taskName = taskName;
        this.type = type;
        this.fullyQualifiedType = fullyQualifiedType;
        this.nextRunTime = nextRunTime;
        this.properties = properties;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getType() {
        return type;
    }

    public String getFullyQualifiedType() {
        return fullyQualifiedType;
    }

    public String getNextRunTime() {
        return nextRunTime;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
