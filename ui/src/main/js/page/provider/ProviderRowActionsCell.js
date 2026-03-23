import React, { useState } from 'react';
import PropTypes from 'prop-types';
import ProviderModal from 'page/provider/ProviderModal';
import ProviderDeleteModal from 'page/provider/ProviderDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from '../../common/component/table/cell/RowActionsCell';

const ProviderRowActionsCell = ({ data, settings }) => {
    const [showCopyModal, setShowCopyModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();

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

    function handleClick(type) {
        setStatusMessage();
        if (type === 'edit') {
            setShowEditModal(true);
            setSelectedData(data);
        } else if (type === 'copy') {
            setShowCopyModal(true);
            setSelectedData((rowData) => ({
                ...rowData,
                id: null,
                name: '',
                createdAt: null,
                lastUpdated: null
            }));
        }
    }

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
        setSelectedData(data);
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
                <Dropdown.Item as="button" onClick={handleEditClick} disabled={settings.readonly}>
                    Edit
                </Dropdown.Item>
                <Dropdown.Item onClick={handleCopyClick} disabled={settings.readonly}>
                    Copy
                </Dropdown.Item>
                <Dropdown.Divider />
                <Dropdown.Item onClick={handleDeleteClick} disabled={settings.readonly}>
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
                    readonly={settings.readonly}
                />
            )}

            { showEditModal && (
                <ProviderModal
                    data={selectedData}
                    isOpen={showEditModal}
                    toggleModal={setShowEditModal}
                    modalOptions={editModalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage={editModalOptions.successMessage}
                    readonly={settings.readonly}
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
