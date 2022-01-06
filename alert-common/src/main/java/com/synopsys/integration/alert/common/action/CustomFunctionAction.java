/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.exception.IntegrationException;

public abstract class CustomFunctionAction<T> {
    private final AuthorizationManager authorizationManager;

    public CustomFunctionAction(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public ActionResponse<T> createResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            if (!isAllowed(fieldModel)) {
                return new ActionResponse<>(HttpStatus.FORBIDDEN, ResponseFactory.UNAUTHORIZED_REQUEST_MESSAGE);
            }

            Collection<AlertFieldStatus> relatedFieldStatuses = validateRelatedFields(fieldModel);
            if (!relatedFieldStatuses.isEmpty()) {
                Optional<ActionResponse<T>> validationActionResponse = createValidationActionResponse(relatedFieldStatuses);
                if (validationActionResponse.isPresent()) {
                    return validationActionResponse.get();
                }
            }

            return createActionResponse(fieldModel, servletContentWrapper);
        } catch (ResponseStatusException e) {
            return new ActionResponse<>(e.getStatus(), e.getReason());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    public abstract ActionResponse<T> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) throws IntegrationException;

    // TODO consider making custom-endpoints declaring a validation endpoint that must be checked first by the UI
    protected abstract Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel);

    protected boolean isAllowed(FieldModel fieldModel) {
        return authorizationManager.hasExecutePermission(fieldModel.getContext(), fieldModel.getDescriptorName());
    }

    private Optional<ActionResponse<T>> createValidationActionResponse(Collection<AlertFieldStatus> fieldStatuses) {
        Predicate<AlertFieldStatus> hasErrorSeverity = status -> FieldStatusSeverity.ERROR.equals(status.getSeverity());
        boolean hasErrors = fieldStatuses
            .stream()
            .anyMatch(hasErrorSeverity);
        if (hasErrors) {
            String errorMessages = fieldStatuses
                .stream()
                .filter(hasErrorSeverity)
                .map(AlertFieldStatus::getFieldMessage)
                .distinct()
                .collect(Collectors.joining(", "));
            ActionResponse<T> errorActionResponse = new ActionResponse<>(HttpStatus.BAD_REQUEST, String.format("There were errors with the fields related to this action: %s", errorMessages));
            return Optional.of(errorActionResponse);
        }
        return Optional.empty();
    }

    private ActionResponse<T> createErrorResponse(Exception e) {
        return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("An internal issue occurred while trying to retrieve your data: %s", e.getMessage()));
    }

}
