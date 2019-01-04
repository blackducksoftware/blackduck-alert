package com.synopsys.integration.alert.audit.mock;

import java.util.Date;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.web.audit.JobAuditModel;

public class MockJobAuditModel {
    private final Gson gson = new Gson();
    private final String timeAuditCreated = new Date(400).toString();
    private final String timeLastSent = new Date(500).toString();
    private final String status = AuditEntryStatus.SUCCESS.name();

    public JobAuditModel createRestModel() {
        return new JobAuditModel(timeAuditCreated, timeLastSent, status);
    }

    public JobAuditModel createEmptyRestModel() {
        return new JobAuditModel();
    }

    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("timeAuditCreated", timeAuditCreated);
        json.addProperty("timeLastSent", timeLastSent);
        json.addProperty("status", status);

        return json.toString();
    }

    public String getEmptyRestModelJson() {
        return "{}";
    }

    public void verifyEmptyRestModel() throws JSONException {
        final String emptyRestModel = createEmptyRestModel().toString();
        final String json = getEmptyRestModelJson();
        JSONAssert.assertEquals(emptyRestModel, json, false);
    }

    public void verifyRestModel() throws JSONException {
        final String restModel = gson.toJson(createRestModel());
        final String json = getRestModelJson();
        JSONAssert.assertEquals(restModel, json, false);
    }

    @Test
    public void testConfiguration() throws JSONException {
        verifyEmptyRestModel();
        verifyRestModel();
    }

}
