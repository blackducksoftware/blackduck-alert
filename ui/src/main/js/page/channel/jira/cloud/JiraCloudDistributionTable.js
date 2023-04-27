import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import JiraCloudEditCell from 'page/channel/jira/cloud/JiraCloudEditCell';
import JiraCloudDistributionTableActions from 'page/channel/jira/cloud/JiraCloudDistributionTableActions';

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please create a Provider connection to use this table.'
};

function JiraCloudDistributionTable({ cloudTableData }) {
    const [tableData, setTableData] = useState();
    const [selected, setSelected] = useState([]);
    const [sortConfig, setSortConfig] = useState();

    const COLUMNS = [{
        key: 'fieldName',
        label: 'Jira Name',
        sortable: true
    }, {
        key: 'fieldValue',
        label: 'Value',
        sortable: true
    }, {
        key: 'editJiraCloudFieldMapping',
        label: 'Edit',
        sortable: false,
        customCell: JiraCloudEditCell,
        settings: { alignment: 'center', tableData, setTableData },
        customCallback: setTableData
    }];
    
    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    const onSort = (name) => {
        if (name !== sortConfig?.name || !sortConfig) {
            return setSortConfig({ name, direction: 'ASC' });
        }

        if (name === sortConfig?.name && sortConfig?.direction === 'DESC') {
            return setSortConfig();
        }

        if (name === sortConfig?.name) {
            return setSortConfig({ name, direction: 'DESC' });
        }

        return setSortConfig();
    };

    useEffect(() => {
        let data = cloudTableData;

        if (sortConfig) {
            const { name, direction } = sortConfig;
            data = [...data].sort((a, b) => {
                if (a[name] === null) {
                    return 1;
                }
                if (b[name] === null) {
                    return -1;
                }
                if (a[name] === null && b[name] === null) {
                    return 0;
                }
                return (
                    a[name].toString().localeCompare(b[name].toString(), 'en', { numeric: true }) * (direction === 'ASC' ? 1 : -1)
                );
            });
        }
        setTableData(data);
    }, [cloudTableData, sortConfig]);

    return (
        <Table
            tableData={tableData}
            columns={COLUMNS}
            multiSelect
            selected={selected}
            onSelected={onSelected}
            onSort={onSort}
            sortConfig={sortConfig}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => <JiraCloudDistributionTableActions data={tableData} selected={selected} updateTableData={setTableData} />}
        />
    );
}

JiraCloudDistributionTable.propTypes = {
    tableData: PropTypes.object
};

export default JiraCloudDistributionTable;
