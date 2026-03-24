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
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const { readOnly, setSelected } = settings;

    const editModalOptions = {
        type: 'EDIT',
        title: 'Edit Jira Server',
        submitText: 'Save'
    };

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
        setSelectedData(data);
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
                <Dropdown.Item onClick={handleDeleteClick} disabled={readOnly}>
                    Delete
                </Dropdown.Item>
            </RowActionsCell>

            { showEditModal && (
                <CertificateModal
                    data={selectedData}
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
                    setSelected={setSelected}
                />
            )}
        </>

    );
};

CertificatesRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool,
        paramsConfig: PropTypes.object,
        setParamsConfig: PropTypes.func,
        setSelected: PropTypes.func
    })
};

export default CertificatesRowActionsCell;
