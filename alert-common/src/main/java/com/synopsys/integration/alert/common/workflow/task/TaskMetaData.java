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

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class TaskMetaData extends AlertSerializableModel {
    private static final long serialVersionUID = -3249768131233749231L;
    private String taskName;
    private String name;
    private String fullyQualifiedName;
    private String nextRunTime;

    public TaskMetaData(String taskName, String name, String fullyQualifiedName, String nextRunTime) {
        this.taskName = taskName;
        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.nextRunTime = nextRunTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getName() {
        return name;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getNextRunTime() {
        return nextRunTime;
    }
}
