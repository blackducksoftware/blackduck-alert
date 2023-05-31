import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import JiraFieldMapEditCell from 'page/channel/jira/common/JiraFieldMapEditCell';
import JiraFieldMapTableActions from 'page/channel/jira/common/JiraFieldMapTableActions';

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please add a Jira Cloud field mapping to use this table.'
};

function JiraFieldMapDistributionTable({ cloudTableData, onFieldMappingUpdate }) {
    const [tableData, setTableData] = useState(cloudTableData);
    const [selected, setSelected] = useState([]);
    const [data, setData] = useState();

    useEffect(() => {
        onFieldMappingUpdate(tableData);
    }, [tableData]);

    function handleEditData(data) {
        setData(data);
    }

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
        customCell: JiraFieldMapEditCell,
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
            tableActions={() => <JiraFieldMapTableActions data={tableData} selected={selected} setSelected={setSelected} updateTableData={setTableData} />}
        />
    );
}

JiraFieldMapDistributionTable.propTypes = {
    tableData: PropTypes.object
};

export default JiraFieldMapDistributionTable;
