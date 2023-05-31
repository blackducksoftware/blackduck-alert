import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import DistributionTableActions from 'page/distribution/DistributionTableActions';
import DistributionActionCell from 'page/distribution/DistributionActionCell';
import ChannelCell from 'page/distribution/ChannelCell';
import ProviderCell from 'page/distribution/ProviderCell';
import FrequencyCell from 'page/distribution/FrequencyCell';
import ProviderEnabledCell from 'page/provider/ProviderEnabledCell';
import { fetchDistibution } from 'store/actions/distribution';

const COLUMNS = [{
    key: 'jobName',
    label: 'Name',
    sortable: true
}, {
    key: 'channelName',
    label: 'Channel',
    sortable: true,
    customCell: ChannelCell
}, {
    key: 'provider',
    label: 'Provider',
    sortable: true,
    customCell: ProviderCell
}, {
    key: 'frequencyType',
    label: 'Frequency',
    sortable: true,
    customCell: FrequencyCell
}, {
    key: 'auditTimeLastSent',
    label: 'Last Run',
    sortable: false
}, {
    key: 'auditStatus',
    label: 'Status',
    sortable: false
}, {
    key: 'enabled',
    label: 'Enabled',
    sortable: false,
    customCell: ProviderEnabledCell,
    settings: { alignment: 'center' }
}, {
    key: 'edit',
    label: 'Edit',
    sortable: false,
    customCell: DistributionActionCell,
    settings: { alignment: 'center', type: 'edit' }
}, {
    key: 'copy',
    label: 'Copy',
    sortable: false,
    customCell: DistributionActionCell,
    settings: { alignment: 'center', type: 'copy' }
}];

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please create a Distribution Job to use this table.'
};

const DistributionTable = ({ readonly }) => {
    const dispatch = useDispatch();
    const { data } = useSelector((state) => state.distribution);
    const refreshStatus = JSON.parse(window.localStorage.getItem('DISTRIBUTION_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [selected, setSelected] = useState([]);
    const [sortConfig, setSortConfig] = useState();
    const [paramsConfig, setParamsConfig] = useState({
        pageNumber: data?.pageNumber || 0,
        pageSize: data?.pageSize,
        mutatorData: {
            searchTerm: data?.mutatorData?.searchTerm,
            sortName: data?.mutatorData?.name,
            sortOrder: data?.mutatorData?.direction
        }
    });

    useEffect(() => {
        dispatch(fetchDistibution(paramsConfig));
    }, [paramsConfig]);

    useEffect(() => {
        localStorage.setItem('DISTRIBUTION_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchDistibution()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        return undefined;
    }, [autoRefresh]);

    const handleSearchChange = (e) => {
        setParamsConfig({ ...paramsConfig,
            mutatorData: {
                ...paramsConfig.mutatorData,
                searchTerm: e.target.value
            } });
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    function handlePagination(page) {
        setParamsConfig({ ...paramsConfig, pageNumber: page });
    }

    const onSort = (name) => {
        const { sortName, sortOrder } = paramsConfig.mutatorData;
        if (name !== sortName) {
            setSortConfig({ name, direction: 'ASC' });
            return setParamsConfig({ ...paramsConfig,
                mutatorData: {
                    ...paramsConfig.mutatorData,
                    sortName: name,
                    sortOrder: 'asc'
                } });
        }

        if (name === sortName && sortOrder !== 'desc') {
            setSortConfig({ name, direction: 'DESC' });
            return setParamsConfig({ ...paramsConfig,
                mutatorData: {
                    ...paramsConfig.mutatorData,
                    sortName: name,
                    sortOrder: 'desc'
                } });
        }

        setSortConfig();
        return setParamsConfig({ ...paramsConfig,
            mutatorData: {
                ...paramsConfig.mutatorData,
                sortName: '',
                sortOrder: ''
            } });
    };

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    return (
        <Table
            tableData={data?.models}
            columns={COLUMNS}
            multiSelect
            searchBarPlaceholder="Search Distributions..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            onSort={onSort}
            sortConfig={sortConfig}
            selected={selected}
            onSelected={onSelected}
            onPage={handlePagination}
            data={data}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => <DistributionTableActions data={data} readonly={readonly} selected={selected} setSelected={setSelected} />}
        />
    );
};

DistributionTable.propTypes = {
    readonly: PropTypes.bool
};

export default DistributionTable;
