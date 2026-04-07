import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch } from 'react-redux';
import { deleteCertificate, fetchCertificates } from 'store/actions/certificates';
import DeleteModal from 'common/component/modal/DeleteModal';

const DeleteCertificatesModal = ({ isOpen, toggleModal, data, selected, setSelected }) => {
    const dispatch = useDispatch();

    function getStagedForDelete() {
        const staged = data.filter((certificate) => selected.includes(certificate.id));
        return staged.map((certificate) => ({ ...certificate, staged: true }));
    }

    const [selectedCertificates, setSelectedCertificates] = useState(getStagedForDelete());
    const isMultiCertDelete = selectedCertificates.length > 1;

    useEffect(() => {
        setSelectedCertificates(getStagedForDelete());
    }, [selected]);

    function handleClose() {
        toggleModal(false);
        dispatch(fetchCertificates());
    }

    function handleDelete() {
        selectedCertificates.forEach((certificate) => {
            if (certificate.staged) {
                dispatch(deleteCertificate(certificate.id));
            }
        });
        setSelected([]);
        handleClose();
    }

    return (
        <DeleteModal
            isOpen={isOpen}
            title={isMultiCertDelete ? 'Delete Certificates' : 'Delete Certificate'}
            confirmationMessage={isMultiCertDelete ? 'Are you sure you want to delete these certificates?' : 'Are you sure you want to delete this certificate?'}
            onClose={handleClose}
            onDelete={handleDelete}
        />
    );
};

DeleteCertificatesModal.propTypes = {
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.arrayOf(PropTypes.string),
    data: PropTypes.arrayOf(PropTypes.shape({
        alias: PropTypes.string,
        certificateContent: PropTypes.string,
        lastUpdated: PropTypes.string,
        id: PropTypes.string
    })).isRequired,
    setSelected: PropTypes.func
};

export default DeleteCertificatesModal;
