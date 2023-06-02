import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUsers } from 'store/actions/users';
import UserCopyCell from 'page/usermgmt/user/UserCopyCell';
import UserEditCell from 'page/usermgmt/user/UserEditCell';
import UserRoleCell from 'page/usermgmt/user/UserRoleCell';
import Table from 'common/component/table/Table';
import UserTableActions from 'page/usermgmt/user/UserTableActions';

const COLUMNS = [{
    key: 'username',
    label: 'Username',
    sortable: true
}, {
    key: 'emailAddress',
    label: 'Email',
    sortable: true
}, {
    key: 'authenticationType',
    label: 'Authentication Type',
    sortable: true
}, {
    key: 'roleNames',
    label: 'Roles',
    sortable: false,
    customCell: UserRoleCell
}, {
    key: 'editUser',
    label: 'Edit',
    sortable: false,
    customCell: UserEditCell,
    settings: { alignment: 'center' }
}, {
    key: 'copyUser',
    label: 'Copy',
    sortable: false,
    customCell: UserCopyCell,
    settings: { alignment: 'center' }
}];

const emptyTableConfig = {
    message: 'There are no records to display for this table. Please create a User to use this table.'
};

const UserTable = ({ canCreate, canDelete }) => {
    const dispatch = useDispatch();
    const refreshStatus = JSON.parse(window.localStorage.getItem('USER_MANAGEMENT_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [tableData, setTableData] = useState();
    const [search, setNewSearch] = useState('');
    const [selected, setSelected] = useState([]);
    const [sortConfig, setSortConfig] = useState();
    const users = useSelector((state) => state.users.data);
    // Disable select options for users: sysadmin, jobmanager, alertuser
    const disableSelectOptions = {
        key: 'username',
        disabledItems: ['sysadmin', 'jobmanager', 'alertuser'],
        title: 'System created user, unable to select for deletion.'
    };

    useEffect(() => {
        dispatch(fetchUsers());
    }, []);

    useEffect(() => {
        localStorage.setItem('USER_MANAGEMENT_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchUsers()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }
        return undefined;
    }, [autoRefresh]);

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

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
        let data = users;

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

        setTableData(!search ? data : data.filter((user) => user.username.toLowerCase().includes(search.toLowerCase())));
    }, [users, search, sortConfig]);

    return (
        <Table
            tableData={tableData}
            columns={COLUMNS}
            multiSelect
            selected={selected}
            disableSelectOptions={disableSelectOptions}
            onSelected={onSelected}
            onSort={onSort}
            sortConfig={sortConfig}
            searchBarPlaceholder="Search Users..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => <UserTableActions canCreate={canCreate} canDelete={canDelete} data={tableData} selected={selected} setSelected={setSelected} />}
        />
    );
};

UserTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

export default UserTable;
