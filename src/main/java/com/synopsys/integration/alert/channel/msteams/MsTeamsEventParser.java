package com.synopsys.integration.alert.channel.msteams;

import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.exception.IntegrationException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Component
public class MsTeamsEventParser {
    private FreemarkerTemplatingService freemarkerTemplatingService;
    private Configuration freemarkerConfiguration;
    private Template msTeamsTemplate;

    public MsTeamsEventParser(FreemarkerTemplatingService freemarkerTemplatingService) throws IOException {
        this.freemarkerTemplatingService = freemarkerTemplatingService;
        this.freemarkerConfiguration = freemarkerTemplatingService.createFreemarkerConfig(new File("C:\\cygwin64\\home\\ekerwin\\source\\blackduck-alert\\src\\main\\resources\\channel\\msteams\\templates"));
        this.msTeamsTemplate = freemarkerConfiguration.getTemplate("message_content.ftl");
    }

    public MsTeamsMessage createMessage(DistributionEvent distributionEvent) {
        MessageContentGroup messageContentGroup = distributionEvent.getContent();

        MsTeamsMessage msTeamsMessage = new MsTeamsMessage();
        messageContentGroup
                .getSubContent()
                .stream()
                .forEach(content -> msTeamsMessage.addContent(content));

        return msTeamsMessage;
    }

    public String toJson(MsTeamsMessage msTeamsMessage) throws IntegrationException {
        try {
            StringWriter stringWriter = new StringWriter();
            msTeamsTemplate.process(msTeamsMessage, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
