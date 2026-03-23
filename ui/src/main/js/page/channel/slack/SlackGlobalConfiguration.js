import React from 'react';
import PageLayout from 'common/component/PageLayout';
import { SLACK_INFO } from 'page/channel/slack/SlackModels';

const SlackGlobalConfiguration = () => (
    <PageLayout
        title={SLACK_INFO.label}
        description="Configure Slack for Alert."
        headerIcon={['fab', 'slack']}
    />
);

export default SlackGlobalConfiguration;
