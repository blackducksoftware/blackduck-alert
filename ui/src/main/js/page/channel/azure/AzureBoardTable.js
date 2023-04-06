import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Table from 'common/component/table/Table';
import AzureBoardTableActions from 'page/channel/azure/AzureBoardTableActions';
import { fetchAzure } from '../../../store/actions/azure';
import AzureEditCell from 'page/channel/azure/AzureEditCell';
import AzureCopyCell from 'page/channel/azure/AzureCopyCell';

const COLUMNS = [{
    key: 'name',
    label: 'Name',
    sortable: true
}, {
    key: 'organizationName',
    label: 'Organization Name',
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
    key: 'editAzureBoard',
    label: 'Edit',
    sortable: false,
    customCell: AzureEditCell,
    settings: { alignment: 'center' }
}, {
    key: 'copyAzureBoard',
    label: 'Copy',
    sortable: false,
    customCell: AzureCopyCell,
    settings: { alignment: 'center' }
}];

const AzureBoardTale = ({ readonly, allowDelete }) => {
    const dispatch = useDispatch();
    const { data } = useSelector((state) => state.azure);
    const [autoRefresh, setAutoRefresh] = useState(false);
    const [tableData, setTableData] = useState();
    const [selected, setSelected] = useState([]);
    const [pageNumber, setPageNumber] = useState(data?.currentPage);
    const [offset, setOffset] = useState(data?.pageSize);
    const [mutatorData, setMutatorData] = useState({
        searchTerm: data?.mutatorData?.searchTerm,
        sortName: data?.mutatorData?.name,
        sortOrder: data?.mutatorData?.direction
    });

    useEffect(() => {
        const params = {
            pageNumber,
            pageSize: offset,
            mutatorData
        };
        dispatch(fetchAzure(params));
        setTableData(data);
    }, [pageNumber, offset, mutatorData, data]);

    useEffect(() => {
        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchAzure()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        return undefined;
    }, [autoRefresh]);

    const handleSearchChange = (e) => {
        setMutatorData({...mutatorData, searchTerm: e.target.value})
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    const onSort = (name) => {
        if (name !== mutatorData.sortName) {
            return setMutatorData({...mutatorData, sortName: name, sortOrder: 'asc'})
        }

        if (name === mutatorData.sortName && mutatorData.sortOrder !== 'desc') {
            return setMutatorData({...mutatorData, sortName: name, sortOrder: 'desc'})
        }

        return setMutatorData({...mutatorData, sortName: '', sortOrder: ''})
    };

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    return (
        <Table
            tableData={tableData?.models}
            columns={COLUMNS}
            multiSelect
            searchBarPlaceholder="Search Tasks..."
            handleSearchChange={handleSearchChange}
            active={autoRefresh}
            onToggle={handleToggle}
            onSort={onSort}
            selected={selected}
            onSelected={onSelected}
            tableActions={() => <AzureBoardTableActions data={tableData} readonly={readonly} allowDelete={allowDelete} selected={selected} />}
        />
    );
};

export default AzureBoardTale;
