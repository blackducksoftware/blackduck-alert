package com.blackduck.integration.alert.common.action;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;

import com.blackduck.integration.alert.common.rest.HttpServletContentWrapper;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.common.util.PagingParamValidationUtils;
import com.blackduck.integration.exception.IntegrationException;

import jakarta.servlet.http.HttpServletRequest;

public abstract class PagedCustomFunctionAction<T extends AlertPagedModel<?>> extends CustomFunctionAction<T> {
    protected PagedCustomFunctionAction(AuthorizationManager authorizationManager) {
        super(authorizationManager);
    }

    @Override
    public final ActionResponse<T> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) throws IntegrationException {
        HttpServletRequest httpRequest = servletContentWrapper.getHttpRequest();
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();

        int pageNumber = extractIntParam(parameterMap, "pageNumber", 0);
        int pageSize = extractIntParam(parameterMap, "pageSize", 10);
        Optional<ActionResponse<T>> pageRequestError = PagingParamValidationUtils.createErrorActionResponseIfInvalid(pageNumber, pageSize);
        if (pageRequestError.isPresent()) {
            return pageRequestError.get();
        }

        String searchTerm = extractFirstParam(parameterMap, "searchTerm").orElse("");
        return createPagedActionResponse(fieldModel, servletContentWrapper, pageNumber, pageSize, searchTerm);
    }

    protected abstract ActionResponse<T> createPagedActionResponse(
        FieldModel fieldModel,
        HttpServletContentWrapper servletContentWrapper,
        int pageNumber,
        int pageSize,
        String searchTerm
    ) throws IntegrationException;

    protected final Optional<String> extractFirstParam(Map<String, String[]> parameterMap, String paramName) {
        return Optional.ofNullable(parameterMap.get(paramName))
            .filter(paramValues -> paramValues.length > 0)
            .map(paramValues -> paramValues[0]);
    }

    protected int extractIntParam(Map<String, String[]> parameterMap, String paramName, int defaultValue) {
        return extractFirstParam(parameterMap, paramName)
            .filter(NumberUtils::isDigits)
            .map(NumberUtils::toInt)
            .orElse(defaultValue);
    }

}
