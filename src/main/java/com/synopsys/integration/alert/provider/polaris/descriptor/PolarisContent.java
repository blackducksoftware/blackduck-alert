/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.provider.polaris.PolarisProviderKey;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;

//@Component
// FIXME should this be removed?
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

    public static final ProviderContentType ISSUE_COUNT_INCREASED = new ProviderContentType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED.name());
    public static final ProviderContentType ISSUE_COUNT_DECREASED = new ProviderContentType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_DECREASED.name());

    @Autowired
    public PolarisContent(PolarisProviderKey polarisProviderKey) {
        super(polarisProviderKey, Set.of(ISSUE_COUNT_INCREASED, ISSUE_COUNT_DECREASED), EnumSet.of(FormatType.DEFAULT, FormatType.SUMMARY));
    }

}
