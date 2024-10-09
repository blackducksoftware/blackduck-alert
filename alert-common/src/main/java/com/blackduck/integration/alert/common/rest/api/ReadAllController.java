package com.blackduck.integration.alert.common.rest.api;

import org.springframework.web.bind.annotation.GetMapping;

public interface ReadAllController<T> {
    @GetMapping
    T getAll();
}
