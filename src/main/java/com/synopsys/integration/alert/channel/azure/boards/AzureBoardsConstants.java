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
package com.synopsys.integration.alert.channel.azure.boards;

public class AzureBoardsConstants {
    //TODO: Figure out what constants we will need to use in Azure Boards

    //TODO: Figure out which of these values should be the default work item type. A list can be found here:
    //https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/work%20item%20types/list?view=azure-devops-rest-5.1
    public static final String DEFAULT_WORK_ITEM_TYPE = "Task";
    //public static final String DEFAULT_WORK_ITEM_TYPE_REFERENCE_NAME = "Microsoft.VSTS.WorkItemTypes.Task";
}
