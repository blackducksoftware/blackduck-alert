import React from 'react';
import EmptyGlobalConfiguration from 'global/channels/EmptyGlobalConfiguration';
import { SLACK_INFO } from 'global/channels/slack/SlackModels';

const SlackGlobalConfiguration = () => (
    <EmptyGlobalConfiguration label={SLACK_INFO.label} description="Configure Slack for Alert." />
);

export default SlackGlobalConfiguration;
