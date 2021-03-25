package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.api.DistributionEventReceiver;
import com.synopsys.integration.alert.channel.api.issue.action.IssueTrackerTestAction;
import com.synopsys.integration.alert.common.channel.DistributionChannelTestAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class Channel_ComprehensiveRequirements_ITTest {
    private static final String COMPONENT_NAME_DISTRIBUTION_RECEIVER = DistributionEventReceiver.class.getSimpleName();
    private static final String COMPONENT_NAME_DISTRIBUTION_TEST_ACTION = DistributionChannelTestAction.class.getSimpleName();
    private static final String COMPONENT_NAME_ISSUE_TRACKER_TEST_ACTION = IssueTrackerTestAction.class.getSimpleName();

    @Autowired
    private List<ChannelKey> channelKeys;
    @Autowired
    private List<DistributionEventReceiver> distributionEventReceivers;
    @Autowired
    private List<DistributionChannelTestAction> distributionChannelTestActions;

    @Test
    public void checkIfChannelsHaveAllNecessarySpringComponents() {
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

        // Distribution Event Receiver
        boolean hasReceiver = distributionEventReceivers
                                  .stream()
                                  .anyMatch(receiver -> receiver.getDestinationName().equals(channelKey.getUniversalKey()));
        if (!hasReceiver) {
            missingComponents.add(COMPONENT_NAME_DISTRIBUTION_RECEIVER);
        }

        // Distribution Test Action
        Optional<DistributionChannelTestAction> foundTestAction = distributionChannelTestActions
                                                                      .stream()
                                                                      .filter(testAction -> testAction.getDescriptorKey().equals(channelKey))
                                                                      .findAny();
        if (foundTestAction.isPresent()) {
            DistributionChannelTestAction testAction = foundTestAction.get();
            if (channelKey instanceof IssueTrackerChannelKey && !(testAction instanceof IssueTrackerTestAction)) {
                missingComponents.add(COMPONENT_NAME_ISSUE_TRACKER_TEST_ACTION);
            }
        } else {
            missingComponents.add(COMPONENT_NAME_DISTRIBUTION_TEST_ACTION);
        }

        return missingComponents;
    }

    private String createChannelFailureMessage(ChannelKey channelKey, List<String> missingComponents) {
        String missingComponentsString = StringUtils.join(missingComponents, ", ");
        return String.format("%s (%s) was missing the following components: %s", channelKey.getDisplayName(), channelKey.getUniversalKey(), missingComponentsString);
    }

}
