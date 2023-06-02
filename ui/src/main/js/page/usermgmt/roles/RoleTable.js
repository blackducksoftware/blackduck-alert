import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { fetchRoles } from 'store/actions/roles';
import Table from 'common/component/table/Table';
import RoleCopyCell from 'page/usermgmt/roles//RoleCopyCell';
import RoleEditCell from 'page/usermgmt/roles/RoleEditCell';
import RoleTableActions from 'page/usermgmt/roles/RoleTableActions';

const COLUMNS = [{
    key: 'roleName',
    label: 'Name',
    sortable: true
}, {
    key: 'editRole',
    label: 'Edit',
    sortable: false,
    customCell: RoleEditCell,
    settings: { alignment: 'center' }
}, {
    key: 'copyRole',
    label: 'Copy',
    sortable: false,
    customCell: RoleCopyCell,
    settings: { alignment: 'center' }
}];

const emptyTableConfig = {
    message: 'There are no records to display for this table. Please create a Role to use this table.'
};

const RoleTable = ({ canCreate, canDelete }) => {
    const dispatch = useDispatch();
    const refreshStatus = JSON.parse(window.localStorage.getItem('ROLE_MANAGEMENT_REFRESH_STATUS') || true);
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [tableData, setTableData] = useState();
    const [search, setNewSearch] = useState('');
    const [selected, setSelected] = useState([]);
    const [sortConfig, setSortConfig] = useState();
    const roles = useSelector((state) => state.roles.data);
    // Disable select options for users: sysadmin, jobmanager, alertuser
    const disableSelectOptions = {
        key: 'roleName',
        disabledItems: ['ALERT_ADMIN', 'ALERT_JOB_MANAGER', 'ALERT_USER'],
        title: 'System created role, unable to select for deletion.'
    };

    useEffect(() => {
        dispatch(fetchRoles());
    }, []);

    useEffect(() => {
        localStorage.setItem('ROLE_MANAGEMENT_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchRoles()), 30000);
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
        let data = roles;

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

        setTableData(!search ? data : data.filter((role) => role.roleName.toLowerCase().includes(search.toLowerCase())));
    }, [roles, search, sortConfig]);

    return (
        <>
            <Table
                tableData={tableData}
                columns={COLUMNS}
                multiSelect
                selected={selected}
                onSelected={onSelected}
                searchBarPlaceholder="Search Roles..."
                handleSearchChange={handleSearchChange}
                active={autoRefresh}
                onToggle={handleToggle}
                onSort={onSort}
                sortConfig={sortConfig}
                disableSelectOptions={disableSelectOptions}
                emptyTableConfig={emptyTableConfig}
                tableActions={() => <RoleTableActions canCreate={canCreate} canDelete={canDelete} data={roles} selected={selected} setSelected={setSelected} />}
            />
        </>
    );
};

RoleTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

export default RoleTable;
