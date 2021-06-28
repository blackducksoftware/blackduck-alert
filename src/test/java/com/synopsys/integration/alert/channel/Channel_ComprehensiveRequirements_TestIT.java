package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.channel.DistributionEventReceiver;
import com.synopsys.integration.alert.api.channel.issue.action.IssueTrackerTestAction;
import com.synopsys.integration.alert.common.channel.DistributionChannelTestAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

/*
 * This class uses underscores to indicate that it does not reference one particular class and instead tests an Alert concept.
 */
@AlertIntegrationTest
public class Channel_ComprehensiveRequirements_TestIT {
    private static final String COMPONENT_NAME_DISTRIBUTION_RECEIVER = DistributionEventReceiver.class.getSimpleName();
    private static final String COMPONENT_NAME_DISTRIBUTION_TEST_ACTION = DistributionChannelTestAction.class.getSimpleName();
    private static final String COMPONENT_NAME_ISSUE_TRACKER_TEST_ACTION = IssueTrackerTestAction.class.getSimpleName();

    @Autowired
    private List<ChannelKey> channelKeys;
    @Autowired
    private List<DistributionChannel<?>> distributionChannels;
    @Autowired
    private List<DistributionEventReceiver<?>> distributionEventReceivers;
    @Autowired
    private List<DistributionChannelTestAction> distributionChannelTestActions;

    @Test
    public void channelKeysCountMatchesDistributionChannelsCountTest() {
        assertEquals(
            channelKeys.size(),
            distributionChannels.size(),
            String.format("The number of %ss does not match the number of %ss", ChannelKey.class.getSimpleName(), DistributionChannel.class.getSimpleName())
        );
    }

    /**
     * The purpose of this test is to determine if a Channel has created all of the necessary Spring components<br />
     * in order for it to function properly within Alert. Currently that functionality includes receiving<br />
     * distribution events and performing "test actions".
     */
    @Test
    public void channelsHaveRequiredSpringComponentsTest() {
        List<String> channelFailureMessages = new LinkedList<>();
        for (ChannelKey channelKey : channelKeys) {
            List<String> missingComponents = findMissingComponents(channelKey);
            if (!missingComponents.isEmpty()) {
                String channelFailureMessage = createChannelFailureMessage(channelKey, missingComponents);
                channelFailureMessages.add(channelFailureMessage);
            }
        }

        if (!channelFailureMessages.isEmpty()) {
            String failureMessage = StringUtils.join(channelFailureMessages, "\n\n");
            fail(failureMessage);
        }
    }

    private List<String> findMissingComponents(ChannelKey channelKey) {
        List<String> missingComponents = new LinkedList<>();
        createDistributionEventReceiverMissingMessage(channelKey).ifPresent(missingComponents::add);
        createTestActionMissingMessage(channelKey).ifPresent(missingComponents::add);
        return missingComponents;
    }

    private Optional<String> createDistributionEventReceiverMissingMessage(ChannelKey channelKey) {
        boolean hasReceiver = distributionEventReceivers
                                  .stream()
                                  .anyMatch(receiver -> receiver.getDestinationName().equals(channelKey.getUniversalKey()));
        if (!hasReceiver) {
            return Optional.of(COMPONENT_NAME_DISTRIBUTION_RECEIVER);
        }
        return Optional.empty();
    }

    private Optional<String> createTestActionMissingMessage(ChannelKey channelKey) {
        Optional<DistributionChannelTestAction> foundTestAction = distributionChannelTestActions
                                                                      .stream()
                                                                      .filter(testAction -> testAction.getDescriptorKey().equals(channelKey))
                                                                      .findAny();
        if (foundTestAction.isPresent()) {
            DistributionChannelTestAction testAction = foundTestAction.get();
            if (channelKey instanceof IssueTrackerChannelKey && !(testAction instanceof IssueTrackerTestAction)) {
                return Optional.of(COMPONENT_NAME_ISSUE_TRACKER_TEST_ACTION);
            }
        } else {
            return Optional.of(COMPONENT_NAME_DISTRIBUTION_TEST_ACTION);
        }
        return Optional.empty();
    }

    private String createChannelFailureMessage(ChannelKey channelKey, List<String> missingComponents) {
        String missingComponentsString = StringUtils.join(missingComponents, ", ");
        return String.format("%s (%s) was missing the following components: %s", channelKey.getDisplayName(), channelKey.getUniversalKey(), missingComponentsString);
    }

}
