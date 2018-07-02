/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.web.channel.actions;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.Descriptor;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class ChannelConfigActions<R extends ConfigRestModel, D extends Descriptor> {
    private final ObjectTransformer objectTransformer;

    public ChannelConfigActions(final ObjectTransformer objectTransformer) {
        this.objectTransformer = objectTransformer;
    }

    public abstract boolean doesConfigExist(final String id, D descriptor);

    public abstract List<R> getConfig(final Long id, D descriptor) throws AlertException;

    public abstract void deleteConfig(final String id, D descriptor);

    public abstract DatabaseEntity saveConfig(final R restModel, D descriptor) throws AlertException;

    public abstract String validateConfig(final R restModel, D descriptor) throws AlertFieldException;

    public abstract String testConfig(final R restModel, final D descriptor) throws IntegrationException;

    public abstract DatabaseEntity saveNewConfigUpdateFromSavedConfig(final R restModel, D descriptor) throws AlertException;

    public Boolean isBoolean(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        final String trimmedValue = value.trim();
        return trimmedValue.equalsIgnoreCase("false") || trimmedValue.equalsIgnoreCase("true");
    }

    public ObjectTransformer getObjectTransformer() {
        return objectTransformer;
    }

}
