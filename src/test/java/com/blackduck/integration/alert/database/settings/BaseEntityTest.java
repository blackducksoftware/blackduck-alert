package com.blackduck.integration.alert.database.settings;

import org.json.JSONException;

public interface BaseEntityTest<B> {
    void testEmptyEntity() throws JSONException;

    void testEntity() throws JSONException;

    Class<B> getEntityClass();

    void assertEntityFieldsNull(B entity);

    void assertEntityFieldsFull(B entity);

}
