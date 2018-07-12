package com.blackducksoftware.integration.alert.descriptor;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.google.gson.Gson;

public class TransformerDescriptor {
    private final Gson gson;
    private final ObjectTransformer objectTransformer;
    private final Class<? extends DatabaseEntity> entityClass;
    private final Class<? extends ConfigRestModel> restModelClass;

    public TransformerDescriptor(final Gson gson, final ObjectTransformer objectTransformer, final Class<? extends DatabaseEntity> entityClass, final Class<? extends ConfigRestModel> restModelClass) {
        this.gson = gson;
        this.objectTransformer = objectTransformer;
        this.entityClass = entityClass;
        this.restModelClass = restModelClass;
    }

    public ConfigRestModel convertFromStringToRestModel(final String json) {
        return gson.fromJson(json, restModelClass);
    }

    public DatabaseEntity convertFromRestModelToConfigEntity(final ConfigRestModel restModel) throws AlertException {
        return objectTransformer.configRestModelToDatabaseEntity(restModel, entityClass);
    }

    public ConfigRestModel convertFromConfigEntityToRestModel(final DatabaseEntity entity) throws AlertException {
        return objectTransformer.databaseEntityToConfigRestModel(entity, restModelClass);
    }
}
