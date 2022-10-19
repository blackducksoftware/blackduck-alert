import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import { AZURE_BOARDS_INFO, AZURE_BOARDS_URLS } from 'page/channel/azure/AzureBoardsModel';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import AzureBoardsTable from 'page/channel/azure/AzureBoardsTable';
import { TableHeaderColumn } from 'react-bootstrap-table';

const AzureBoardsTableConstructor = ({
    csrfToken, readonly, showRefreshButton, displayDelete
}) => {
    const [azureBoardsData, setAzureBoardsData] = useState([]);

    const assignedDataFormat = (cell) => (
        <div title={(cell) ? cell.toString() : null}>
            {cell}
        </div>
    );

    const azureRequestUrl = `${ConfigurationRequestBuilder.CONFIG_API_URL}/azure-boards`;

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
        <AzureBoardsTable
            csrfToken={csrfToken}
            key={AZURE_BOARDS_INFO.key}
            label={AZURE_BOARDS_INFO.label}
            description="Configure the Azure Boards instance that Alert will send issue updates to."
            apiUrl={azureRequestUrl}
            tableData={azureBoardsData}
            setTableData={setAzureBoardsData}
            editPageUrl={AZURE_BOARDS_URLS.editUrl}
            copyPageUrl={AZURE_BOARDS_URLS.copyUrl}
            includeEnabled={false}
            readonly={readonly}
            showRefreshButton={showRefreshButton}
            displayDelete={displayDelete}
        >
            {createColumn('name', 'Name')}
            {createColumn('organizationName', 'Organization Name')}
            {createColumn('createdAt', 'Created At')}
            {createColumn('lastUpdated', 'Last Updated')}
        </AzureBoardsTable>
    );
};

AzureBoardsTableConstructor.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    readonly: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    displayDelete: PropTypes.bool
};

AzureBoardsTableConstructor.defaultProps = {
    readonly: false,
    showRefreshButton: false,
    displayDelete: true
};

export default AzureBoardsTableConstructor;
