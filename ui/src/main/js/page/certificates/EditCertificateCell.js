import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import CertificateModal from 'page/certificates/CertificateModal';

const useStyles = createUseStyles({
    editCell: {
        '&:hover': {
            cursor: 'pointer',
        }
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
            <span className={classes.editCell} onClick={() => handleClick()}>
                <FontAwesomeIcon icon="pencil-alt" />
            </span>

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

export default EditCertificateCell;