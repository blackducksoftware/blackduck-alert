/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem.response;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemUserModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public final class WorkItemResponseFields {
    public static final AzureFieldDefinition<String> System_Id = AzureFieldDefinition.stringField("System.Id");
    public static final AzureFieldDefinition<String> System_AreaPath = AzureFieldDefinition.stringField("System.AreaPath");
    public static final AzureFieldDefinition<String> System_TeamProject = AzureFieldDefinition.stringField("System.TeamProject");
    public static final AzureFieldDefinition<String> System_IterationPath = AzureFieldDefinition.stringField("System.IterationPath");
    public static final AzureFieldDefinition<String> System_WorkItemType = AzureFieldDefinition.stringField("System.WorkItemType");
    public static final AzureFieldDefinition<String> System_State = AzureFieldDefinition.stringField("System.State");
    public static final AzureFieldDefinition<String> System_Reason = AzureFieldDefinition.stringField("System.Reason");
    public static final AzureFieldDefinition<String> System_CreatedDate = AzureFieldDefinition.stringField("System.CreatedDate");
    public static final AzureFieldDefinition<String> System_ChangedDate = AzureFieldDefinition.stringField("System.ChangedDate");
    public static final AzureFieldDefinition<String> System_Title = AzureFieldDefinition.stringField("System.Title");
    public static final AzureFieldDefinition<String> System_Description = AzureFieldDefinition.stringField("System.Description");

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
                   .map(WorkItemResponseFields::convertToAzureFieldDefinition)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    private static AzureFieldDefinition convertToAzureFieldDefinition(Field field) {
        try {
            return (AzureFieldDefinition) field.get(null);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private WorkItemResponseFields() {
        // This class should not be instantiated
    }

}
