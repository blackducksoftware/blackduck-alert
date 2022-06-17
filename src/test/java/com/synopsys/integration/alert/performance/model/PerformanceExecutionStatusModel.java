package com.synopsys.integration.alert.performance.model;

import com.synopsys.integration.alert.performance.model.enumeration.ExecutionStatus;

public class PerformanceExecutionStatusModel {
    private final ExecutionStatus status;
    private final String message;

    public static PerformanceExecutionStatusModel succcess() {
        return success("");
    }

    public static PerformanceExecutionStatusModel success(String message) {
        return new PerformanceExecutionStatusModel(ExecutionStatus.SUCCESS, message);
    }

    public static PerformanceExecutionStatusModel failure(String message) {
        return new PerformanceExecutionStatusModel(ExecutionStatus.FAILURE, message);
    }

    private PerformanceExecutionStatusModel(ExecutionStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ExecutionStatus getExecutionStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status.equals(ExecutionStatus.SUCCESS);
    }

    public String getMessage() {
        return message;
    }
}