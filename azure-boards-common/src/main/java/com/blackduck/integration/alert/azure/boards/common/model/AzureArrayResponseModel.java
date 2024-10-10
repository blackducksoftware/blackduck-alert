/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.model;

import java.util.List;

public class AzureArrayResponseModel<T> {
    private final Integer count;
    private final List<T> value;

    public AzureArrayResponseModel() {
        count = 0;
        value = List.of();
    }

    public AzureArrayResponseModel(Integer count, List<T> value) {
        this.count = count;
        this.value = value;
    }

    public Integer getCount() {
        return count;
    }

    public List<T> getValue() {
        return value;
    }

}
