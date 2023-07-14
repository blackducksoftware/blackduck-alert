import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import AuthenticationTypeCell from 'page/channel/jira/server/AuthenticationTypeCell';
import JiraServerEditCell from 'page/channel/jira/server/JiraServerEditCell';
import JiraServerTableActions from 'page/channel/jira/server/JiraServerTableActions';
import JiraServerCopyCell from 'page/channel/jira/server/JiraServerCopyCell';
import { fetchJiraServer } from 'store/actions/jira-server';

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please create a Jira Server connection to use this table.'
};

const JiraServerTable = ({ readonly, allowDelete }) => {
    const dispatch = useDispatch();
    const { data } = useSelector((state) => state.jiraServer);
    const refreshStatus = JSON.parse(window.localStorage.getItem('JIRA_SERVER_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [selected, setSelected] = useState([]);
    const [sortConfig, setSortConfig] = useState();
    const [paramsConfig, setParamsConfig] = useState({
        pageNumber: data?.pageNumber || 0,
        pageSize: data?.pageSize || 10,
        mutatorData: {
            searchTerm: data?.mutatorData?.searchTerm,
            sortName: data?.mutatorData?.name,
            sortOrder: data?.mutatorData?.direction
        }
    });

    const COLUMNS = [{
        key: 'name',
        label: 'Name',
        sortable: true
    }, {
        key: 'url',
        label: 'Url',
        sortable: true
    }, {
        key: 'authorizationMethod',
        label: 'Authentication Type',
        sortable: true,
        customCell: AuthenticationTypeCell
    }, {
        key: 'createdAt',
        label: 'Created At',
        sortable: true
    }, {
        key: 'lastUpdated',
        label: 'Last Updated',
        sortable: true
    }, {
        key: 'editJiraServer',
        label: 'Edit',
        sortable: false,
        customCell: JiraServerEditCell,
        settings: { alignment: 'center', paramsConfig }
    }, {
        key: 'copyJiraServer',
        label: 'Copy',
        sortable: false,
        customCell: JiraServerCopyCell,
        settings: { alignment: 'center', paramsConfig }
    }];

    useEffect(() => {
        dispatch(fetchJiraServer(paramsConfig));
    }, [paramsConfig]);

    useEffect(() => {
        localStorage.setItem('JIRA_SERVER_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchJiraServer(paramsConfig)), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        return undefined;
    }, [autoRefresh, paramsConfig]);

    const handleSearchChange = (searchValue) => {
        setParamsConfig({
            ...paramsConfig,
            mutatorData: {
                ...paramsConfig.mutatorData,
                searchTerm: searchValue
            }
        });
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    function handlePagination(page) {
        setParamsConfig({ ...paramsConfig, pageNumber: page });
    }

    function handlePageSize(count) {
        setParamsConfig({ ...paramsConfig, pageSize: count, pageNumber: 0 });
    }

    const onSort = (name) => {
        const { sortName, sortOrder } = paramsConfig.mutatorData;
        if (name !== sortName) {
            setSortConfig({ name, direction: 'ASC' });
            return setParamsConfig({
                ...paramsConfig,
                mutatorData: {
                    ...paramsConfig.mutatorData,
                    sortName: name,
                    sortOrder: 'asc'
                }
            });
        }

        if (name === sortName && sortOrder !== 'desc') {
            setSortConfig({ name, direction: 'DESC' });
            return setParamsConfig({
                ...paramsConfig,
                mutatorData: {
                    ...paramsConfig.mutatorData,
                    sortName: name,
                    sortOrder: 'desc'
                }
            });
        }

        setSortConfig();
        return setParamsConfig({
            ...paramsConfig,
            mutatorData: {
                ...paramsConfig.mutatorData,
                sortName: '',
                sortOrder: ''
            }
        });
    };

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    return (
        <Table
            tableData={data?.models}
            columns={COLUMNS}
            multiSelect
            searchBarPlaceholder="Search Jira Server..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            onSort={onSort}
            sortConfig={sortConfig}
            selected={selected}
            onSelected={onSelected}
            onPage={handlePagination}
            onPageSize={handlePageSize}
            pageSize={data?.pageSize}
            showPageSize
            data={data}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => (
                <JiraServerTableActions
                    data={data}
                    readonly={readonly}
                    allowDelete={allowDelete}
                    selected={selected}
                    setSelected={setSelected}
                    paramsConfig={paramsConfig}
                    setParamsConfig={setParamsConfig}
                />
            )}
        />
    );
};

JiraServerTable.propTypes = {
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool
};

export default JiraServerTable;
