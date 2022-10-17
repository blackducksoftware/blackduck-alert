import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import { AZURE_INFO, AZURE_URLS } from 'page/channel/azure/AzureModel';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import AzureBoardTable from 'page/channel/azure/AzureBoardTable';
import { TableHeaderColumn } from 'react-bootstrap-table';

const AzureBoardTableConstructor = ({
    csrfToken, readonly, showRefreshButton, displayDelete
}) => {
    const [azureBoardData, setAzureBoardData] = useState([]);

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
        <AzureBoardTable
            csrfToken={csrfToken}
            key={AZURE_INFO.key}
            label={AZURE_INFO.label}
            description="Configure the Azure Boards instance that Alert will send issue updates to."
            apiUrl={azureRequestUrl}
            tableData={azureBoardData}
            setTableData={setAzureBoardData}
            editPageUrl={AZURE_URLS.editUrl}
            copyPageUrl={AZURE_URLS.copyUrl}
            includeEnabled={false}
            readonly={readonly}
            showRefreshButton={showRefreshButton}
            displayDelete={displayDelete}
        >
            {createColumn('name', 'Name')}
            {createColumn('organizationName', 'Organization Name')}
            {createColumn('createdAt', 'Created At')}
            {createColumn('lastUpdated', 'Last Updated')}
        </AzureBoardTable>
    );
};

AzureBoardTableConstructor.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    readonly: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    displayDelete: PropTypes.bool
};

AzureBoardTableConstructor.defaultProps = {
    readonly: false,
    showRefreshButton: false,
    displayDelete: true
};

export default AzureBoardTableConstructor;
