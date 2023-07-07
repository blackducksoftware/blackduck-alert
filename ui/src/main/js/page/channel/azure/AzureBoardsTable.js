import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import AzureBoardsTableActions from 'page/channel/azure/AzureBoardsTableActions';
import AzureBoardsEditCell from 'page/channel/azure/AzureBoardsEditCell';
import AzureBoardsCopyCell from 'page/channel/azure/AzureBoardsCopyCell';
import AzureBoardsModal from 'page/channel/azure/AzureBoardsModal';
import StatusMessage from 'common/component/StatusMessage';
import { useLocation } from 'react-router-dom';
import { fetchAzureBoards } from 'store/actions/azure-boards';

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
    customCell: AzureBoardsEditCell,
    settings: { alignment: 'center' }
}, {
    key: 'copyAzureBoard',
    label: 'Copy',
    sortable: false,
    customCell: AzureBoardsCopyCell,
    settings: { alignment: 'center' }
}];

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please create an Azure Board connection to use this table.'
};

const AzureBoardsTable = ({ readonly, allowDelete }) => {
    const location = useLocation();
    const dispatch = useDispatch();
    const { data } = useSelector((state) => state.azureBoards);
    const refreshStatus = JSON.parse(window.localStorage.getItem('AZURE_BOARDS_REFRESH_STATUS'));
    const [autoRefresh, setAutoRefresh] = useState(refreshStatus);
    const [selected, setSelected] = useState([]);
    const [sortConfig, setSortConfig] = useState();
    const [paramsConfig, setParamsConfig] = useState({
        pageNumber: data?.pageNumber || 0,
        pageSize: data?.pageSize,
        mutatorData: {
            searchTerm: data?.mutatorData?.searchTerm,
            sortName: data?.mutatorData?.name,
            sortOrder: data?.mutatorData?.direction
        }
    });
    const [showModal, setShowModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();
    const [modalData, setModalData] = useState();

    useEffect(() => {
        // If a user authenticates via OAuth, OAuth will redirect us back to Alert with a url path similar to the following: 
        // `alert/channels/azure_boards/edit/{id}`.  If an ID is present as well as the string 'edit', we can assert that 
        // the modal should reopen with the data relevant to that ID.

        // split the url to determine if edit is present
        const parsedUrlArray = location.pathname.split('/');

        if (parsedUrlArray.includes('edit')) {
            // obtain the id of the azureBoards board that OAuth just authenticated
            const modalDataID = parsedUrlArray.slice(-1)[0];
            // filter the table data and set the data for the modal to the one that matches the id from the line above
            data.models.forEach(model => {
                if (model.id === modalDataID) {
                    setModalData(model);
                    setShowModal(true);
                }
            })
        }
    }, [location, data]);

    useEffect(() => {
        dispatch(fetchAzureBoards(paramsConfig));
    }, [paramsConfig]);

    useEffect(() => {
        localStorage.setItem('AZURE_BOARDS_REFRESH_STATUS', JSON.stringify(autoRefresh));

        if (autoRefresh) {
            const refreshIntervalId = setInterval(() => dispatch(fetchAzureBoards()), 30000);
            return function clearRefreshInterval() {
                clearInterval(refreshIntervalId);
            };
        }

        return undefined;
    }, [autoRefresh]);

    const handleSearchChange = (searchValue) => {
        setParamsConfig({
            ...paramsConfig,
            mutatorData: {
                ...paramsConfig.mutatorData,
                searchTerm: searchValue
            }
        });
    };

    function handleToggle() {
        setAutoRefresh(!autoRefresh);
    }

    function handlePagination(page) {
        setParamsConfig({ ...paramsConfig, pageNumber: page });
    }

    function handlePageSize(count) {
        setParamsConfig({ ...paramsConfig, pageSize: count });
    }

    const onSort = (name) => {
        const { sortName, sortOrder } = paramsConfig.mutatorData;
        if (name !== sortName) {
            setSortConfig({ name, direction: 'ASC' });
            return setParamsConfig({ ...paramsConfig,
                mutatorData: {
                    ...paramsConfig.mutatorData,
                    sortName: name,
                    sortOrder: 'asc'
                } });
        }

        if (name === sortName && sortOrder !== 'desc') {
            setSortConfig({ name, direction: 'DESC' });
            return setParamsConfig({ ...paramsConfig,
                mutatorData: {
                    ...paramsConfig.mutatorData,
                    sortName: name,
                    sortOrder: 'desc'
                } });
        }

        setSortConfig();
        return setParamsConfig({ ...paramsConfig,
            mutatorData: {
                ...paramsConfig.mutatorData,
                sortName: '',
                sortOrder: ''
            } });
    };

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    return (
        <>
            <Table
                tableData={data?.models}
                columns={COLUMNS}
                multiSelect
                searchBarPlaceholder="Search Azure Boards..."
                handleSearchChange={handleSearchChange}
                active={autoRefresh}
                onToggle={handleToggle}
                onSort={onSort}
                sortConfig={sortConfig}
                selected={selected}
                onSelected={onSelected}
                onPage={handlePagination}
                onPageSize={handlePageSize}
                showPageSize
                data={data}
                emptyTableConfig={emptyTableConfig}
                tableActions={() => 
                    <AzureBoardsTableActions
                        data={data}
                        readonly={readonly}
                        allowDelete={allowDelete}
                        selected={selected}
                        setSelected={setSelected}
                        paramsConfig={paramsConfig}
                    />
                }
            />
            {statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            {showModal && (
                <AzureBoardsModal
                    data={modalData}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={{
                        type: 'EDIT',
                        title: 'Edit Azure Board',
                        submitText: 'Save',
                        openedAfterOAuthHandshake: true
                    }}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Azure Board."
                />
            )}
        </>
    );
};

AzureBoardsTable.propTypes = {
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool
};
export default AzureBoardsTable;
