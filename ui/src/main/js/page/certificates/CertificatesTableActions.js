import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import CertificateModal from 'page/certificates/CertificateModal';
import DeleteCertificatesModal from 'page/certificates/DeleteCertificatesModal';

const useStyles = createUseStyles({
    createUserBtn: {
        background: 'none',
        border: 'solid .5px',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        borderRadius: '6px',
        fontSize: '14px',
        backgroundColor: '#2E3B4E',
        color: 'white',
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        }
    },
    deleteUserBtn: {
        background: 'none',
        border: 'solid .5px',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        borderRadius: '6px',
        fontSize: '14px',
        backgroundColor: '#E03C31',
        color: 'white',
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        },
        '&:disabled': {
            border: ['1px', 'solid', '#D9D9D9'],
            backgroundColor: '#D9D9D9',
            color: '#666666',
            cursor: 'not-allowed'
        }
    }
});

const CertificatesTableActions = ({ data, selected }) => {
    const classes = useStyles();

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);

    function handleCreateUserClick() {
        setShowCreateModal(true);
    }

    function handleDeleteUserClick() {
        setShowDeleteModal(true);
    }

    return (
        <>
            <button className={classes.createUserBtn} onClick={handleCreateUserClick} type="button">
                <FontAwesomeIcon icon="plus" />
                Create Certificate
            </button>

            <button className={classes.deleteUserBtn} onClick={handleDeleteUserClick} disabled={selected.length === 0} type="button">
                <FontAwesomeIcon icon="trash" />
                Delete
            </button>

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
