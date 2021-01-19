package com.synopys.integration.alert.channel.api;

public abstract class IssueTrackerChannel extends Channel {
    protected final IssueTrackerMessageResolver issueTrackerMessageResolver;

    public IssueTrackerChannel(ChannelMessageFormatter channelMessageFormatter, ChannelMessageSender channelMessageSender, IssueTrackerMessageResolver issueTrackerMessageResolver) {
        super(channelMessageFormatter, channelMessageSender);
        this.issueTrackerMessageResolver = issueTrackerMessageResolver;
    }

    @Override
    public final void handleEvent(Object event) {
        Object resolvedEvent = issueTrackerMessageResolver.resolve(event);
        Object message = channelMessageFormatter.formatEvent(resolvedEvent);
        Object result = channelMessageSender.sendMessage(message);
    }

}
