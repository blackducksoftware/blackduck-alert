/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.util;

import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;

public final class PagingParamValidationUtils {
    // TODO find a good home for this
    public static <T> Optional<ActionResponse<T>> createErrorActionResponseIfInvalid(Integer pageNumber, Integer pageSize) {
        StringBuilder messageBuilder = new StringBuilder();
        if (pageNumber < 0) {
            messageBuilder.append("The parameter 'pageNumber' cannot be negative. ");
        }

        if (pageSize < 1) {
            messageBuilder.append("The parameter 'pageSize' must be greater than 0.");
        }

        String errorMessage = messageBuilder.toString();
        if (errorMessage.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ActionResponse<>(HttpStatus.BAD_REQUEST, errorMessage));
    }

    private PagingParamValidationUtils() {
        // This class should not be instantiated
    }

}
