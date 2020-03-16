package com.synopsys.integration.alert.channel.msteams;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class MsTeamsChannelTest extends ChannelTest {
    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(new TestAlertProperties());
        MsTeamsEventParser msTeamsEventParser = new MsTeamsEventParser(freemarkerTemplatingService);
        MsTeamsKey msTeamsKey = new MsTeamsKey();
        MsTeamsMessageParser msTeamsMessageParser = new MsTeamsMessageParser(new MarkupEncoderUtil());
        MsTeamsChannel msTeamsChannel = new MsTeamsChannel(msTeamsKey, gson, createAuditUtility(), createRestChannelUtility(), msTeamsEventParser, msTeamsMessageParser);
        ProviderMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, MsTeamsDescriptor.KEY_WEBHOOK, properties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK));

        FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        DistributionEvent event = new DistributionEvent(
            "1L", msTeamsKey.getUniversalKey(), RestConstants.formatDate(new Date()), new BlackDuckProviderKey().getUniversalKey(), ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        msTeamsChannel.sendAuditedMessage(event);

        boolean actual = outputLogger.isLineContainingText("Successfully sent a " + msTeamsKey.getUniversalKey() + " message!");
        assertTrue(actual, "No success message appeared in the logs");
    }

}
