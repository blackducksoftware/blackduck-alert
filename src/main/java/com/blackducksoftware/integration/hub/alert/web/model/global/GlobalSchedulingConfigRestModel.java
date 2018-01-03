/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.model.global;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class GlobalSchedulingConfigRestModel extends ConfigRestModel {
    private static final long serialVersionUID = -9134645093354671571L;

    private String accumulatorCron;
    private String dailyDigestCron;
    private String purgeDataCron;

    public GlobalSchedulingConfigRestModel() {

    }

    public GlobalSchedulingConfigRestModel(final String id, final String accumulatorCron, final String dailyDigestCron, final String purgeDataCron) {
        super(id);
        this.accumulatorCron = accumulatorCron;
        this.dailyDigestCron = dailyDigestCron;
        this.purgeDataCron = purgeDataCron;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getAccumulatorCron() {
        return accumulatorCron;
    }

    public String getDailyDigestCron() {
        return dailyDigestCron;
    }

    public String getPurgeDataCron() {
        return purgeDataCron;
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        return reflectionToStringBuilder.build();
    }

}
