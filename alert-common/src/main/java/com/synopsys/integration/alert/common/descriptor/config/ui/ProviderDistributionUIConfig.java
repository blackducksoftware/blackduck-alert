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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;

public abstract class ProviderDistributionUIConfig extends UIConfig {
    public static final String KEY_NOTIFICATION_TYPES = "provider.distribution.notification.types";
    public static final String KEY_FORMAT_TYPE = "provider.distribution.format.type";

    private static final String LABEL_NOTIFICATION_TYPES = "Notification Types";
    private static final String LABEL_FORMAT = "Format";

    private static final String DESCRIPTION_NOTIFICATION_TYPES = "Select one or more of the notification types. Only these notification types will be included for this distribution job.";
    private static final String DESCRIPTION_FORMAT = "Select the format of the message that will be created.";

    private final ProviderContent providerContent;

    public ProviderDistributionUIConfig(final String label, final String urlName, final String fontAwesomeIcon, final ProviderContent providerContent) {
        super(label, label + " provider distribution setup.", urlName, fontAwesomeIcon);
        this.providerContent = providerContent;
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField notificationTypesField = SelectConfigField.createRequired(KEY_NOTIFICATION_TYPES, LABEL_NOTIFICATION_TYPES, DESCRIPTION_NOTIFICATION_TYPES, false, true,
            providerContent.getContentTypes()
                .stream()
                .map(ProviderContentType::getNotificationType)
                .map(LabelValueSelectOption::new)
                .sorted()
                .collect(Collectors.toList()));
        final ConfigField formatField = SelectConfigField.createRequired(KEY_FORMAT_TYPE, LABEL_FORMAT, DESCRIPTION_FORMAT,
            providerContent.getSupportedContentFormats()
                .stream()
                .map(FormatType::name)
                .map(LabelValueSelectOption::new)
                .sorted()
                .collect(Collectors.toList()));

        final List<ConfigField> configFields = List.of(notificationTypesField, formatField);
        final List<ConfigField> providerDistributionFields = createProviderDistributionFields();
        return Stream.concat(configFields.stream(), providerDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createProviderDistributionFields();

}
