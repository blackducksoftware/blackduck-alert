package com.synopsys.integration.alert.database.deprecated.channel.hipchat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.deprecated.channel.DistributionChannelConfigEntity;

@Entity
@Table(schema = "alert", name = "hip_chat_distribution_config")
public class HipChatDistributionConfigEntity extends DistributionChannelConfigEntity {
    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "notify")
    private Boolean notify;

    @Column(name = "color")
    private String color;

    public HipChatDistributionConfigEntity() {
        // JPA requires default constructor definitions
    }

    public HipChatDistributionConfigEntity(final Integer roomId, final Boolean notify, final String color) {
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Boolean getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }

}