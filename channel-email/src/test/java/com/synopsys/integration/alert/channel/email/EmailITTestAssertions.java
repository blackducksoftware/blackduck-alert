package com.synopsys.integration.alert.channel.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public final class EmailITTestAssertions {
    private static final LinkableItem TEST_PROVIDER = new LinkableItem("Test Provider Label", "Test Provider Config Name");
    private static final ProviderDetails TEST_PROVIDER_DETAILS = new ProviderDetails(0L, TEST_PROVIDER);

    private static final LinkableItem TEST_DETAIL_1 = new LinkableItem("Detail 1 Label", "Test Detail Value");
    private static final LinkableItem TEST_DETAIL_2 = new LinkableItem("Detail 2 Label", "Test Detail Value (with URL)", "https://google.com");
    private static final List<LinkableItem> TEST_DETAILS = List.of(TEST_DETAIL_1, TEST_DETAIL_2);

    private static final String SIMPLE_MESSAGE_CLASS_NAME = SimpleMessage.class.getSimpleName();
    private static final String TEST_SUMMARY_VALUE = "Test summary field of " + SIMPLE_MESSAGE_CLASS_NAME;
    private static final String TEST_DESCRIPTION_VALUE = "Test description field of " + SIMPLE_MESSAGE_CLASS_NAME;
    private static final SimpleMessage TEST_SIMPLE_MESSAGE = SimpleMessage.original(TEST_PROVIDER_DETAILS, TEST_SUMMARY_VALUE, TEST_DESCRIPTION_VALUE, TEST_DETAILS);

    private static final ProviderMessageHolder TEST_MESSAGE_HOLDER = new ProviderMessageHolder(List.of(), List.of(TEST_SIMPLE_MESSAGE));

    public static <D extends DistributionJobDetailsModel> void assertSendSimpleMessageSuccess(DistributionChannel<D> channel, D distributionDetails) {
        MessageResult messageResult = null;
        try {
            messageResult = channel.distributeMessages(distributionDetails, TEST_MESSAGE_HOLDER, "jobName");
        } catch (AlertException e) {
            Assertions.fail("Failed to distribute simple channel message due to an exception", e);
        }

        assertFalse(messageResult.hasErrors(), "The message result had errors");
        assertFalse(messageResult.hasWarnings(), "The message result had warnings");
    }

    public static <D extends DistributionJobDetailsModel> void assertSendSimpleMessageException(DistributionChannel<D> channel, D distributionDetails) {
        assertSendSimpleMessageException(channel, distributionDetails, null);
    }

    public static <D extends DistributionJobDetailsModel> void assertSendSimpleMessageException(DistributionChannel<D> channel, D distributionDetails, @Nullable String expectedExceptionMessage) {
        try {
            channel.distributeMessages(distributionDetails, TEST_MESSAGE_HOLDER, "jobName");
            Assertions.fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            if (null != expectedExceptionMessage) {
                String exceptionMessage = e.getMessage();
                assertEquals(expectedExceptionMessage, exceptionMessage);
            }
        }
    }

    private EmailITTestAssertions() {}

}
