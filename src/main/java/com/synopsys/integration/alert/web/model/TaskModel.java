package com.synopsys.integration.alert.web.model;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class TaskModel extends AlertSerializableModel {
    private static final long serialVersionUID = -3249768131233749231L;
    private String name;
    private String nextRunTime;

    public TaskModel(String name, String nextRunTime) {
        this.name = name;
        this.nextRunTime = nextRunTime;
    }

    public String getName() {
        return name;
    }

    public String getNextRunTime() {
        return nextRunTime;
    }
}
