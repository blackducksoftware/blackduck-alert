/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface StaticUniqueConfigResourceController<T> {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    T create(@RequestBody T resource);

    @GetMapping
    T getOne();

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void update(@RequestBody T resource);

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete();

}
