package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;

import com.blackduck.integration.util.Stringable;

public class HrefNode extends Stringable implements Serializable {
    private final String href;

    public HrefNode(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

}
