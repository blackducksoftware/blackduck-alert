/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams;

import java.util.List;

import com.synopsys.integration.alert.common.channel.template.FreemarkerDataModel;

public class MsTeamsMessage implements FreemarkerDataModel {
    private String title;
    private String topic;
    private List<MsTeamsSection> sections;

    public MsTeamsMessage(String title, String topic, List<MsTeamsSection> sections) {
        this.title = title;
        this.topic = topic;
        this.sections = sections;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<MsTeamsSection> getSections() {
        return sections;
    }

    public void setSections(List<MsTeamsSection> sections) {
        this.sections = sections;
    }
}
