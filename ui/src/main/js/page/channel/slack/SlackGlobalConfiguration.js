import React from 'react';
import EmptyGlobalConfiguration from 'common/configuration/global/EmptyGlobalConfiguration';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';

const SlackGlobalConfiguration = () => (
    <EmptyGlobalConfiguration label={SLACK_INFO.label} description="Configure Slack for Alert." />
);

export default SlackGlobalConfiguration;
