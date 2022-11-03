import React, { useEffect, useState } from 'react';
// import PropTypes from 'prop-types';
// import { useDispatch, useSelector } from 'react-redux';
// import { fetchRoles } from 'store/actions/roles';
import GithubTableActions from 'page/channel/github/GithubTableActions';
import TimeStampCell from 'common/component/table/cell/TimeStampCell';
// import RoleEditRowAction from 'page/user/roles/RoleEditRowAction';
import Table from 'common/component/table/Table';
import { fetchGithub } from 'store/actions/github';





// const COLUMNS = [{
//     key: 'roleName',
//     label: 'Name',
//     sortable: true
// }, {
//     key: 'editRole',
//     label: 'Edit',
//     sortable: false,
//     customCell: RoleEditRowAction,
//     settings: { alignment: 'center' }
// }, {
//     key: 'copyRole',
//     label: 'Copy',
//     sortable: false,
//     customCell: RoleCopyRowAction,
//     settings: { alignment: 'center' }
// }]

const COLUMNS = [{
    key: 'name',
    label: 'Github Username',
    sortable: true
}, {
    key: 'createdAt',
    label: 'Created',
    sortable: false,
    customCell: TimeStampCell
}, {
    key: 'lastUpdated',
    label: 'Last Updated',
    sortable: false,
    customCell: TimeStampCell
}]

const GithubTable = ({ data }) => {

    const [search, setNewSearch] = useState("");
    const [selected, setSelected] = useState([]);
    const [autoRefresh, setAutoRefresh] = useState(false);


    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchGithub()), 30000);
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
                tableData={data.models}
                columns={COLUMNS}
                multiSelect
                selected={selected}
                onSelected={onSelected}
                searchBarPlaceholder="Search Github Providers..."
                handleSearchChange={handleSearchChange}
                active={autoRefresh} 
                onToggle={handleToggle}
                tableActions={() => <GithubTableActions data={data.models} selected={selected}/>}
            />
        </>

    )
}

export default GithubTable;