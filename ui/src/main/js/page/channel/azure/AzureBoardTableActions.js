import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import AzureBoardDeleteModal from 'page/channel/azure/AzureBoardDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import AzureBoardModal from './AzureBoardModal';

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

const AzureBoardTableActions = ({ data, readonly, allowDelete, selected }) => {
    const classes = useStyles();
    const modalOptions = {
        type: 'CREATE',
        submitText: 'Create',
        title: 'Create Azure Board Connection'
    };

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateUserClick() {
        setStatusMessage();
        setShowCreateModal(true);
    }

    function handleDeleteUserClick() {
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

            <button className={classes.createUserBtn} onClick={handleCreateUserClick} type="button">
                <FontAwesomeIcon icon="plus" />
                Create Azure Board
            </button>

            { allowDelete && (
                <button className={classes.deleteUserBtn} onClick={handleDeleteUserClick} disabled={selected.length === 0} type="button">
                    <FontAwesomeIcon icon="trash" />
                    Delete
                </button>
            )}

            { showCreateModal && (
                <AzureBoardModal
                    readonly={readonly}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully added 1 new Azure Board connection."
                />
            )}

            { showDeleteModal && (
                <AzureBoardDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
                />
            )}
        </>
    );
};

AzureBoardTableActions.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    data: PropTypes.arrayOf(PropTypes.object),
    selected: PropTypes.array
};

export default AzureBoardTableActions;
