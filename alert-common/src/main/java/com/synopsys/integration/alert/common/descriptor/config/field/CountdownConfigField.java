/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class CountdownConfigField extends ConfigField {
    public Long countdown;

    public CountdownConfigField(final String key, final String label, final String description, final String panel, final Long countdown) {
        super(key, label, description, FieldType.COUNTDOWN.getFieldTypeName(), false, false, panel);
        this.countdown = countdown;
    }

    public static CountdownConfigField create(final String key, final String label, final String description, final Long countdown) {
        return new CountdownConfigField(key, label, description, "", countdown);
    }
}
