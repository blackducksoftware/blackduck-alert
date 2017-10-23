package com.blackducksoftware.integration.hub.alert.channel.email;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.AlertProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

public class EmailChannelTest {

    @Test
    public void sendEmailTest() throws Exception {
        final List<VulnerabilityEntity> vulns = new ArrayList<>();
        final VulnerabilityEntity vulnerability = new VulnerabilityEntity("Vuln ID", "Vuln Operation");
        vulns.add(vulnerability);
        final NotificationEntity notification = new NotificationEntity("EventKey", new Date(), NotificationCategoryEnum.POLICY_VIOLATION.toString(), "Manual Test Project", "Manual Test Project Version", "Manual Test Component", "1.0.3",
                "Manual Policy Rule", vulns);

        final Properties testProperties = new Properties();
        testProperties.load(new FileInputStream(new File("./src/test/resources/application.properties")));

        final AlertProperties alertProperties = new AlertProperties();
        alertProperties.setHubUrl(testProperties.getProperty("blackduck.hub.url"));
        final Gson gson = new Gson();
        final EmailChannel emailChannel = new EmailChannel(alertProperties, gson, null);
        final EmailEvent event = new EmailEvent(notification);

        final EmailConfigEntity emailConfigEntity = new EmailConfigEntity(null, testProperties.getProperty("mail.smtp.host"), null, null, null, null, null, testProperties.getProperty("mail.smtp.from"), null, null, null, null, null, null,
                null, testProperties.getProperty("hub.email.template.directory"), testProperties.getProperty("logo.image"));

        emailChannel.sendEmail(event, emailConfigEntity, testProperties.getProperty("email.to.address"));
    }
}
