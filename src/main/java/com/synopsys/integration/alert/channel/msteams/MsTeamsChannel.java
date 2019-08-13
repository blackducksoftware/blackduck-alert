package com.synopsys.integration.alert.channel.msteams;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Component(value = MsTeamsChannel.COMPONENT_NAME)
public class MsTeamsChannel extends NamedDistributionChannel {
    public static final String COMPONENT_NAME = "channel_msteams";

    private RestChannelUtility restChannelUtility;
    private MsTeamsEventParser msTeamsEventParser;

    @Autowired
    public MsTeamsChannel(final Gson gson, final AuditUtility auditUtility, RestChannelUtility restChannelUtility, MsTeamsEventParser msTeamsEventParser) {
        super(COMPONENT_NAME, gson, auditUtility);
        this.restChannelUtility = restChannelUtility;
        this.msTeamsEventParser = msTeamsEventParser;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        final FieldAccessor fields = event.getFieldAccessor();
        final String webhook = fields.getString(MsTeamsDescriptor.KEY_WEBHOOK).orElseThrow(() -> new AlertException("Missing Webhook URL"));

        MsTeamsMessage msTeamsMessage = msTeamsEventParser.createMessage(event);
        String json = msTeamsEventParser.toJson(msTeamsMessage);

        Request request = restChannelUtility.createPostMessageRequest(webhook, new HashMap<>(), json);

        restChannelUtility.sendSingleMessage(request, event.getDestination());
    }

}
