import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import JiraServerTable from 'page/channel/jira/server/JiraServerTable';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';

const JiraServerPageLayout = ({ readonly, allowDelete }) => (
    <div>
        <PageHeader
            title={JIRA_SERVER_INFO.label}
            description="Configure the Jira Server instance that Alert will send issue updates to."
        />
        <JiraServerTable readonly={readonly} allowDelete={allowDelete} />
    </div>
);

export default JiraServerPageLayout;
