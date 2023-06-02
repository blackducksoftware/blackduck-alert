import React from 'react';
import Table from 'common/component/table/Table';
import AboutChannelCell from 'page/about/AboutChannelCell';

const COLUMNS = [{
    key: 'name',
    label: 'Channel Name',
    customCell: AboutChannelCell
}];

const emptyTableConfig = {
    message: 'There are no records to display for this table.'
};

const AboutChannelsTable = ({ tableData }) => (
    <Table
        tableData={tableData}
        columns={COLUMNS}
        emptyTableConfig={emptyTableConfig}
    />
);

export default AboutChannelsTable;
