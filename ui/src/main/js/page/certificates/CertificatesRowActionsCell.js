import React, { useState } from 'react';
import PropTypes from 'prop-types';
import CertificateModal from 'page/certificates/CertificateModal';
import DeleteCertificatesModal from 'page/certificates/DeleteCertificatesModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const CertificatesRowActionsCell = ({ data, settings }) => {
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();
    const { readOnly } = settings;

    const editModalOptions = {
        type: 'EDIT',
        title: 'Edit Certificate',
        submitText: 'Save'
    };

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
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
                <Dropdown.Item as="button" onClick={handleEditClick} disabled={readOnly}>
                    Edit
                </Dropdown.Item>
                <Dropdown.Divider />
                <Dropdown.Item as="button" onClick={handleDeleteClick} disabled={readOnly}>
                    Delete
                </Dropdown.Item>
            </RowActionsCell>

            { showEditModal && (
                <CertificateModal
                    data={data}
                    isOpen={showEditModal}
                    toggleModal={setShowEditModal}
                    modalOptions={editModalOptions}
                />
            )}

            { showDeleteModal && (
                <DeleteCertificatesModal
                    data={[data]}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={[data.id]}
                />
            )}
        </>

    );
};

CertificatesRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readOnly: PropTypes.bool
    })
};

export default CertificatesRowActionsCell;
