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

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;
import com.google.gson.JsonObject;

public class GlobalSchedulingMockUtils implements MockUtils<CommonDistributionConfigRestModel, GlobalSchedulingConfigRestModel, DatabaseEntity, GlobalSchedulingConfigEntity> {
    private final String accumulatorCron;
    private final String dailyDigestCron;
    private final String purgeDataCron;
    private final String id;

    public GlobalSchedulingMockUtils() {
        this("1 1 1 1 1 1", "2 2 2 2 2 2", "3 3 3 3 3 3", "1");
    }

    public GlobalSchedulingMockUtils(final String accumulatorCron, final String dailyDigestCron, final String purgeDataCron, final String id) {
        super();
        this.accumulatorCron = accumulatorCron;
        this.dailyDigestCron = dailyDigestCron;
        this.purgeDataCron = purgeDataCron;
        this.id = id;
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
    public String getId() {
        return id;
    }

    @Override
    public GlobalSchedulingConfigRestModel createGlobalRestModel() {
        return new GlobalSchedulingConfigRestModel(id, accumulatorCron, dailyDigestCron, purgeDataCron);
    }

    @Override
    public GlobalSchedulingConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalSchedulingConfigRestModel();
    }

    @Override
    public GlobalSchedulingConfigEntity createGlobalEntity() {
        final GlobalSchedulingConfigEntity entity = new GlobalSchedulingConfigEntity(accumulatorCron, dailyDigestCron, purgeDataCron);
        entity.setId(Long.valueOf(id));
        return entity;
    }

    @Override
    public GlobalSchedulingConfigEntity createEmptyGlobalEntity() {
        return new GlobalSchedulingConfigEntity();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("accumulatorCron", accumulatorCron);
        json.addProperty("dailyDigestCron", dailyDigestCron);
        json.addProperty("purgeDataCron", purgeDataCron);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("accumulatorCron", null);
        json.add("dailyDigestCron", null);
        json.add("purgeDataCron", null);
        json.add("id", null);
        return json.toString();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("accumulatorCron", accumulatorCron);
        json.addProperty("dailyDigestCron", dailyDigestCron);
        json.addProperty("purgeDataCron", purgeDataCron);
        json.addProperty("id", Long.valueOf(id));
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("accumulatorCron", null);
        json.add("dailyDigestCron", null);
        json.add("purgeDataCron", null);
        json.add("id", null);
        return json.toString();
    }

    /*
     * Not global methods thus not included
     */

    @Override
    @Deprecated
    public String getEntityJson() {
        return null;
    }

    @Override
    @Deprecated
    public String getEmptyEntityJson() {
        return null;
    }

    @Override
    @Deprecated
    public CommonDistributionConfigRestModel createRestModel() {
        return null;
    }

    @Override
    @Deprecated
    public CommonDistributionConfigRestModel createEmptyRestModel() {
        return null;
    }

    @Override
    @Deprecated
    public DatabaseEntity createEntity() {
        return null;
    }

    @Override
    @Deprecated
    public DatabaseEntity createEmptyEntity() {
        return null;
    }

    @Override
    @Deprecated
    public String getRestModelJson() {
        return null;
    }

    @Override
    @Deprecated
    public String getEmptyRestModelJson() {
        return null;
    }

}
