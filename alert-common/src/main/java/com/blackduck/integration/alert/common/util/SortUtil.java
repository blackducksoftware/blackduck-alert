/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;

public final class SortUtil {

    public static Sort createSortByFieldName(@Nullable String fieldName, @Nullable String direction) {
        Sort sort = Sort.unsorted();
        if (StringUtils.isNotBlank(fieldName) && StringUtils.isNotBlank(direction)) {
            Optional<Sort.Direction> sortDirection = Sort.Direction.fromOptionalString(direction);
            if (sortDirection.isPresent()) {
                sort = sortDirection
                    .filter(Sort.Direction::isAscending)
                    .map(ignored -> Sort.by(Sort.Order.asc(fieldName)))
                    .orElse(Sort.by(Sort.Order.desc(fieldName)));
            }
        }

        return sort;
    }

    private SortUtil() {
        // prevent construction of new instances.
    }
}
