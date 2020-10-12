package com.synopsys.integration.alert.component.audit.mock;

import java.util.Date;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;

public class MockAuditJobStatusModel {
    private final Gson gson = new Gson();
    private final String timeAuditCreated = new Date(400).toString();
    private final String timeLastSent = new Date(500).toString();
    private final String status = AuditEntryStatus.SUCCESS.name();

    public AuditJobStatusModel createRestModel() {
        return new AuditJobStatusModel(timeAuditCreated, timeLastSent, status);
    }

    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("timeAuditCreated", timeAuditCreated);
        json.addProperty("timeLastSent", timeLastSent);
        json.addProperty("status", status);

        return json.toString();
    }

    public void verifyRestModel() throws JSONException {
        final String restModel = gson.toJson(createRestModel());
        final String json = getRestModelJson();
        JSONAssert.assertEquals(restModel, json, false);
    }

    @Test
    public void testConfiguration() throws JSONException {
        verifyRestModel();
    }

}
