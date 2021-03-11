/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.channel.NamedDistributionChannel;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component
public class MsTeamsChannel extends NamedDistributionChannel {
    private final RestChannelUtility restChannelUtility;
    private final MsTeamsEventParser msTeamsEventParser;
    private final MsTeamsMessageParser msTeamsMessageParser;

    @Autowired
    public MsTeamsChannel(Gson gson, AuditAccessor auditAccessor, RestChannelUtility restChannelUtility, MsTeamsEventParser msTeamsEventParser,
        MsTeamsMessageParser msTeamsMessageParser) {
        super(ChannelKeys.MS_TEAMS, gson, auditAccessor);
        this.restChannelUtility = restChannelUtility;
        this.msTeamsEventParser = msTeamsEventParser;
        this.msTeamsMessageParser = msTeamsMessageParser;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        DistributionJobModel distributionJobModel = event.getDistributionJobModel();
        DistributionJobDetailsModel distributionJobDetails = distributionJobModel.getDistributionJobDetails();
        MSTeamsJobDetailsModel asMSTeamsJobDetails = distributionJobDetails.getAs(DistributionJobDetailsModel.MS_TEAMS);
        String webhook = Optional.ofNullable(asMSTeamsJobDetails.getWebhook())
                             .filter(StringUtils::isNotBlank)
                             .orElseThrow(() -> AlertFieldException.singleFieldError(MsTeamsDescriptor.KEY_WEBHOOK, "MS Teams missing the required webhook field - the distribution configuration is likely invalid."));

        MsTeamsMessage msTeamsMessage = msTeamsMessageParser.createMsTeamsMessage(event.getContent());
        List<Request> teamsRequests = new LinkedList<>();
        for (MsTeamsMessage message : msTeamsEventParser.splitMessages(msTeamsMessage)) {
            String json = msTeamsEventParser.toJson(message);
            Request request = restChannelUtility.createPostMessageRequest(webhook, new HashMap<>(), json);
            teamsRequests.add(request);
        }

        restChannelUtility.sendMessage(teamsRequests, event.getDestination());
    }

}
