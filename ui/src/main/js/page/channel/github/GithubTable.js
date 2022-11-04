import React, { useEffect, useState } from 'react';
// import PropTypes from 'prop-types';
import GithubTableActions from 'page/channel/github/GithubTableActions';
import TimeStampCell from 'common/component/table/cell/TimeStampCell';
import Table from 'common/component/table/Table';
import { fetchGithub } from 'store/actions/github';
import TableLoader from 'common/component/loaders/TableLoader';
import GithubAddUserModal from 'page/channel/github/GithubAddUserModal';
import EditGithubRowAction from 'page/channel/github/EditGithubRowAction';

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
}, {
    key: 'editGithubPAT',
    label: 'Edit',
    sortable: false,
    customCell: EditGithubRowAction
}];

const GithubTable = ({ data }) => {

    const [search, setNewSearch] = useState("");
    const [selected, setSelected] = useState([]);
    const [autoRefresh, setAutoRefresh] = useState(false);
    const [showAddGithubModal, setShowAddGithubModal] = useState(false);


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

    function handleLoaderClick () {
        setShowAddGithubModal(true);
    }
    
    // Show Add Github Modal when the user clicks on the TableLoader button
    if (showAddGithubModal) { 
        return (
            <GithubAddUserModal isOpen={showAddGithubModal} toggleModal={setShowAddGithubModal} />
        )
    }

    // Show No Data screen when the user has not added data
    if (data.models.length === 0) {
        return (
            <TableLoader 
                icon={['fab', 'github']}  
                onClick={handleLoaderClick}
                buttonLabel="Connect Github" 
                description="You currently have no GitHub connections. Click above and connect your Github 
                account to recieve alerts for your repositories."
            />
        )
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