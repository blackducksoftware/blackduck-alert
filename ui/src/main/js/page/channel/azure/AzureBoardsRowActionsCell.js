import React, { useState } from 'react';
import PropTypes from 'prop-types';
import AzureBoardsModal from 'page/channel/azure/AzureBoardsModal';
import AzureBoardsDeleteModal from 'page/channel/azure/AzureBoardsDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const AzureBoardRowActionsCell = ({ data, settings }) => {
    const [showCopyModal, setShowCopyModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const { readonly, allowDelete, paramsConfig, setParamsConfig } = settings;
    const dataStagedForDelete = { models: [data] };

    const copyModalOptions = {
        type: 'COPY',
        title: 'Copy Azure Board',
        submitText: 'Save',
        copyDescription: `Performing this action will create a new Azure Board by using the same settings as '${data.name}'`
    };

    const editModalOptions = {
        type: 'EDIT',
        title: 'Edit Azure Board',
        submitText: 'Save'
    };

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
    }

    function handleCopyClick() {
        setStatusMessage();
        setShowCopyModal(true);
        setSelectedData({
            ...data,
            id: null,
            name: '',
            createdAt: null,
            lastUpdated: null
        });
    }

    function handleDeleteClick() {
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

            <RowActionsCell>
                <Dropdown.Item as="button" onClick={handleEditClick} disabled={readonly}>
                    Edit
                </Dropdown.Item>
                <Dropdown.Item as="button" onClick={handleCopyClick} disabled={readonly}>
                    Copy
                </Dropdown.Item>
                <Dropdown.Divider />
                <Dropdown.Item as="button" onClick={handleDeleteClick} disabled={readonly || !allowDelete}>
                    Delete
                </Dropdown.Item>
            </RowActionsCell>

            { showCopyModal && (
                <AzureBoardsModal
                    data={selectedData}
                    isOpen={showCopyModal}
                    toggleModal={setShowCopyModal}
                    modalOptions={copyModalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully created 1 Azure Board."
                    paramsConfig={paramsConfig}
                />
            )}

            { showEditModal && (
                <AzureBoardsModal
                    data={data}
                    isOpen={showEditModal}
                    toggleModal={setShowEditModal}
                    modalOptions={editModalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Azure Board."
                    paramsConfig={paramsConfig}
                />
            )}

            { showDeleteModal && (
                <AzureBoardsDeleteModal
                    data={dataStagedForDelete}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={[data.id]}
                    setStatusMessage={setStatusMessage}
                    paramsConfig={paramsConfig}
                    setParamsConfig={setParamsConfig}
                />
            )}
        </>

    );
};

AzureBoardRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool,
        allowDelete: PropTypes.bool,
        paramsConfig: PropTypes.object,
        setParamsConfig: PropTypes.func
    })
};

export default AzureBoardRowActionsCell;
