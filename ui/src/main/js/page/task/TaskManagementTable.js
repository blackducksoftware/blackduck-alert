import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import Table from 'common/component/table/Table';
import ViewTaskCell from 'page/task/ViewTaskCell';
import { fetchTasks } from 'store/actions/tasks';

const COLUMNS = [{
    key: 'type',
    label: 'Task Name',
    sortable: true
}, {
    key: 'nextRunTime',
    label: 'Next Run Time',
    sortable: false
}, {
    key: 'viewTask',
    label: 'View Task',
    customCell: ViewTaskCell,
    settings: { alignment: 'right' }
}];

const TaskManagementTable = () => {
    const dispatch = useDispatch();
    const [search, setNewSearch] = useState("");
    const [autoRefresh, setAutoRefresh] = useState(false);
    const tasks = useSelector(state => state.tasks.data);
    
    useEffect(() => {
        dispatch(fetchTasks())
    }, []);

    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchUsers()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            }
        }
    }, [autoRefresh])

    const handleSearchChange = (e) => {
        setNewSearch(e.target.value);
    }

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    const getTasks = () => {
        return (!search ? tasks : tasks.filter((task) => task.type.toLowerCase().includes(search.toLowerCase())));
    }

    return (
        <Table 
            tableData={getTasks()}
            columns={COLUMNS}
            searchBarPlaceholder="Search Tasks..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
        />
    )
}

export default TaskManagementTable;