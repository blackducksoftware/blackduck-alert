import React, { useState } from 'react';
import PropTypes from 'prop-types';
import CertificateModal from 'page/certificates/CertificateModal';
import DeleteCertificatesModal from 'page/certificates/DeleteCertificatesModal';
import Button from 'common/component/button/Button';
import { fetchCertificates } from 'store/actions/certificates';
import { useDispatch, useSelector } from 'react-redux';

const CertificatesTableActions = ({ data, selected, setSelected }) => {
    const dispatch = useDispatch();
    const { fetching } = useSelector((state) => state.certificates);
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

    function handleRefresh() {
        dispatch(fetchCertificates());
    }

    return (
        <>
            <Button onClick={handleCreateCertificateClick} type="button" icon="plus" text="Create Certificate" />
            <Button onClick={handleDeleteCertificateClick} isDisabled={selected.length === 0} icon="trash" text="Delete" buttonStyle="delete" />
            <Button onClick={handleRefresh} type="button" text="Refresh" isDisabled={fetching} showLoader={fetching} />

            {showCreateModal && (
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
                    setSelected={setSelected}
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
    })),
    setSelected: PropTypes.func
};

export default CertificatesTableActions;
