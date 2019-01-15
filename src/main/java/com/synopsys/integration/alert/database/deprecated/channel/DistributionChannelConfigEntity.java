package com.synopsys.integration.alert.database.deprecated.channel;

import javax.persistence.MappedSuperclass;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@MappedSuperclass
public abstract class DistributionChannelConfigEntity extends DatabaseEntity {
    public DistributionChannelConfigEntity() {
        // JPA requires default constructor definitions
    }

}