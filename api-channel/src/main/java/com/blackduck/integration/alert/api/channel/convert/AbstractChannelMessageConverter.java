package com.blackduck.integration.alert.api.channel.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.ListUtils;

import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessage;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public abstract class AbstractChannelMessageConverter<D extends DistributionJobDetailsModel, T> implements ChannelMessageConverter<D, T> {
    private final SimpleMessageConverter simpleMessageConverter;
    private final ProjectMessageConverter projectMessageConverter;

    protected AbstractChannelMessageConverter(ChannelMessageFormatter channelMessageFormatter) {
        this(new SimpleMessageConverter(channelMessageFormatter), new ProjectMessageConverter(channelMessageFormatter));
    }

    protected AbstractChannelMessageConverter(SimpleMessageConverter simpleMessageConverter, ProjectMessageConverter projectMessageConverter) {
        this.simpleMessageConverter = simpleMessageConverter;
        this.projectMessageConverter = projectMessageConverter;
    }

    @Override
    public final List<T> convertToChannelMessages(D distributionDetails, ProviderMessageHolder messages, String jobName) {
        List<T> convertedSimpleMessages = convertProviderMessagesToChannelMessages(
            messages.getSimpleMessages(),
            simpleMessage -> simpleMessageConverter.convertToFormattedMessageChunks(simpleMessage, jobName),
            (message, formattedMessageChunks) -> convertSimpleMessageToChannelMessages(distributionDetails, message, formattedMessageChunks)
        );

        List<T> convertedProjectMessages = convertProviderMessagesToChannelMessages(
            messages.getProjectMessages(),
            projectMessage -> projectMessageConverter.convertToFormattedMessageChunks(projectMessage, jobName),
            (message, formattedMessageChunks) -> convertProjectMessageToChannelMessages(distributionDetails, message, formattedMessageChunks)
        );

        return ListUtils.union(convertedSimpleMessages, convertedProjectMessages);
    }

    protected abstract List<T> convertSimpleMessageToChannelMessages(D distributionDetails, SimpleMessage simpleMessage, List<String> messageChunks);

    protected abstract List<T> convertProjectMessageToChannelMessages(D distributionDetails, ProjectMessage projectMessage, List<String> messageChunks);

    private <M extends ProviderMessage<M>> List<T> convertProviderMessagesToChannelMessages(List<M> messages, Function<M, List<String>> convertToMessageChunks, BiFunction<M, List<String>, List<T>> convertToChannelMessages) {
        List<T> channelMessages = new LinkedList<>();
        for (M message : messages) {
            List<String> projectMessageChunks = convertToMessageChunks.apply(message);
            List<T> projectChannelMessages = convertToChannelMessages.apply(message, projectMessageChunks);
            channelMessages.addAll(projectChannelMessages);
        }
        return channelMessages;
    }

}
