/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Deprecated
public abstract class AbstractFunctionController<T> extends BaseController {
    public static final String API_FUNCTION_URL = "/api/function";
    private final CustomFunctionAction<T> functionAction;

    public AbstractFunctionController(CustomFunctionAction<T> functionAction) {
        this.functionAction = functionAction;
    }

    @PostMapping
    public T postConfig(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @RequestBody FieldModel restModel) {
        HttpServletContentWrapper servletContentWrapper = new HttpServletContentWrapper(httpRequest, httpResponse);
        ActionResponse<T> result = functionAction.createResponse(restModel, servletContentWrapper);
        return ResponseFactory.createContentResponseFromAction(result);
    }

}
