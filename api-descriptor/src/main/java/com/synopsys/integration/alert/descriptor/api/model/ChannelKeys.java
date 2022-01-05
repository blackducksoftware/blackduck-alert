/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.descriptor.api.model;

import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;

public final class ChannelKeys {
    public static final AzureBoardsChannelKey AZURE_BOARDS = new AzureBoardsChannelKey();
    public static final EmailChannelKey EMAIL = new EmailChannelKey();
    public static final JiraCloudChannelKey JIRA_CLOUD = new JiraCloudChannelKey();
    public static final JiraServerChannelKey JIRA_SERVER = new JiraServerChannelKey();
    public static final MsTeamsKey MS_TEAMS = new MsTeamsKey();
    public static final SlackChannelKey SLACK = new SlackChannelKey();

    private static final Map<String, ChannelKey> KEYS = new HashMap<>();

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
