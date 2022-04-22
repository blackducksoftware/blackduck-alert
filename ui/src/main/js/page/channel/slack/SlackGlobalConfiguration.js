import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';

const SlackGlobalConfiguration = () => (
    <PageHeader
        title={SLACK_INFO.label}
        description="Configure Slack for Alert."
    />
);

export default SlackGlobalConfiguration;
