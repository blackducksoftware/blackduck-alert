package com.blackduck.integration.alert.common.rest.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;

public interface TestController<T> {
    @PostMapping("/test")
    ValidationResponseModel test(@RequestBody T resource);
}
