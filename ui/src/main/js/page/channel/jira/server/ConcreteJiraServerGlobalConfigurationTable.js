import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import { JIRA_SERVER_INFO, JIRA_SERVER_URLS } from 'page/channel/jira/server/JiraServerModel';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import ConcreteGlobalConfigurationTable from 'page/channel/jira/server/ConcreteGlobalConfigurationTable';
import { TableHeaderColumn } from 'react-bootstrap-table';

const ConcreteJiraServerGlobalConfigurationTable = ({ csrfToken, readonly, showRefreshButton, displayDelete }) => {
    const [jiraServerConfigs, setJiraServerConfigs] = useState([]);

    const jiraServerRequestUrl = `${ConfigurationRequestBuilder.CONFIG_API_URL}/jira_server`;
    // FIXME needs to add true paging
    const jiraServerPagedRequestUrl = `${jiraServerRequestUrl}?pageNumber=0&pageSize=100`;

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(jiraServerPagedRequestUrl, csrfToken);
        const data = await response.json();
        if (data) {
            setJiraServerConfigs(data.models);
        }
    };

    const deleteData = async (configId) => {
        await ConfigurationRequestBuilder.createDeleteRequest(jiraServerRequestUrl, csrfToken, configId);
    };

    const assignedDataFormat = (cell) => (
        <div title={(cell) ? cell.toString() : null}>
            {cell}
        </div>
    );

    const createColumn = (header, text) => (
        <TableHeaderColumn
            key={header}
            dataField={header}
            searchable
            dataSort
            columnClassName="tableCell"
            tdStyle={{ whiteSpace: 'normal' }}
            dataFormat={assignedDataFormat}
        >
            {text}
        </TableHeaderColumn>
    );

    return (
        <ConcreteGlobalConfigurationTable
            key={JIRA_SERVER_INFO.key}
            label={JIRA_SERVER_INFO.label}
            description="Configure the Jira Server instance that Alert will send issue updates to."
            tableData={jiraServerConfigs}
            readRequest={fetchData}
            deleteRequest={deleteData}
            editPageUrl={JIRA_SERVER_URLS.jiraServerEditUrl}
            copyPageUrl={JIRA_SERVER_URLS.jiraServerCopyUrl}
            includeEnabled={false}
            readonly={readonly}
            showRefreshButton={showRefreshButton}
            displayDelete={displayDelete}
        >
            {createColumn('name', 'Name')}
            {createColumn('url', 'Url')}
            {createColumn('createdAt', 'Created At')}
            {createColumn('lastUpdated', 'Last Updated')}
        </ConcreteGlobalConfigurationTable>
    );
};

ConcreteJiraServerGlobalConfigurationTable.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    readonly: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    displayDelete: PropTypes.bool
};

ConcreteJiraServerGlobalConfigurationTable.defaultProps = {
    readonly: false,
    showRefreshButton: false,
    displayDelete: true
};

export default ConcreteJiraServerGlobalConfigurationTable;
