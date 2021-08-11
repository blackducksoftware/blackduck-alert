package com.synopsys.integration.alert.api.channel.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.convert.IssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueTransitioner;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public class IssueTrackerChannelTest {
    @Test
    public void distributeMessagesTest() throws AlertException {
        IssueTrackerModelExtractor<String> modelExtractor = new IssueTrackerModelExtractor<>(createFormatter(), null);
        IssueTrackerMessageSender<String> messageSender = createMessageSender();
        IssueTrackerProcessor<String> processor = new IssueTrackerProcessor<>(modelExtractor, messageSender);

        IssueTrackerProcessorFactory<DistributionJobDetailsModel, String> processorFactory = x -> processor;
        IssueTrackerResponsePostProcessor postProcessor = new IssueTrackerResponsePostProcessor() {
            @Override
            public <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
            }
        };
        IssueTrackerChannel<DistributionJobDetailsModel, String> issueTrackerChannel = new IssueTrackerChannel<>(processorFactory, postProcessor) {};

        MessageResult testResult = issueTrackerChannel.distributeMessages(null, ProviderMessageHolder.empty(), null);

        IssueTrackerResponse<?> processorResponse = processor.processMessages(ProviderMessageHolder.empty(), "jobName");
        assertEquals(processorResponse.getStatusMessage(), testResult.getStatusMessage());
    }

    private IssueTrackerMessageSender<String> createMessageSender() {
        IssueTrackerIssueCommenter<String> commenter = new IssueTrackerIssueCommenter<>(null) {
            @Override
            protected boolean isCommentingEnabled() {
                return false;
            }

            @Override
            protected void addComment(String comment, ExistingIssueDetails<String> existingIssueDetails, @Nullable ProjectIssueModel source) {
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

    private IssueTrackerMessageFormatter createFormatter() {
        return new IssueTrackerMessageFormatter(10000, 10000, 10000, "\n") {
            @Override
            public String encode(String txt) {
                return txt;
            }

            @Override
            public String emphasize(String txt) {
                return txt;
            }

            @Override
            public String createLink(String txt, String url) {
                return url;
            }
        };
    }

}
