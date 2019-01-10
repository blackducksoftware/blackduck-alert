/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.provider.polaris;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContentType;

@Component(PolarisProvider.COMPONENT_NAME)
public class PolarisProvider extends Provider {
    public static final String COMPONENT_NAME = "provider_polaris";

    public PolarisProvider() {
        super(PolarisProvider.COMPONENT_NAME);
    }

    @Override
    public void initialize() {
        // FIXME schedule Polaris tasks
    }

    @Override
    public void destroy() {
        // FIXME unschedule Polaris tasks
    }

    @Override
    public Set<ProviderContentType> getProviderContentTypes() {
        // FIXME create content types for this provider
        return Set.of();
    }

    @Override
    public Set<FormatType> getSupportedFormatTypes() {
        return EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST);
    }
}
