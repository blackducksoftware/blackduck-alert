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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisDescriptor;

@Component
public class PolarisProperties extends ProviderProperties {
    public static final Integer DEFAULT_TIMEOUT = 300;

    private final AlertProperties alertProperties;

    @Autowired
    public PolarisProperties(final AlertProperties alertProperties, final BaseConfigurationAccessor configurationAccessor) {
        super(PolarisProvider.COMPONENT_NAME, configurationAccessor);
        this.alertProperties = alertProperties;
    }

    public Optional<String> getUrl() {
        return createFieldAccessor()
                   .getString(PolarisDescriptor.KEY_POLARIS_URL)
                   .filter(StringUtils::isNotBlank);
    }

    public Integer getTimeout() {
        return createFieldAccessor()
                   .getInteger(PolarisDescriptor.KEY_POLARIS_TIMEOUT)
                   .orElse(DEFAULT_TIMEOUT);
    }

}
