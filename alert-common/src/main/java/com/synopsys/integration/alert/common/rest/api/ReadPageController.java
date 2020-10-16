/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.rest.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ReadPageController<P extends AlertPagedModel<?>> {
    @GetMapping
    P getPage(
        @RequestParam(defaultValue = AlertPagedModel.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @RequestParam(defaultValue = AlertPagedModel.DEFAULT_PAGE_SIZE) Integer pageSize,
        // TODO determine if this actually works as a catch-all for all other query params
        @RequestParam(defaultValue = "{}") Map<String, String> queryParams
    );

}
