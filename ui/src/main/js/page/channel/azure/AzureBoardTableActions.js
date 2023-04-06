import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import AzureBoardModal from './AzureBoardModal';
import AzureBoardDeleteModal from 'page/channel/azure/AzureBoardDeleteModal';
import CreateButton from '../../../common/component/button/CreateButton';
import DeleteButton from '../../../common/component/button/DeleteButton';

const AzureBoardTableActions = ({ data, readonly, allowDelete, selected }) => {
    const modalOptions = {
        type: 'CREATE',
        submitText: 'Create',
        title: 'Create Azure Board Connection'
    };

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
    return (
        <>
            { statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            <CreateButton onClick={handleCreateAzureBoardClick} type="button" icon="plus" text="Create Azure Board" />

            { allowDelete && (
                <DeleteButton onClick={handleDeleteAzureBoardClick} isDisabled={selected.length === 0} type="button" icon="trash" text="Delete" />
            )}

            { showCreateModal && (
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
                />
            )}
        </>
    );
};

AzureBoardTableActions.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    data: PropTypes.object,
    selected: PropTypes.array
};

export default AzureBoardTableActions;
