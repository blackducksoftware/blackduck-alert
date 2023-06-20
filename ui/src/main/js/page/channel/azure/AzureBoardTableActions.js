import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import Button from 'common/component/button/Button';
import AzureBoardModal from 'page/channel/azure/AzureBoardModal';
import AzureBoardDeleteModal from 'page/channel/azure/AzureBoardDeleteModal';
import { fetchAzure } from 'store/actions/azure';
import { useDispatch } from 'react-redux';

const AzureBoardTableActions = ({ data, readonly, allowDelete, selected, setSelected }) => {
    const modalOptions = {
        type: 'CREATE',
        submitText: 'Create',
        title: 'Create Azure Board Connection'
    };

    const dispatch = useDispatch();
    const [refreshDisabled, setRefreshDisabled] = useState(false);
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
        setRefreshDisabled(true);
        setTimeout(() => setRefreshDisabled(false), 1000);
        dispatch(fetchAzure());
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

            <Button onClick={handleRefresh} type="button" text="Refresh" isDisabled={refreshDisabled} />

            {showCreateModal && (
                <AzureBoardModal
                    readonly={readonly}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully added 1 new Azure Board connection."
                />
            )}

            { showDeleteModal && (
                <AzureBoardDeleteModal
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

AzureBoardTableActions.propTypes = {
    data: PropTypes.object,
    selected: PropTypes.array,
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool,
    setSelected: PropTypes.func
};

export default AzureBoardTableActions;
