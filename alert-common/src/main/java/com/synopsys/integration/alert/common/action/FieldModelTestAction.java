/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action;

import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Deprecated(forRemoval = true)
public abstract class FieldModelTestAction {
    public static final String KEY_CUSTOM_TOPIC = "channel.common.custom.message.topic";
    public static final String KEY_CUSTOM_MESSAGE = "channel.common.custom.message.content";
    public static final String KEY_DESTINATION_NAME = "test.field.destination.name";

    public abstract MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException;

}
