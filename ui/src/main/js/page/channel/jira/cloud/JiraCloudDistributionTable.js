import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import JiraCloudEditCell from 'page/channel/jira/cloud/JiraCloudEditCell';
import JiraCloudDistributionTableActions from 'page/channel/jira/cloud/JiraCloudDistributionTableActions';

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please add a Jira Cloud field mapping to use this table.'
};

function JiraCloudDistributionTable({ cloudTableData, onFieldMappingUpdate }) {
    const [tableData, setTableData] = useState(cloudTableData);
    const [selected, setSelected] = useState([]);
    const [data, setData] = useState();

    function handleEditData(data) {
        setData(data);
    }

    useEffect(() => {
        onFieldMappingUpdate(tableData);
    }, [tableData]);

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
        settings: { alignment: 'center', tableData },
        customCallback: handleEditData
    }];
    
    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    return (
        <Table
            tableData={tableData}
            columns={COLUMNS}
            multiSelect
            selected={selected}
            onSelected={onSelected}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => <JiraCloudDistributionTableActions data={tableData} selected={selected} updateTableData={setTableData} />}
        />
    );
}

JiraCloudDistributionTable.propTypes = {
    tableData: PropTypes.object
};

export default JiraCloudDistributionTable;
