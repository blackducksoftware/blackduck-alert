import React from 'react';
import PropTypes from 'prop-types';
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

AboutProviderTable.propTypes = {
    tableData: PropTypes.arrayOf(PropTypes.object)
};

export default AboutProviderTable;
