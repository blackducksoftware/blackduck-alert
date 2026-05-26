import React from 'react';
import PropTypes from 'prop-types';
import PageLayout from 'common/component/PageLayout';
import JiraServerTable from 'page/channel/jira/server/JiraServerTable';
import { JIRA_SERVER_INFO } from 'page/channel/jira/server/JiraServerModel';
import useGetPermissions from 'common/hooks/useGetPermissions';

const JiraServerPageLayout = ({ descriptor }) => {
    const { readOnly, canDelete } = useGetPermissions(descriptor);

    return (
        <PageLayout
            title={JIRA_SERVER_INFO.label}
            description="Configure the Jira Server instance that Alert will send issue updates to."
            headerIcon="server"
        >
            <JiraServerTable readOnly={readOnly} allowDelete={canDelete} />
        </PageLayout>
    );
};

JiraServerPageLayout.propTypes = {
    descriptor: PropTypes.object
};

export default JiraServerPageLayout;
