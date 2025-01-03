/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.mock;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MockAuditJobStatusModel {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final UUID jobId = UUID.randomUUID();
    private final String timeAuditCreated = new Date(400).toString();
    private final String timeLastSent = new Date(500).toString();
    private final String status = AuditEntryStatus.SUCCESS.name();

    public AuditJobStatusModel createRestModel() {
        return new AuditJobStatusModel(jobId, timeAuditCreated, timeLastSent, status);
    }

    public String getRestModelJson() {
        JsonObject json = new JsonObject();
        json.addProperty("jobId", jobId.toString());
        json.addProperty("timeAuditCreated", timeAuditCreated);
        json.addProperty("timeLastSent", timeLastSent);
        json.addProperty("status", status);

        return json.toString();
    }

    public void verifyRestModel() throws JSONException {
        String restModel = gson.toJson(createRestModel());
        String json = getRestModelJson();
        JSONAssert.assertEquals(restModel, json, false);
    }

    @Test
    public void testConfiguration() throws JSONException {
        verifyRestModel();
    }

}
