/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.descriptor.model;

import java.util.HashMap;
import java.util.Map;

import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.descriptor.MsTeamsKey;
import com.blackduck.integration.alert.api.descriptor.SlackChannelKey;

public final class ChannelKeys {
    public static final AzureBoardsChannelKey AZURE_BOARDS = new AzureBoardsChannelKey();
    public static final EmailChannelKey EMAIL = new EmailChannelKey();
    public static final JiraCloudChannelKey JIRA_CLOUD = new JiraCloudChannelKey();
    public static final JiraServerChannelKey JIRA_SERVER = new JiraServerChannelKey();
    public static final MsTeamsKey MS_TEAMS = new MsTeamsKey();
    public static final SlackChannelKey SLACK = new SlackChannelKey();

    private static final Map<String, ChannelKey> KEYS = new HashMap<>();

    private ChannelKeys() {}

    public static ChannelKey getChannelKey(String universalKey) {
        return KEYS.get(universalKey);
    }

    private static void putKey(ChannelKey channelKey) {
        KEYS.put(channelKey.getUniversalKey(), channelKey);
    }

    static {
        ChannelKeys.putKey(ChannelKeys.AZURE_BOARDS);
        ChannelKeys.putKey(ChannelKeys.EMAIL);
        ChannelKeys.putKey(ChannelKeys.JIRA_CLOUD);
        ChannelKeys.putKey(ChannelKeys.JIRA_SERVER);
        ChannelKeys.putKey(ChannelKeys.MS_TEAMS);
        ChannelKeys.putKey(ChannelKeys.SLACK);
    }

}
