package com.synopsys.integration.alert.common.descriptor.config;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;

public abstract class TypeConverter {
    private final ContentConverter contentConverter;

    public TypeConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    public abstract Config getConfigFromJson(final String json);

    public abstract DatabaseEntity populateEntityFromConfig(Config config);

    public abstract Config populateConfigFromEntity(DatabaseEntity entity);

    public ContentConverter getContentConverter() {
        return contentConverter;
    }

    public void addIdToEntityPK(final String id, final DatabaseEntity entity) {
        final Long longId = contentConverter.getLongValue(id);
        entity.setId(longId);
    }

}
