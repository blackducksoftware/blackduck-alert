import React, { useState } from 'react';
import PropTypes from 'prop-types';
import CertificateModal from 'page/certificates/CertificateModal';
import DeleteCertificatesModal from 'page/certificates/DeleteCertificatesModal';
import CreateButton from 'common/component/button/CreateButton';
import DeleteButton from 'common/component/button/DeleteButton';

const CertificatesTableActions = ({ data, selected }) => {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    const modalOptions = {
        submitText: 'Save',
        title: 'Create Certificate'
    };

    function handleCreateCertificateClick() {
        setShowCreateModal(true);
    }

    function handleDeleteCertificateClick() {
        setShowDeleteModal(true);
    }

    return (
        <>
            <CreateButton onClick={handleCreateCertificateClick} type="button" icon="plus" text="Create Certificate" />
            <DeleteButton onClick={handleDeleteCertificateClick} isDisabled={selected.length === 0} icon="trash" text="Delete" />

            { showCreateModal && (
                <CertificateModal
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully added 1 new Certificate."
                />
            )}

            { showDeleteModal && (
                <DeleteCertificatesModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                />
            )}

        </>

    );
};

CertificatesTableActions.propTypes = {
    selected: PropTypes.array,
    data: PropTypes.arrayOf(PropTypes.shape({
        alias: PropTypes.string,
        certificateContent: PropTypes.string,
        lastUpdated: PropTypes.string,
        id: PropTypes.string
    }))
};

export default CertificatesTableActions;
