package com.blackduck.integration.alert.database.settings;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.database.DatabaseRelation;

public abstract class RelationTest<R extends DatabaseRelation> implements BaseEntityTest<R> {
    @Override
    @Test
    public void testEmptyEntity() throws JSONException {
        final R configEntity = createMockEmptyRelation();
        final R configEntityNew = createMockEmptyRelation();
        assertEquals(configEntity, configEntityNew);
    }

    @Override
    @Test
    public void testEntity() throws JSONException {
        final Long firstId = 13L;
        final Long secondId = 17L;

        final R configEntity = createMockRelation(firstId, secondId);
        final R configEntityNew = createMockRelation(firstId, secondId);
        assertEquals(configEntity, configEntityNew);
    }

    public abstract R createMockRelation(final Long firstId, final Long secondId);

    public abstract R createMockEmptyRelation();

}
