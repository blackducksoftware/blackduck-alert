/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.tasks.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseController;

@RestController
@RequestMapping(TaskController.TASK_BASE_PATH)
public class TaskController extends BaseController {
    public static final String TASK_BASE_PATH = AlertRestConstants.BASE_PATH + "/task";
    private final TaskActions taskActions;

    @Autowired
    public TaskController(TaskActions taskActions) {
        this.taskActions = taskActions;
    }

    @GetMapping
    public MultiTaskMetaDataModel getAllTasks() {
        return ResponseFactory.createContentResponseFromAction(taskActions.getTasks());
    }
}
