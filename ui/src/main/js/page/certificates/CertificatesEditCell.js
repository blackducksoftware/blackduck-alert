import React, { useState } from 'react';
import PropTypes from 'prop-types';
import CertificateModal from 'page/certificates/CertificateModal';
import IconButton from 'common/component/button/IconButton';

const CertificatesEditCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);
    const modalOptions = {
        submitText: 'Save',
        title: 'Edit Certificate'
    };

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            <IconButton icon="pencil-alt" onClick={() => handleClick()} />

            { showModal && (
                <CertificateModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    modalOptions={modalOptions}
                />
            )}
        </>
    );
};

CertificatesEditCell.propTypes = {
    data: PropTypes.shape({
        alias: PropTypes.string,
        certificateContent: PropTypes.string,
        lastUpdated: PropTypes.string,
        id: PropTypes.string
    })
};

export default CertificatesEditCell;
