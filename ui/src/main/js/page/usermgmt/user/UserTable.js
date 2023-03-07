import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUsers } from 'store/actions/users';
import UserCopyRowAction from 'page/usermgmt/user/UserCopyRowAction';
import UserEditRowAction from 'page/usermgmt/user/UserEditRowAction';
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
    customCell: UserEditRowAction,
    settings: { alignment: 'center' }
}, {
    key: 'copyUser',
    label: 'Copy',
    sortable: false,
    customCell: UserCopyRowAction,
    settings: { alignment: 'center' }
}]

const UserTable = ({ canCreate, canDelete }) => {
    const dispatch = useDispatch();
    const [tableData, setTableData] = useState();
    const [search, setNewSearch] = useState("");
    const [selected, setSelected] = useState([]);
    const [autoRefresh, setAutoRefresh] = useState(false);
    const [sortConfig, setSortConfig] = useState();
    const users = useSelector(state => state.users.data);

    useEffect(() => {
        dispatch(fetchUsers());
    }, []);

    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchUsers()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            }
        }
    }, [autoRefresh])

    const onSelected = selected => {
        setSelected(selected);
    };

    const handleSearchChange = (e) => {
        setNewSearch(e.target.value);
    }

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    const onSort = (name) => {
        if (name !== sortConfig?.name || !sortConfig) {
            return setSortConfig({name, direction: 'ASC'});
        }

        if (name === sortConfig?.name && sortConfig?.direction === 'DESC') {
            return setSortConfig();
        }

        if (name === sortConfig?.name) {
            return setSortConfig({name, direction: 'DESC'});
        }

        return setSortConfig();
    }
    
    useEffect(() => {
        let data = users;

        if (sortConfig) {
            const { name, direction } = sortConfig;
            data = [...data].sort((a, b) => {
                if (a[name] === null) return 1;
                if (b[name] === null) return -1;
                if (a[name] === null && b[name] === null) return 0;
                return (
                 a[name].toString().localeCompare(b[name].toString(), "en", {
                  numeric: true,
                 }) * (direction === 'ASC' ? 1 : -1)
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
            onSelected={onSelected}
            onSort={onSort}
            sortConfig={sortConfig}
            searchBarPlaceholder="Search Users..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh} 
            onToggle={handleToggle}
            tableActions={() => <UserTableActions canCreate={canCreate} canDelete={canDelete} data={tableData} selected={selected}/>}
        />
    )
}

UserTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

export default UserTable;