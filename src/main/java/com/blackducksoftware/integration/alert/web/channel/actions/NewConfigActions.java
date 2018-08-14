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
package com.blackducksoftware.integration.alert.web.channel.actions;

import java.util.List;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class NewConfigActions {
    private final ContentConverter contentConverter;

    public NewConfigActions(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    public boolean doesConfigExist(final String id, final DescriptorConfig descriptor) {
        return doesConfigExist(contentConverter.getLongValue(id), descriptor);
    }

    public abstract boolean doesConfigExist(final Long id, DescriptorConfig descriptor);

    public abstract List<? extends Config> getConfig(final Long id, DescriptorConfig descriptor) throws AlertException;

    public void deleteConfig(final String id, final DescriptorConfig descriptor) {
        deleteConfig(contentConverter.getLongValue(id), descriptor);
    }

    public abstract void deleteConfig(final Long id, DescriptorConfig descriptor);

    public abstract DatabaseEntity saveConfig(final Config restModel, DescriptorConfig descriptor) throws AlertException;

    public abstract String validateConfig(final Config restModel, DescriptorConfig descriptor) throws AlertFieldException;

    public abstract String testConfig(final Config restModel, final DescriptorConfig descriptor) throws IntegrationException;

    public abstract DatabaseEntity saveNewConfigUpdateFromSavedConfig(final Config restModel, DescriptorConfig descriptor) throws AlertException;

    public ContentConverter getContentConverter() {
        return contentConverter;
    }

}
