import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { useDispatch } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { deleteUser, fetchUsers } from 'store/actions/users';
import Modal from 'common/component/modal/Modal';

const useStyles = createUseStyles({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    },
    userCard: {
        display: 'flex',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        backgroundColor: '#e8e6e6',
        padding: '8px',
        margin: [0, '50px', '10px', '20px'],
        width: '250px'
    },
    userIcon: {
        flexBasis: '20%',
        backgroundColor: 'white',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        height: '50px',
        paddingTop: '5px',
        textAlign: 'center'
    },
    userInfo: {
        flexGrow: 1,
        padding: ['5px', 0, 0, '15px']
    }
});


const UserDeleteModal = ({ isOpen, toggleModal, data, selected }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const [selectedUsers, setSelectedUsers] = useState(getStagedForDelete());
    const isMultiUserDelete = selectedUsers.length > 1;

    function getStagedForDelete() {
        const staged = data.filter((user) => selected.includes(user.id));
        return staged.map((user) => ({ ...user, staged: true }));
    }

    useEffect(() => {
        setSelectedUsers(getStagedForDelete());
    }, [selected]);

    function handleClose() {
        toggleModal(false);

        dispatch(fetchUsers());
    }

    function handleDelete() {
        selectedUsers.forEach((user) => {
            if (user.staged) {
                dispatch(deleteUser(user.id));
            }
        });
        handleClose();
        
    }
    
    function toggleSelect(selection) {
        const toggledUsers = selectedUsers.map((user) => {
            if (user.id === selection.id) {
                return {...user, staged: !user.staged}
            }
            return user;
        });

        setSelectedUsers(toggledUsers);
    }

    return (
        <>
            <Modal 
                isOpen={isOpen} 
                size="sm" 
                title={isMultiUserDelete ? 'Delete Users' : 'Delete User'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiUserDelete ? 'Are you sure you want to delete these users?' : 'Are you sure you want to delete this user?' }
                </div>
                <div>
                    { selectedUsers?.map((user) => {
                        return (
                            <div className={classes.cardContainer}>
                                <input type="checkbox" checked={user.staged} onChange={() => toggleSelect(user)}/>
                                <div className={classes.userCard}>
                                    <div className={classes.userIcon}>
                                        <FontAwesomeIcon icon="user" size="3x"/>
                                    </div>
                                    <div className={classes.userInfo}>
                                        <div style={{fontSize: '16px'}}>{user.username}</div>
                                        <div>{user.emailAddress}</div>
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

UserDeleteModal.propTypes = {
    data: PropTypes.object,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array
};

export default UserDeleteModal;