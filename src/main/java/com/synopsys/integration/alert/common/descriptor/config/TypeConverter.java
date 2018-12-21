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
package com.synopsys.integration.alert.common.descriptor.config;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;

public abstract class TypeConverter {
    private final ContentConverter contentConverter;

    public TypeConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    public abstract Config getConfigFromJson(final String json);

    public abstract DatabaseEntity populateEntityFromConfig(Config config);

    public abstract Config populateConfigFromEntity(DatabaseEntity entity);

    public ContentConverter getContentConverter() {
        return contentConverter;
    }

    public void addIdToEntityPK(final String id, final DatabaseEntity entity) {
        final Long longId = contentConverter.getLongValue(id);
        entity.setId(longId);
    }

}
