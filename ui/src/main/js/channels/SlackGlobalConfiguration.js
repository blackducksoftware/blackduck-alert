import React from 'react';
import EmptyGlobalConfiguration from 'channels/EmptyGlobalConfiguration';
import { SLACK_INFO } from 'channels/SlackModels';

const SlackGlobalConfiguration = () => (
    <EmptyGlobalConfiguration label={SLACK_INFO.label} description="Configure Slack for Alert." />
);

export default SlackGlobalConfiguration;
