/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.model;

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
