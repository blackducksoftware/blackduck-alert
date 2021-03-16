package com.synopsys.integration.alert.channel.api.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueTransitioner;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public class IssueTrackerChannelTest {
    @Test
    public void distributeMessagesTest() throws AlertException {
        IssueTrackerModelExtractor<String> modelExtractor = new IssueTrackerModelExtractor<>(null, null);
        IssueTrackerMessageSender<String> messageSender = createMessageSender();
        IssueTrackerProcessor<String> processor = new IssueTrackerProcessor<>(modelExtractor, messageSender);

        IssueTrackerProcessorFactory<DistributionJobDetailsModel, String> processorFactory = x -> processor;
        IssueTrackerResponsePostProcessor postProcessor = x -> {};
        IssueTrackerChannel<DistributionJobDetailsModel, String> issueTrackerChannel = new IssueTrackerChannel<>(processorFactory, postProcessor) {};

        MessageResult testResult = issueTrackerChannel.distributeMessages(null, ProviderMessageHolder.empty());

        IssueTrackerResponse processorResponse = processor.processMessages(ProviderMessageHolder.empty());
        assertEquals(processorResponse.getStatusMessage(), testResult.getStatusMessage());
    }

    private IssueTrackerMessageSender<String> createMessageSender() {
        IssueTrackerIssueCommenter<String> commenter = new IssueTrackerIssueCommenter<>(null) {
            @Override
            protected boolean isCommentingEnabled() {
                return false;
            }

            @Override
            protected void addComment(String comment, ExistingIssueDetails<String> existingIssueDetails, ProjectIssueModel source) {
            }
        };
        IssueTrackerIssueTransitioner<String> transitioner = new IssueTrackerIssueTransitioner<>(commenter, null) {
            @Override
            protected Optional<String> retrieveJobTransitionName(IssueOperation issueOperation) {
                return Optional.empty();
            }

            @Override
            protected boolean isTransitionRequired(ExistingIssueDetails<String> existingIssueDetails, IssueOperation issueOperation) {
                return false;
            }

            @Override
            protected void findAndPerformTransition(ExistingIssueDetails<String> existingIssueDetails, String transitionName) {
            }
        };
        IssueTrackerIssueCreator<String> creator = new IssueTrackerIssueCreator<>(null, commenter, null) {
            @Override
            protected ExistingIssueDetails<String> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) {
                return null;
            }

            @Override
            protected void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) {
            }
        };
        return new IssueTrackerMessageSender<>(creator, transitioner, commenter);
    }

}
