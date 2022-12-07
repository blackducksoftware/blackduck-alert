import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import CertificateModal from 'page/certificates/CertificateModal';

const useStyles = createUseStyles({
    editCell: {
        all: 'unset',
        cursor: 'pointer'
    }
});

const EditCertificateCell = ({ data }) => {
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            <button className={classes.editCell} onClick={() => handleClick()} type="button">
                <FontAwesomeIcon icon="pencil-alt" />
            </button>

            { showModal ? (
                <CertificateModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                    type="edit"
                />
            ) : null }
        </>
    );
};

EditCertificateCell.propTypes = {
    data: PropTypes.shape({
        alias: PropTypes.string,
        certificateContent: PropTypes.string,
        lastUpdated: PropTypes.string,
        id: PropTypes.string
    })
};

export default EditCertificateCell;
