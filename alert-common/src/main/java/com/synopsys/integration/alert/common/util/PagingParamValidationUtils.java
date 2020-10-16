package com.synopsys.integration.alert.common.util;

import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;

public class PagingParamValidationUtils {
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

}
