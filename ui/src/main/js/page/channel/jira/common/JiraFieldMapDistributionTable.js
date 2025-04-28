import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import JiraFieldMapEditCell from 'page/channel/jira/common/JiraFieldMapEditCell';
import JiraFieldMapTableActions from 'page/channel/jira/common/JiraFieldMapTableActions';
import JiraFieldAlwaysCreateJsonCell from "./JiraFieldAlwaysCreateJsonCell";

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please add a Jira field mapping to use this table.'
};

const JiraFieldMapDistributionTable = ({ initialData, onFieldMappingUpdate }) => {
    const [tableData, setTableData] = useState(initialData);
    const [selected, setSelected] = useState([]);

    useEffect(() => {
        onFieldMappingUpdate(tableData);
    }, [tableData]);

    function handleEditData(editedData) {
        onFieldMappingUpdate(editedData)
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
        key: 'createJsonObject',
        label: 'Treat Value as JSON',
        sortable: false,
        customCell: JiraFieldAlwaysCreateJsonCell,
        settings: { alignment: 'center' }
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
};

JiraFieldMapDistributionTable.propTypes = {
    initialData: PropTypes.object,
    onFieldMappingUpdate: PropTypes.func
};

export default JiraFieldMapDistributionTable;
