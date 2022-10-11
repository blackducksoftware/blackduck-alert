import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { fetchRoles } from 'store/actions/roles';
import RoleCopyRowAction from 'page/user/roles/RoleCopyRowAction';
import RoleEditRowAction from 'page/user/roles/RoleEditRowAction';
import Table from 'common/component/table/Table';
import RoleTableActions from 'page/user/roles/RoleTableActions';

const COLUMNS = [{
    key: 'roleName',
    label: 'Name',
    sortable: true
}, {
    key: 'editRole',
    label: 'Edit',
    sortable: false,
    customCell: RoleEditRowAction,
    settings: { alignment: 'center' }
}, {
    key: 'copyRole',
    label: 'Copy',
    sortable: false,
    customCell: RoleCopyRowAction,
    settings: { alignment: 'center' }
}]

const RolesTable = ({canCreate, canDelete}) => {
    const dispatch = useDispatch();
    const [search, setNewSearch] = useState("");
    const [selected, setSelected] = useState([]);
    const [autoRefresh, setAutoRefresh] = useState(false);
    const roles = useSelector(state => state.roles);

    useEffect(() => {
        dispatch(fetchRoles());
    }, []);

    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchRoles()), 30000);
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

    return (
        <>
            <Table 
                tableData={roles.data}
                columns={COLUMNS}
                multiSelect
                selected={selected}
                onSelected={onSelected}
                searchBarPlaceholder="Search Roles..."
                handleSearchChange={handleSearchChange}
                active={autoRefresh} 
                onToggle={handleToggle}
                tableActions={() => <RoleTableActions canCreate={canCreate} canDelete={canDelete} data={roles.data} selected={selected}/>}
            />
        </>
        
    )
}

export default RolesTable;