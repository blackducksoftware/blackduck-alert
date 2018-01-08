/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.mock;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.JsonArray;

public class NotificationTypeMockUtils {
    private final NotificationCategoryEnum type1;
    private final NotificationCategoryEnum type2;

    public NotificationTypeMockUtils() {
        this(NotificationCategoryEnum.POLICY_VIOLATION, NotificationCategoryEnum.VULNERABILITY);
    }

    public NotificationTypeMockUtils(final NotificationCategoryEnum type1, final NotificationCategoryEnum type2) {
        this.type1 = type1;
        this.type2 = type2;
    }

    public NotificationTypeEntity getType1Entity() {
        return new NotificationTypeEntity(type1);
    }

    public NotificationTypeEntity getType2Entity() {
        return new NotificationTypeEntity(type2);
    }

    public NotificationCategoryEnum getType1() {
        return type1;
    }

    public NotificationCategoryEnum getType2() {
        return type2;
    }

    public List<DistributionNotificationTypeRelation> getNotificationTypeRelations() {
        final DistributionNotificationTypeRelation relation1 = new DistributionNotificationTypeRelation(1L, 1L);
        final DistributionNotificationTypeRelation relation2 = new DistributionNotificationTypeRelation(1L, 2L);
        return Arrays.asList(relation1, relation2);
    }

    public List<NotificationCategoryEnum> createNotificiationTypeListing() {
        return Arrays.asList(type1, type2);
    }

    public List<String> createNotificiationTypeListingAsStrings() {
        return Arrays.asList(type1.name(), type2.name());
    }

    public JsonArray getNotificationListingJson() {
        final JsonArray json = new JsonArray();
        json.add(type1.name());
        json.add(type2.name());
        return json;
    }
}
