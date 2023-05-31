import React from 'react';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import EventTypeCell from 'page/audit/EventTypeCell';
import DistributionLastSentCell from 'page/audit/DistributionLastSentCell';
import RefreshFailureCell from 'page/audit/RefreshFailureCell';

const emptyTableConfig = {
    message: 'There are no records to display for this table.'
};

const DistributionTable = ({ data }) => {
    const COLUMNS = [{
        key: 'name',
        label: 'Distribution Job',
        sortable: false
    }, {
        key: 'eventType',
        label: 'Event Type',
        sortable: false,
        customCell: EventTypeCell
    }, {
        key: 'timeLastSent',
        label: 'Time Last Sent',
        sortable: false,
        customCell: DistributionLastSentCell
    }, {
        key: 'refreshJob',
        label: 'Refresh',
        sortable: false,
        customCell: RefreshFailureCell,
        settings: {
            alignment: 'center',
            type: 'job',
            notificationId: data.id
        }
    }];

    return (
        <Table
            tableData={data?.jobs}
            columns={COLUMNS}
            emptyTableConfig={emptyTableConfig}
        />
    );
};

DistributionTable.propTypes = {
    data: PropTypes.shape({
        jobs: PropTypes.object,
        id: PropTypes.string
    })
};

export default DistributionTable;
