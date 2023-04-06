import React, { useState } from 'react';
import PropTypes from 'prop-types';
import CertificateModal from 'page/certificates/CertificateModal';
import DeleteCertificatesModal from 'page/certificates/DeleteCertificatesModal';
import CreateButton from 'common/component/button/CreateButton';
import DeleteButton from 'common/component/button/DeleteButton';

const CertificatesTableActions = ({ data, selected }) => {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);

    function handleCreateCertificateClick() {
        setShowCreateModal(true);
    }

    function handleDeleteCertificateClick() {
        setShowDeleteModal(true);
    }

    return (
        <>
            <CreateButton onClick={handleCreateCertificateClick} type="button" icon="plus" text="Create Certificate" />
           
            <DeleteButton onClick={handleDeleteCertificateClick} isDisabled={selected.length === 0} type="button" icon="trash">
                Delete
            </DeleteButton>

            { showCreateModal ? (
                <CertificateModal
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    type="create"
                />
            ) : null }

            { showDeleteModal ? (
                <DeleteCertificatesModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                />
            ) : null }

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
