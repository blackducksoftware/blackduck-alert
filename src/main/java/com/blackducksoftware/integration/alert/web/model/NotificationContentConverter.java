package com.blackducksoftware.integration.alert.web.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;

@Component
public class NotificationContentConverter extends DatabaseContentConverter {

    @Autowired
    public NotificationContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

}
