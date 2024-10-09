package com.blackduck.integration.alert.common.rest.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;

public interface ValidateController<T> {
    @PostMapping("/validate")
    ValidationResponseModel validate(@RequestBody T requestBody);
}
