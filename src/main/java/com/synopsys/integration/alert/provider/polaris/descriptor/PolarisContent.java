/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;

//@Component
public class PolarisContent extends ProviderContent {
    public static final String LABEL_PROJECT_NAME = "Project";
    public static final String LABEL_BRANCHES = "Branches";
    public static final String LABEL_ISSUE_TYPE = "Issue Type";
    public static final String LABEL_NUMBER_OF_ISSUES_UPDATED = "Issues Updated";
    public static final String LABEL_NEW_ISSUE_TOTAL = "New Total";

    public static final String JSON_FIELD_PROJECT_NAME = "projectName";
    public static final String JSON_FIELD_PROJECT_LINK = "projectLink";
    public static final String JSON_FIELD_DESCRIPTION = "description";

    public static final String JSON_FIELD_ISSUE_TYPE = "issueType";
    public static final String JSON_FIELD_CHANGED_COUNT = "numberChanged";
    public static final String JSON_FIELD_NEW_TOTAL = "newTotal";

    private static final JsonField<String> PROJECT_NAME_FIELD = JsonField.createStringField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT, JSON_FIELD_PROJECT_NAME), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC,
        LABEL_PROJECT_NAME, List.of(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN));
    private static final JsonField<String> PROJECT_LINK_FIELD = JsonField.createStringField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT, JSON_FIELD_PROJECT_LINK), JSON_FIELD_PROJECT_LINK, FieldContentIdentifier.TOPIC_URL,
        LABEL_PROJECT_NAME + JsonField.LABEL_URL_SUFFIX);
    private static final JsonField<String> BRANCHES_FIELD = JsonField.createStringField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT, JSON_FIELD_DESCRIPTION), JSON_FIELD_DESCRIPTION, FieldContentIdentifier.SUB_TOPIC,
        LABEL_BRANCHES);
    private static final JsonField<String> ISSUE_TYPE_FIELD = JsonField.createStringField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT, JSON_FIELD_ISSUE_TYPE), JSON_FIELD_ISSUE_TYPE, FieldContentIdentifier.CATEGORY_ITEM,
        LABEL_ISSUE_TYPE);
    private static final JsonField<Integer> ISSUE_PREVIOUS_COUNT_FIELD = JsonField.createIntegerField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT, JSON_FIELD_CHANGED_COUNT), JSON_FIELD_CHANGED_COUNT,
        FieldContentIdentifier.CATEGORY_ITEM, LABEL_NUMBER_OF_ISSUES_UPDATED);
    private static final JsonField<Integer> ISSUE_NEW_COUNT_FIELD = JsonField.createIntegerField(JsonField.createJsonPath(JsonField.FORMAT_SINGLE_REPLACEMENT, JSON_FIELD_NEW_TOTAL), JSON_FIELD_NEW_TOTAL,
        FieldContentIdentifier.CATEGORY_ITEM, LABEL_NEW_ISSUE_TOTAL);

    private static final List<JsonField<?>> POLARIS_FIELDS = List.of(PROJECT_NAME_FIELD, PROJECT_LINK_FIELD, BRANCHES_FIELD, ISSUE_TYPE_FIELD, ISSUE_PREVIOUS_COUNT_FIELD, ISSUE_NEW_COUNT_FIELD);

    public static final ProviderContentType ISSUE_COUNT_INCREASED = new ProviderContentType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED.name(), POLARIS_FIELDS);
    public static final ProviderContentType ISSUE_COUNT_DECREASED = new ProviderContentType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_DECREASED.name(), POLARIS_FIELDS);

    public PolarisContent() {
        super(PolarisProvider.COMPONENT_NAME, Set.of(ISSUE_COUNT_INCREASED, ISSUE_COUNT_DECREASED), EnumSet.of(FormatType.DEFAULT, FormatType.SUMMARY));
    }

}
