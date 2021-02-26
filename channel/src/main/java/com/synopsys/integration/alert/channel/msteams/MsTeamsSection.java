/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams;

import java.util.List;

public class MsTeamsSection {
    private String subTopic;
    private List<String> componentsMessage;

    public String getSubTopic() {
        return subTopic;
    }

    public void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
    }

    public List<String> getComponentsMessage() {
        return componentsMessage;
    }

    public void setComponentsMessage(List<String> componentsMessage) {
        this.componentsMessage = componentsMessage;
    }

}
