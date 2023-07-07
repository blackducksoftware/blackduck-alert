import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import Button from 'common/component/button/Button';
import AzureBoardsModal from 'page/channel/azure/AzureBoardsModal';
import AzureBoardsDeleteModal from 'page/channel/azure/AzureBoardsDeleteModal';
import { fetchAzureBoards } from 'store/actions/azure-boards';
import { useDispatch, useSelector } from 'react-redux';

const AzureBoardsTableActions = ({ data, readonly, allowDelete, selected, setSelected, paramsConfig }) => {
    const modalOptions = {
        type: 'CREATE',
        submitText: 'Create',
        title: 'Create Azure Boards Connection'
    };

    const dispatch = useDispatch();
    const { fetching } = useSelector((state) => state.azureBoards);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateAzureBoardClick() {
        setStatusMessage();
        setShowCreateModal(true);
    }

    function handleDeleteAzureBoardClick() {
        setStatusMessage();
        setShowDeleteModal(true);
    }

    function handleRefresh() {
        dispatch(fetchAzureBoards(paramsConfig));
    }

    return (
        <>
            {statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <Button onClick={handleCreateAzureBoardClick} type="button" icon="plus" text="Create Azure Board" />

            {allowDelete && (
                <Button
                    onClick={handleDeleteAzureBoardClick}
                    isDisabled={selected.length === 0}
                    type="button"
                    icon="trash"
                    text="Delete"
                    buttonStyle="delete"
                />
            )}

            <Button onClick={handleRefresh} type="button" text="Refresh" isDisabled={fetching} showLoader={fetching} />

            {showCreateModal && (
                <AzureBoardsModal
                    readonly={readonly}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully added 1 new Azure Board connection."
                />
            )}

            { showDeleteModal && (
                <AzureBoardsDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
                    setSelected={setSelected}
                />
            )}
        </>
    );
};

AzureBoardsTableActions.propTypes = {
    data: PropTypes.object,
    selected: PropTypes.array,
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool,
    setSelected: PropTypes.func,
    paramsConfig: PropTypes.object
};

export default AzureBoardsTableActions;
