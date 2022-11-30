import React, { useEffect, useState } from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { fetchCertificates, deleteCertificate } from 'store/actions/certificates';
import Modal from 'common/component/modal/Modal';

const useStyles = createUseStyles({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '14px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    },
    certCard: {
        display: 'flex',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        backgroundColor: '#e8e6e6',
        padding: '8px',
        margin: [0, '50px', '10px', '20px'],
        width: '250px'
    },
    certIcon: {
        flexBasis: '20%',
        backgroundColor: 'white',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        height: '50px',
        paddingTop: '5px',
        textAlign: 'center'
    },
    certInfo: {
        flexGrow: 1,
        alignSelf: 'center',
        margin: [0, '15px']
    }
});


const DeleteCertificatesModal = ({ isOpen, toggleModal, data, selected }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const [selectedCertificates, setSelectedCertificates] = useState(getStagedForDelete());
    const isMultiCertDelete = selectedCertificates.length > 1;

    function getStagedForDelete() {
        const staged = data.filter(certificate => selected.includes(certificate.id));
        return staged.map(certificate => ({ ...certificate, staged: true }));
    }

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
        handleClose();

    }

    function toggleSelect(selection) {
        const toggledCertificates = selectedCertificates.map((cert) => {
            if (cert.id === selection.id) {
                return {...cert, staged: !cert.staged}
            }
            return cert;
        });

        setSelectedCertificates(toggledCertificates);
    }

    return (
        <>
            <Modal 
                isOpen={isOpen} 
                size="sm" 
                title={isMultiCertDelete ? 'Delete Certificates' : 'Delete Certificate'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiCertDelete ? 'Are you sure you want to delete these certificates?' : 'Are you sure you want to delete this certificate?' }
                </div>
                <div>
                    { selectedCertificates?.map((cert) => {
                        return (
                            <div className={classes.cardContainer}>
                                <input type="checkbox" checked={cert.staged} onChange={() => toggleSelect(cert)}/>
                                <div className={classes.certCard}>
                                    <div className={classes.certIcon}>
                                        <FontAwesomeIcon icon="award" size="3x"/>
                                    </div>
                                    <div className={classes.certInfo}>
                                        <div style={{fontSize: '16px'}}>{cert.alias}</div>
                                    </div>
                                </div>
                            </div>
                        )
                    }) }
                </div>
            </Modal>
        </>

    );
};

export default DeleteCertificatesModal;