import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchJiraServer } from '../../../../store/actions/jira-server';
import Table from 'common/component/table/Table';
import JiraServerEditCell from 'page/channel/jira/server/JiraServerEditCell';
import JiraServerTableActions from './JiraServerTableActions';
import JiraServerCopyCell from 'page/channel/jira/server/JiraServerCopyCell';

const COLUMNS = [{
    key: 'name',
    label: 'Name',
    sortable: true
}, {
    key: 'url',
    label: 'Url',
    sortable: true
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
    settings: { alignment: 'center' }
}, {
    key: 'copyJiraServer',
    label: 'Copy',
    sortable: false,
    customCell: JiraServerCopyCell,
    settings: { alignment: 'center' }
}];

const JiraServerTable = ({ readonly, allowDelete }) => {
    const dispatch = useDispatch();
    const { data } = useSelector((state) => state.jiraServer);
    const [autoRefresh, setAutoRefresh] = useState(false);
    const [selected, setSelected] = useState([]);
    const [paramsConfig, setParamsConfig] = useState({
        pageNumber: data?.pageNumber || 0,
        pageSize: data?.pageSize || 10,
        mutatorData: {
            searchTerm: data?.mutatorData?.searchTerm,
            sortName: data?.mutatorData?.name,
            sortOrder: data?.mutatorData?.direction
        }
    })

    useEffect(() => {
        dispatch(fetchJiraServer(paramsConfig));
    }, [paramsConfig]);

    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchJiraServer(paramsConfig)), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        return undefined;
    }, [autoRefresh]);

    const handleSearchChange = (e) => {
        setParamsConfig({...paramsConfig, mutatorData: {
            ...mutatorData,
            searchTerm: e.target.value
        }});
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    function handlePagination(page) {
        setParamsConfig({...paramsConfig, pageNumber: page});
    }

    const onSort = (name) => {
        const { sortName, sortOrder } = paramsConfig.mutatorData;
        if (name !== sortName) {
            return setParamsConfig({...paramsConfig, mutatorData: {
                ...mutatorData,
                sortName: name,
                sortOrder: 'asc'
            }});
        }

        if (name === sortName && sortOrder !== 'desc') {
            return setParamsConfig({...paramsConfig, mutatorData: {
                ...mutatorData,
                sortName: name,
                sortOrder: 'desc'
            }});
        }

        return setParamsConfig({...paramsConfig, mutatorData: {
            ...mutatorData,
            sortName: '',
            sortOrder: ''
        }});
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
            selected={selected}
            onSelected={onSelected}
            onPage={handlePagination}
            data={data}
            tableActions={() => <JiraServerTableActions data={data} readonly={readonly} allowDelete={allowDelete} selected={selected} />}
        />
    );
};

export default JiraServerTable;
