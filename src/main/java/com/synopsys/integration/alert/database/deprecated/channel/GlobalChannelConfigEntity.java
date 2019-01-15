package com.synopsys.integration.alert.database.deprecated.channel;

import javax.persistence.MappedSuperclass;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@MappedSuperclass
public abstract class GlobalChannelConfigEntity extends DatabaseEntity {
    public GlobalChannelConfigEntity() {
        // JPA requires default constructor definitions
    }

}
