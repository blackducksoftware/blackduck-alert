import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Table from 'common/component/table/Table';
import ViewTaskCell from 'page/task/ViewTaskCell';
import { fetchTasks } from 'store/actions/tasks';
import { fetchUsers } from 'store/actions/users';

const COLUMNS = [{
    key: 'type',
    label: 'Task Name',
    sortable: true
}, {
    key: 'nextRunTime',
    label: 'Next Run Time',
    sortable: true
}, {
    key: 'viewTask',
    label: 'View Task',
    customCell: ViewTaskCell,
    settings: { alignment: 'right' }
}];

const emptyTableConfig = {
    message: 'There are no records to display for this table.'
};

const TaskManagementTable = () => {
    const dispatch = useDispatch();
    const refreshStatus = JSON.parse(window.localStorage.getItem('TASK_MANAGEMENT_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [tableData, setTableData] = useState();
    const [search, setNewSearch] = useState('');
    const [sortConfig, setSortConfig] = useState();
    const tasks = useSelector((state) => state.tasks.data);

    useEffect(() => {
        dispatch(fetchTasks());
    }, []);

    useEffect(() => {
        localStorage.setItem('TASK_MANAGEMENT_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchUsers()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        return undefined;
    }, [autoRefresh]);

    const handleSearchChange = (e) => {
        setNewSearch(e.target.value);
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    const onSort = (name) => {
        if (name !== sortConfig?.name || !sortConfig) {
            return setSortConfig({ name, direction: 'ASC' });
        }

        if (name === sortConfig?.name && sortConfig?.direction === 'DESC') {
            return setSortConfig();
        }

        if (name === sortConfig?.name) {
            return setSortConfig({ name, direction: 'DESC' });
        }

        return setSortConfig();
    };

    useEffect(() => {
        let data = tasks;

        if (sortConfig) {
            const { name, direction } = sortConfig;
            data = [...data].sort((a, b) => {
                if (a[name] === null) return 1;
                if (b[name] === null) return -1;
                if (a[name] === null && b[name] === null) return 0;
                return (
                    a[name].toString().localeCompare(b[name].toString(), 'en', { numeric: true }) * (direction === 'ASC' ? 1 : -1)
                );
            });
        }

        setTableData(!search ? data : data.filter((task) => task.type.toLowerCase().includes(search.toLowerCase())));
    }, [tasks, search, sortConfig]);

    return (
        <Table
            tableData={tableData}
            columns={COLUMNS}
            searchBarPlaceholder="Search Tasks..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            onSort={onSort}
            sortConfig={sortConfig}
            emptyTableConfig={emptyTableConfig}
        />
    );
};

export default TaskManagementTable;
