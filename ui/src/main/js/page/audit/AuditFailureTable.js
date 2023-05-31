import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Table from 'common/component/table/Table';
import CreatedAtCell from 'page/audit/CreatedAtCell';
import JobsCountCell from 'page/audit/JobsCountCell';
import NotificationCell from 'page/audit/NotificationCell';
import ProviderCell from 'page/audit/ProviderCell';
import RefreshFailureCell from 'page/audit/RefreshFailureCell';
import ViewFailureCell from 'page/audit/ViewFailureCell';
import { fetchAuditData } from 'store/actions/audit';

const emptyTableConfig = {
    message: 'There are no records to display for this table.'
};

const AuditFailureTable = () => {
    const dispatch = useDispatch();
    const { data } = useSelector((state) => state.audit);
    const refreshStatus = JSON.parse(window.localStorage.getItem('AUDIT_FAILURE_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [sortConfig, setSortConfig] = useState();
    const [paramsConfig, setParamsConfig] = useState({
        pageNumber: data?.pageNumber || 0,
        pageSize: data?.pageSize || 10,
        mutatorData: {
            searchTerm: data?.mutatorData?.searchTerm || '',
            sortName: data?.mutatorData?.name || '',
            sortOrder: data?.mutatorData?.direction || ''
        }
    });

    useEffect(() => {
        dispatch(fetchAuditData(paramsConfig));
    }, [paramsConfig]);

    useEffect(() => {
        localStorage.setItem('AUDIT_FAILURE_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchAuditData()), 30000);
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

    const COLUMNS = [{
        key: 'providerType',
        label: 'Provider',
        sortable: true,
        customCell: ProviderCell
    }, {
        key: 'notificationType',
        label: 'Notification Type',
        sortable: true,
        customCell: NotificationCell
    }, {
        key: 'createdAt',
        label: 'Time Retrieved',
        sortable: true,
        customCell: CreatedAtCell
    }, {
        key: 'lastSent',
        label: 'Last Sent',
        sortable: true
    }, {
        key: 'jobsCount',
        label: 'Failed Jobs',
        sortable: false,
        customCell: JobsCountCell,
        settings: { alignment: 'center' }
    }, {
        key: 'viewFailure',
        label: 'View',
        sortable: false,
        customCell: ViewFailureCell,
        settings: { alignment: 'center' }
    }, {
        key: 'refreshNotification',
        label: 'Refresh',
        sortable: false,
        customCell: RefreshFailureCell,
        settings: {
            alignment: 'center',
            params: paramsConfig,
            type: 'notification'
        }
    }];

    return (
        <Table
            tableData={data?.models}
            columns={COLUMNS}
            searchBarPlaceholder="Search Audit Failures..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            onSort={onSort}
            sortConfig={sortConfig}
            onPage={handlePagination}
            data={data}
            emptyTableConfig={emptyTableConfig}
        />
    );
};

export default AuditFailureTable;
