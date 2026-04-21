import React from 'react';
import PropTypes from 'prop-types';
import PageLayout from 'common/component/PageLayout';
import JiraServerTable from 'page/channel/jira/server/JiraServerTable';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';

const JiraServerPageLayout = ({ readonly, allowDelete }) => (
    <PageLayout
        title={JIRA_SERVER_INFO.label}
        description="Configure the Jira Server instance that Alert will send issue updates to."
        headerIcon="server"
    >
        <JiraServerTable readOnly={readonly} allowDelete={allowDelete} />
    </PageLayout>
);

JiraServerPageLayout.propTypes = {
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool
};

export default JiraServerPageLayout;