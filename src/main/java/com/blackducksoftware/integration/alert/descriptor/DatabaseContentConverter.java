package com.blackducksoftware.integration.alert.descriptor;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

public abstract class DatabaseContentConverter {

    public abstract ConfigRestModel getRestModelFromJson(final String json);

    public abstract DatabaseEntity populateDatabaseEntityFromRestModel(ConfigRestModel restModel);

    public abstract ConfigRestModel populateRestModelFromDatabaseEntity(DatabaseEntity entity);

}
