package com.synopsys.integration.alert.performance.model;

import java.time.LocalDateTime;

import com.synopsys.integration.alert.performance.model.enumeration.ExecutionStatus;

public class PerformanceExecutionStatusModel {
    private final ExecutionStatus status;
    private final String message;
    private final LocalDateTime performanceTestStartingTime;
    private final LocalDateTime performanceTestEndingTime;

    public static PerformanceExecutionStatusModel success(LocalDateTime performanceTestStartingTime) {
        return success(performanceTestStartingTime, "Performance Test finished successfully.");
    }

    public static PerformanceExecutionStatusModel success(LocalDateTime performanceTestStartingTime, String message) {
        return new PerformanceExecutionStatusModel(ExecutionStatus.SUCCESS, performanceTestStartingTime, message);
    }

    public static PerformanceExecutionStatusModel failure(LocalDateTime performanceTestStartingTime, String message) {
        return new PerformanceExecutionStatusModel(ExecutionStatus.FAILURE, performanceTestStartingTime, message);
    }

    private PerformanceExecutionStatusModel(ExecutionStatus status, LocalDateTime performanceTestStartingTime, String message) {
        this.status = status;
        this.message = message;
        this.performanceTestStartingTime = performanceTestStartingTime;
        this.performanceTestEndingTime = LocalDateTime.now();
    }

    public ExecutionStatus getExecutionStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status.equals(ExecutionStatus.SUCCESS);
    }

    public boolean isFailure() {
        return status.equals(ExecutionStatus.FAILURE);
    }

    public LocalDateTime getPerformanceTestStartingTime() {
        return performanceTestStartingTime;
    }

    public LocalDateTime getPerformanceTestEndingTime() {
        return performanceTestEndingTime;
    }

    public String getMessage() {
        return message;
    }
}