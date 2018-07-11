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
package com.blackducksoftware.integration.alert.mock;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.alert.mock.entity.MockEntityUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NotificationTypeMockUtils extends MockEntityUtil<NotificationTypeEntity> {
    private NotificationCategoryEnum type;
    private Long id;

    public NotificationTypeMockUtils() {
        this(1L, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    private NotificationTypeMockUtils(final Long id, final NotificationCategoryEnum type) {
        this.type = type;
        this.id = id;
    }

    public List<DistributionNotificationTypeRelation> getNotificationTypeRelations() {
        final DistributionNotificationTypeRelation relation1 = new DistributionNotificationTypeRelation(1L, id);
        return Arrays.asList(relation1);
    }

    public List<NotificationCategoryEnum> createNotificiationTypeListing() {
        return Arrays.asList(type);
    }

    public List<String> createNotificiationTypeListingAsStrings() {
        return Arrays.asList(type.name());
    }

    public JsonArray getNotificationListingJson() {
        final JsonArray json = new JsonArray();
        json.add(type.name());
        return json;
    }

    public NotificationCategoryEnum getType() {
        return type;
    }

    public void setType(final NotificationCategoryEnum type) {
        this.type = type;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public NotificationTypeEntity createEntity() {
        final NotificationTypeEntity entity = new NotificationTypeEntity(type);
        entity.setId(id);
        return entity;
    }

    @Override
    public NotificationTypeEntity createEmptyEntity() {
        return new NotificationTypeEntity();
    }

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", type.name());
        return json.toString();
    }
}
