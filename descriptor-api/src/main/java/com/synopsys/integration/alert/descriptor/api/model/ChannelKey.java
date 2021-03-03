/*
 * descriptor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

public class ChannelKey extends DescriptorKey {
    public static final AzureBoardsChannelKey AZURE_BOARDS = new AzureBoardsChannelKey();
    public static final EmailChannelKey EMAIL = new EmailChannelKey();
    public static final JiraCloudChannelKey JIRA_CLOUD = new JiraCloudChannelKey();
    public static final JiraServerChannelKey JIRA_SERVER = new JiraServerChannelKey();
    public static final MsTeamsKey MS_TEAMS = new MsTeamsKey();
    public static final SlackChannelKey SLACK = new SlackChannelKey();

    private static final Map<String, ChannelKey> channels = new HashMap<>();
    private static void putChannel(ChannelKey channelKey) {
        channels.put(channelKey.getUniversalKey(), channelKey);
    }

    public static ChannelKey getChannelKey(String universalKey) {
        return channels.get(universalKey);
    }

    static {
        putChannel(AZURE_BOARDS);
        putChannel(EMAIL);
        putChannel(JIRA_CLOUD);
        putChannel(JIRA_SERVER);
        putChannel(MS_TEAMS);
        putChannel(SLACK);
    }

    public ChannelKey(String universalKey, String displayName) {
        super(universalKey, displayName);
    }

}
