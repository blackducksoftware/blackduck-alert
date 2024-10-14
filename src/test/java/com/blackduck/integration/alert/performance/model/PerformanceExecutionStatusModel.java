/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance.model;

import java.time.LocalDateTime;

import com.blackduck.integration.alert.performance.model.enumeration.ExecutionStatus;

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