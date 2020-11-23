package com.synopsys.integration.alert.channel.msteams;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.test.common.FieldModelUtils;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class MsTeamsChannelTest extends AbstractChannelTest {
    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        MsTeamsEventParser msTeamsEventParser = new MsTeamsEventParser(freemarkerTemplatingService);
        MsTeamsKey msTeamsKey = new MsTeamsKey();
        MsTeamsMessageParser msTeamsMessageParser = new MsTeamsMessageParser(new MarkupEncoderUtil());
        MsTeamsChannel msTeamsChannel = new MsTeamsChannel(msTeamsKey, gson, createAuditAccessor(), createRestChannelUtility(), msTeamsEventParser, msTeamsMessageParser);
        ProviderMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        FieldModelUtils.addConfigurationFieldToMap(fieldModels, MsTeamsDescriptor.KEY_WEBHOOK, properties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK));

        FieldUtility fieldUtility = new FieldUtility(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", msTeamsKey.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldUtility);

        msTeamsChannel.sendAuditedMessage(event);

        boolean actual = outputLogger.isLineContainingText("Successfully sent a " + msTeamsKey.getUniversalKey() + " message!");
        assertTrue(actual, "No success message appeared in the logs");
    }

}
