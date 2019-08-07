package com.synopsys.integration.alert.channel.msteams;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component(value = MsTeamsChannel.COMPONENT_NAME)
public class MsTeamsChannel extends NamedDistributionChannel {
    public static final String COMPONENT_NAME = "channel_msteams";

    private static final String json = "{\n" +
            "    \"@type\": \"MessageCard\",\n" +
            "    \"@context\": \"https://schema.org/extensions\",\n" +
            "    \"summary\": \"Test from alert junit\",\n" +
            "    \"themeColor\": \"5A2A82\",\n" +
            "    \"title\": \"test from junit\",\n" +
            "    \"sections\": [\n" +
            "        {\n" +
            "            \"activityTitle\": \"gk_alert_test\",\n" +
            "            \"activitySubtitle\": \"1.0.0\",\n" +
            "            \"facts\": [\n" +
            " {\n" +
            " \"name\": \"Category:\",\n" +
            " \"value\": \"Policy\"\n" +
            " },\n" +
            "                {\n" +
            "                    \"name\": \"Operation:\",\n" +
            "                    \"value\": \"ADD\"\n" +
            "                },\n" +
            " {\n" +
            " \"name\": \"Component:\",\n" +
            " \"value\": \"Apache Commons File Upload\"\n" +
            " },\n" +
            " {\n" +
            " \"name\": \"Component Version:\",\n" +
            " \"value\": \"1.2.1\"\n" +
            " },\n" +
            " {\n" +
            " \"name\": \"New Vulnerabilities:\",\n" +
            " \"value\": \"[BDSA-2013-0013][BDSA-2016-1573]\"\n" +
            " }\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"potentialAction\": [\n" +
            "    ]\n" +
            "}";

    private RestChannelUtility restChannelUtility;

    @Autowired
    public MsTeamsChannel(final Gson gson, final AuditUtility auditUtility, RestChannelUtility restChannelUtility) {
        super(COMPONENT_NAME, gson, auditUtility);
        this.restChannelUtility = restChannelUtility;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        final FieldAccessor fields = event.getFieldAccessor();
        final String webhook = fields.getString(MsTeamsDescriptor.KEY_WEBHOOK).orElseThrow(() -> new AlertException("Missing Webhook URL"));
        List<Request> requests = List.of(restChannelUtility.createPostMessageRequest(webhook, new HashMap<>(), json));
        restChannelUtility.sendMessage(requests, event.getDestination());
    }

}
