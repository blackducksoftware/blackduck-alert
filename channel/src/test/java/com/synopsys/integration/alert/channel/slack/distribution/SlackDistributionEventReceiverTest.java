package com.synopsys.integration.alert.channel.slack.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.slack.distribution.mock.MockChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SlackJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class SlackDistributionEventReceiverTest {
    //TODO: 
    // Create a "huge" DistributionEvent for slack to create  multiple messages
    // Mock slack such that it produces a 429, Too Many Requests error

    //Need to figure out:
    // What are we asserting on and what is the expected behavior we want?
    // We probably don't want to JMS overhead. Should be strongly avoided if possible since it complicates Mocking

    //TODO:  Test  1
    //Need a concrete instance of a Slack Channel
    //Need a "large" Distribution Event such that it makes a lot of messages
    //Create a ChannelRestConnectFactory "Mock" that returns a mocked IntHttpClient "Mock" that can be used to simulate the failure
    //This test should be calling SlackChannel's super class MessageBoardChannel.distributeMessages. Pass in the ProviderMessageHolder here

    //TODO:   Test 2, do everything above plus:
    //Create a ProcessingAuditAccessor "Mock" that we can use to test and assert results from the auditAccessor
    //Create a SlackDistributionEventReceiver using the SlackChannel from Test 1.
    //Call the handleEvent on the SlackDistributionEventReceiver's super class DistributionEventReceiver

    private Gson gson;
    private ProcessingAuditAccessor processingAuditAccessor;
    private SlackJobDetailsAccessor slackJobDetailsAccessor;
    private SlackChannel slackChannel;
    private SlackChannelKey slackChannelKey;

    private final String lotsOfText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    @BeforeEach
    public void init() {
        Gson gson = new Gson();
        ProcessingAuditAccessor processingAuditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        SlackJobDetailsAccessor slackJobDetailsAccessor = Mockito.mock(SlackJobDetailsAccessor.class);

        //Slack Channel setup
        SlackChannelKey slackChannelKey = new SlackChannelKey();

        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        SlackChannelMessageFormatter slackChannelMessageFormatter = new SlackChannelMessageFormatter(markupEncoderUtil);
        SlackChannelMessageConverter slackChannelMessageConverter = new SlackChannelMessageConverter(slackChannelMessageFormatter);

        AlertProperties alertProperties = new AlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        MockChannelRestConnectionFactory mockChannelRestConnectionFactory = new MockChannelRestConnectionFactory(alertProperties, proxyManager);
        RestChannelUtility restChannelUtility = new RestChannelUtility(mockChannelRestConnectionFactory);
        SlackChannelMessageSender slackChannelMessageSender = new SlackChannelMessageSender(restChannelUtility, slackChannelKey);

        slackChannel = new SlackChannel(slackChannelMessageConverter, slackChannelMessageSender);
    }

    @Test
    public void testSlackChannel() throws AlertException {
        //Need a SlackJobDetailsModel  & SlackChannelMessageModel
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(UUID.randomUUID(), "http://url", "channelName", "channelUsername");
        ProviderMessageHolder providerMessageHolder = createProviderMessageHolder();

        slackChannel.distributeMessages(slackJobDetailsModel, providerMessageHolder);
    }

    @Test
    public void testSlackDistributionEventReceiver() {
        //Test 2
        SlackDistributionEventReceiver slackDistributionEventReceiver = new SlackDistributionEventReceiver(
            gson,
            processingAuditAccessor,
            slackJobDetailsAccessor,
            slackChannel,
            slackChannelKey
        );
    }

    private ProviderMessageHolder createProviderMessageHolder() {
        List<ProjectMessage> projectMessages = new ArrayList<>();
        List<SimpleMessage> simpleMessages = new ArrayList<>();

        LinkableItem provider = new LinkableItem("Provider", "provider1");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem project = new LinkableItem("Project", "Common Project");
        ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(providerDetails, project, ProjectOperation.CREATE);
        projectMessages.add(projectMessage);

        /*
        LinkableItem details = new LinkableItem("Details", "detail1");
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "summary", "description", List.of(details));
        simpleMessages.add(simpleMessage);
         */

        //make a large summary
        LinkableItem newDetails = new LinkableItem("Details2", "details2");
        SimpleMessage simpleMessage2 = SimpleMessage.original(providerDetails, lotsOfText, lotsOfText, List.of(newDetails));
        simpleMessages.add(simpleMessage2);

        return new ProviderMessageHolder(projectMessages, simpleMessages);
    }

    //TODO
    private DistributionEvent createSlackDistributionEvent() {
        //DistributionEvent distributionEvent = new DistributionEvent(slackChannelKey);

        //return distributionEvent;
        return null;
    }
}
