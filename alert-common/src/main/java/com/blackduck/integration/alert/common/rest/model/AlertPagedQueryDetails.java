package com.blackduck.integration.alert.common.rest.model;

import java.util.Optional;

import org.springframework.data.domain.Sort;

public class AlertPagedQueryDetails {
    private final int offset;
    private final int limit;
    private final String sortName;
    private final Sort.Direction sortOrder;
    private final String searchTerm;

    public AlertPagedQueryDetails(int offset, int limit) {
        this(offset, limit, null, null, null);
    }

    public AlertPagedQueryDetails(int offset, int limit, String sortName, Sort.Direction sortOrder, String searchTerm) {
        this.offset = offset;
        this.limit = limit;
        this.sortName = sortName;
        this.sortOrder = sortOrder;
        this.searchTerm = searchTerm;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public Optional<String> getSortName() {
        return Optional.ofNullable(sortName);
    }

    public Optional<Sort.Direction> getSortOrder() {
        return Optional.ofNullable(sortOrder);
    }

    public Optional<String> getSearchTerm() {
        return Optional.ofNullable(searchTerm);
    }
}
