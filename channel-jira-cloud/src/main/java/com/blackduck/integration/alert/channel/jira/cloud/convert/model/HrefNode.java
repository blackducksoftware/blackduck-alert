package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;

public class HrefNode implements Serializable {
    private final String href;

    public HrefNode(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}
