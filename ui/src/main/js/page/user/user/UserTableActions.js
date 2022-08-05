import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserDeleteModal from 'page/user/user/UserDeleteModal';
import UserCreateModal from 'page/user/user/UserCreateModal';

const useStyles = createUseStyles({
    createUserBtn: {
        background: 'none',
        color: 'inherit',
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
        color: 'inherit',
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

const UserTableActions = ({ canCreate, canDelete, data, selected }) => {
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
            { canCreate ? (
                <button className={classes.createUserBtn} onClick={handleCreateUserClick}>
                    <FontAwesomeIcon icon="plus" />
                    Create User
                </button>
            ) : null }

            { canDelete ? (
                <button className={classes.deleteUserBtn} onClick={handleDeleteUserClick} disabled={selected.length === 0}>
                    <FontAwesomeIcon icon="trash" />
                    Delete
                </button>
            ) : null }

            { showCreateModal ? (
                <UserCreateModal 
                    data={data} 
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                />
            ) : null }
            
            { showDeleteModal ? (
                <UserDeleteModal 
                    data={data}
                    isOpen={showDeleteModal} 
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                />
            ) : null }
            
        </>

    );
};

export default UserTableActions;