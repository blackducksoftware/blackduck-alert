import React from 'react';
import Table from 'common/component/table/Table';
import AboutProviderCell from 'page/about/AboutProviderCell';

const COLUMNS = [{
    key: 'name',
    label: 'Provider Name',
    customCell: AboutProviderCell
}];

const emptyTableConfig = {
    message: 'There are no records to display for this table.'
};

const AboutProviderTable = ({ tableData }) => (
    <Table
        tableData={tableData}
        columns={COLUMNS}
        emptyTableConfig={emptyTableConfig}
    />
);

export default AboutProviderTable;
