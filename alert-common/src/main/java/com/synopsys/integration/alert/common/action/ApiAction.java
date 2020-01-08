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
package com.synopsys.integration.alert.common.action;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public abstract class ApiAction {

    public FieldModel beforeSaveAction(final FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    public FieldModel afterSaveAction(final FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    public FieldModel beforeUpdateAction(final FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    public FieldModel afterUpdateAction(final FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    public FieldModel beforeDeleteAction(final FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    public void afterDeleteAction(final String descriptorName, final String context) throws AlertException {
    }

    public FieldModel afterGetAction(final FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

}
