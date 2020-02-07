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
package com.synopsys.integration.alert.common.provider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

public abstract class ProviderProperties {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String providerName;
    protected final ConfigurationAccessor configurationAccessor;

    public ProviderProperties(final String providerName, final ConfigurationAccessor configurationAccessor) {
        this.providerName = providerName;
        this.configurationAccessor = configurationAccessor;
    }

    // This assumes that there will only ever be one global config for a provider. This may not be the case in the future.
    public Optional<ConfigurationModel> retrieveGlobalConfig() {
        try {
            final List<ConfigurationModel> configurations = configurationAccessor.getConfigurationByDescriptorNameAndContext(providerName, ConfigContextEnum.GLOBAL);
            if (null != configurations && !configurations.isEmpty()) {
                return Optional.of(configurations.get(0));
            }
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Problem connecting to DB.", e);
        }
        return Optional.empty();
    }

    protected FieldAccessor createFieldAccessor() {
        return retrieveGlobalConfig()
                   .map(config -> new FieldAccessor(config.getCopyOfKeyToFieldMap()))
                   .orElse(new FieldAccessor(Map.of()));
    }

    protected Optional<String> createOptionalString(final String value) {
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }
}
