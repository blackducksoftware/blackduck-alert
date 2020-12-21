package com.synopsys.integration.alert.channel.msteams;

import java.util.Date;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.AbstractChannelTest;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class MsTeamsChannelTest extends AbstractChannelTest {
    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IntegrationException {
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        MsTeamsEventParser msTeamsEventParser = new MsTeamsEventParser(freemarkerTemplatingService);
        MsTeamsMessageParser msTeamsMessageParser = new MsTeamsMessageParser(new MarkupEncoderUtil());
        MsTeamsChannel msTeamsChannel = new MsTeamsChannel(gson, auditAccessor, createRestChannelUtility(), msTeamsEventParser, msTeamsMessageParser);
        ProviderMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(properties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK));
        DistributionJobModel testJobModel = DistributionJobModel.builder()
                                                .distributionJobDetails(msTeamsJobDetailsModel)
                                                .build();

        DistributionEvent event = new DistributionEvent(ChannelKeys.MS_TEAMS.getUniversalKey(), RestConstants.formatDate(new Date()), 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), testJobModel, null);

        msTeamsChannel.sendAuditedMessage(event);

        Mockito.verify(auditAccessor).setAuditEntrySuccess(Mockito.any());
    }

}
