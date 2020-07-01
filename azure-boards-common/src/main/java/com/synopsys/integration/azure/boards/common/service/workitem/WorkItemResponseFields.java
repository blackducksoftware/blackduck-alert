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
package com.synopsys.integration.azure.boards.common.service.workitem;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public final class WorkItemResponseFields {
    public static final AzureFieldDefinition<String> System_AreaPath = AzureFieldDefinition.stringField("System.AreaPath");
    public static final AzureFieldDefinition<String> System_TeamProject = AzureFieldDefinition.stringField("System.TeamProject");
    public static final AzureFieldDefinition<String> System_IterationPath = AzureFieldDefinition.stringField("System.IterationPath");
    public static final AzureFieldDefinition<String> System_WorkItemType = AzureFieldDefinition.stringField("System.WorkItemType");
    public static final AzureFieldDefinition<String> System_State = AzureFieldDefinition.stringField("System.State");
    public static final AzureFieldDefinition<String> System_Reason = AzureFieldDefinition.stringField("System.Reason");
    public static final AzureFieldDefinition<String> System_CreatedDate = AzureFieldDefinition.stringField("System.CreatedDate");
    public static final AzureFieldDefinition<String> System_ChangedDate = AzureFieldDefinition.stringField("System.ChangedDate");
    public static final AzureFieldDefinition<String> System_Title = AzureFieldDefinition.stringField("System.Title");

    public static final AzureFieldDefinition<String> Microsoft_VSTS_Common_StateChangeDate = AzureFieldDefinition.stringField("Microsoft.VSTS.Common.StateChangeDate");
    public static final AzureFieldDefinition<String> Microsoft_VSTS_CMMI_Blocked = AzureFieldDefinition.stringField("Microsoft.VSTS.CMMI.Blocked");
    public static final AzureFieldDefinition<String> Microsoft_VSTS_Common_Triage = AzureFieldDefinition.stringField("Microsoft.VSTS.Common.Triage");
    public static final AzureFieldDefinition<String> Microsoft_VSTS_CMMI_TaskType = AzureFieldDefinition.stringField("Microsoft.VSTS.CMMI.TaskType");
    public static final AzureFieldDefinition<String> Microsoft_VSTS_CMMI_RequiresReview = AzureFieldDefinition.stringField("Microsoft.VSTS.CMMI.RequiresReview");
    public static final AzureFieldDefinition<String> Microsoft_VSTS_CMMI_RequiresTest = AzureFieldDefinition.stringField("Microsoft.VSTS.CMMI.RequiresTest");
    public static final AzureFieldDefinition<Integer> Microsoft_VSTS_Common_Priority = AzureFieldDefinition.integerField("Microsoft.VSTS.Common.Priority");

    public static final AzureFieldDefinition<WorkItemUserModel> System_AssignedTo = new AzureFieldDefinition<>("System.AssignedTo", WorkItemUserModel.class);
    public static final AzureFieldDefinition<WorkItemUserModel> System_CreatedBy = new AzureFieldDefinition<>("System.CreatedBy", WorkItemUserModel.class);
    public static final AzureFieldDefinition<WorkItemUserModel> System_ChangedBy = new AzureFieldDefinition<>("System.ChangedBy", WorkItemUserModel.class);

    public static List<AzureFieldDefinition> list() {
        Field[] workItemClassFields = WorkItemResponseFields.class.getFields();
        return Stream.of(workItemClassFields)
                   .filter(field -> Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                   .filter(field -> field.getType().equals(AzureFieldDefinition.class))
                   .map(field -> {
                       try {
                           return (AzureFieldDefinition) field.get(null);
                       } catch (IllegalAccessException e) {
                           return null;
                       }
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

}
