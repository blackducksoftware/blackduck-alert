import React, { useState } from 'react';
import PropTypes from 'prop-types';
import ProviderModal from 'page/provider/ProviderModal';
import ProviderDeleteModal from 'page/provider/ProviderDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const ProviderRowActionsCell = ({ data, settings }) => {
    const [showCopyModal, setShowCopyModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const { readonly } = settings;

    const copyModalOptions = {
        type: 'COPY',
        submitText: 'Save',
        title: 'Copy Provider',
        copyDescription: `Performing this action will create a new provider by using the same settings as '${data.name}'`
    };

    const editModalOptions = {
        type: 'EDIT',
        submitText: 'Save Edit',
        title: 'Edit Provider',
        successMessage: `Successfully updated ${data.name}`
    };

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
    }

    function handleCopyClick() {
        setStatusMessage();
        setShowCopyModal(true);
        setSelectedData((rowData) => ({
            ...rowData,
            id: null,
            name: '',
            createdAt: null,
            lastUpdated: null
        }));
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
                <Dropdown.Item as="button" onClick={handleDeleteClick} disabled={readonly}>
                    Delete
                </Dropdown.Item>
            </RowActionsCell>

            { showCopyModal && (
                <ProviderModal
                    data={selectedData}
                    isOpen={showCopyModal}
                    toggleModal={setShowCopyModal}
                    modalOptions={copyModalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully created 1 new provider."
                    readonly={readonly}
                />
            )}

            { showEditModal && (
                <ProviderModal
                    data={data}
                    isOpen={showEditModal}
                    toggleModal={setShowEditModal}
                    modalOptions={editModalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage={editModalOptions.successMessage}
                    readonly={readonly}
                />
            )}

            { showDeleteModal && (
                <ProviderDeleteModal
                    data={[data]}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={[data.id]}
                    setStatusMessage={setStatusMessage}
                    setSelected={() => console.log('setting')}
                />
            )}
        </>

    );
};

ProviderRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool
    })
};

export default ProviderRowActionsCell;
