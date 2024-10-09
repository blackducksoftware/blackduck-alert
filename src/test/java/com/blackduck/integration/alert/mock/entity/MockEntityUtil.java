package com.blackduck.integration.alert.mock.entity;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackduck.integration.alert.database.DatabaseEntity;
import com.blackduck.integration.alert.mock.MockUtils;

public abstract class MockEntityUtil<E extends DatabaseEntity> implements MockUtils {
    public abstract E createEntity();

    public abstract E createEmptyEntity();

    public abstract String getEntityJson();

    public String getEmptyEntityJson() {
        return "{}";
    }

    public void verifyEmptyEntity() throws JSONException {
        String emptyEntity = createEmptyEntity().toString();
        String emptyJson = getEmptyEntityJson();
        JSONAssert.assertEquals(emptyEntity, emptyJson, false);
    }

    public void verifyEntity() throws JSONException {
        String entity = createEntity().toString();
        String json = getEntityJson();
        JSONAssert.assertEquals(entity, json, false);
    }

    // @Test
    @Override
    public void testConfiguration() throws JSONException {
        verifyEntity();
        verifyEmptyEntity();
    }

}
