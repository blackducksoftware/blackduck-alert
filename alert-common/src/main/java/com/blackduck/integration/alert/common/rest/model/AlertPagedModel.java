package com.blackduck.integration.alert.common.rest.model;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AlertPagedModel<M> extends AlertPagedDetails<M> implements Serializable {
    public static final String DEFAULT_PAGE_NUMBER_STRING = "0";
    public static final String DEFAULT_PAGE_SIZE_STRING = "10";
    public static final Integer DEFAULT_PAGE_NUMBER = Integer.valueOf(DEFAULT_PAGE_NUMBER_STRING);
    public static final Integer DEFAULT_PAGE_SIZE = Integer.valueOf(DEFAULT_PAGE_SIZE_STRING);

    public static <M> AlertPagedModel<M> empty(int currentPage, int pageSize) {
        return new AlertPagedModel<>(0, currentPage, pageSize, List.of());
    }

    public AlertPagedModel(int totalPages, int currentPage, int pageSize, List<M> models) {
        super(totalPages, currentPage, pageSize, models);
    }

    @Override
    public List<M> getModels() {
        return super.getModels();
    }

    @JsonIgnore
    public <T> AlertPagedModel<T> transformContent(Function<M, T> transformation) {
        List<T> transformedContent = getModels().stream().map(transformation).collect(Collectors.toList());
        return new AlertPagedModel<>(getTotalPages(), getCurrentPage(), getPageSize(), transformedContent);
    }

}
