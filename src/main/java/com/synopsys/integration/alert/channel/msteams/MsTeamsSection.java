package com.synopsys.integration.alert.channel.msteams;

import java.util.List;

public class MsTeamsSection {
    private String provider;
    private String topic;
    private String subTopic;
    private List<MsTeamsComponent> components;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSubTopic() {
        return subTopic;
    }

    public void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
    }

    public List<MsTeamsComponent> getComponents() {
        return components;
    }

    public void setComponents(List<MsTeamsComponent> components) {
        this.components = components;
    }

}
