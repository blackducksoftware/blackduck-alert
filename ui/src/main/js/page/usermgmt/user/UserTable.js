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

const UserTableConvert = ({ canCreate, canDelete }) => {
    const dispatch = useDispatch();
    const [search, setNewSearch] = useState("");
    const [selected, setSelected] = useState([]);
    const [autoRefresh, setAutoRefresh] = useState(false);
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

    const getUsers = () => {
        return (!search ? users : users.filter((user) => user.username.toLowerCase().includes(search.toLowerCase())));
    }

    return (
        <Table 
            tableData={getUsers()}
            columns={COLUMNS}
            multiSelect
            selected={selected}
            onSelected={onSelected}
            searchBarPlaceholder="Search Users..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh} 
            onToggle={handleToggle}
            tableActions={() => <UserTableActions canCreate={canCreate} canDelete={canDelete} data={getUsers()} selected={selected}/>}
        />
    )
}

UserTableConvert.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

export default UserTableConvert;