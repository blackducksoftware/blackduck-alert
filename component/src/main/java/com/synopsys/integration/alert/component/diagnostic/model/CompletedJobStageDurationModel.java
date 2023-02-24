package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class CompletedJobStageDurationModel extends AlertSerializableModel {

    private static final long serialVersionUID = -6215504690854338841L;
    private final String name;
    private final String duration;

    public CompletedJobStageDurationModel(String name, String duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }
}
