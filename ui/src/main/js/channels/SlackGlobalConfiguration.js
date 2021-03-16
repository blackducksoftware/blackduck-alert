import React from 'react';
import CommonGlobalConfiguration from './CommonGlobalConfiguration';

const SlackGlobalConfiguration = () => (
    <CommonGlobalConfiguration label="Slack" description="Configure Slack for Alert.">
        <div className="form-horizontal">
            There is no global configuration required. The configuration is handled in the distribution jobs.
        </div>
    </CommonGlobalConfiguration>
);

export default SlackGlobalConfiguration;
