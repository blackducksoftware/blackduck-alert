package com.synopsys.integration.azure.boards.common.model;

import java.util.List;

public class AzureArrayWithCountResponseModel<T> {
    private final Integer count;
    private final List<T> value;

    public AzureArrayWithCountResponseModel() {
        count = 0;
        value = List.of();
    }

    public AzureArrayWithCountResponseModel(Integer count, List<T> value) {
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
