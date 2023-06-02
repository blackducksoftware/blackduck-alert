import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUsers, bulkDeleteUsers } from 'store/actions/users';
import Modal from 'common/component/modal/Modal';
import Card from 'common/component/Card';

const useStyles = createUseStyles({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    }
});

function getStagedForDelete(data, selected) {
    const staged = data.filter((user) => selected.includes(user.id));
    return staged.map((user) => ({ ...user, staged: true }));
}

const UserDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.users);
    const [selectedUsers, setSelectedUsers] = useState(getStagedForDelete(data, selected));
    const [showLoader, setShowLoader] = useState(false);
    const isMultiUserDelete = selectedUsers.length > 1;

    function handleClose() {
        dispatch(fetchUsers());
        toggleModal(false);
    }

    function handleDelete() {
        dispatch(bulkDeleteUsers(selectedUsers));
    }

    useEffect(() => {
        setSelectedUsers(getStagedForDelete(data, selected));
    }, [selected]);

    useEffect(() => {
        if (deleteStatus === 'DELETING') {
            setShowLoader(true);
        }

        if (deleteStatus === 'SUCCESS') {
            setShowLoader(false);

            const successMessage = isMultiUserDelete
                ? `Successfully deleted ${selectedUsers.length} users.`
                : 'Successfully deleted 1 user.';

            setStatusMessage({
                message: successMessage,
                type: 'success'
            });

            setSelected([]);
            handleClose();
        }

        if (deleteStatus === 'ERROR') {
            setShowLoader(false);
            setStatusMessage({
                message: error.fieldErrors.message,
                type: 'error'
            });
            handleClose();
        }
    }, [deleteStatus]);

    function toggleSelect(selection) {
        const toggledUsers = selectedUsers.map((user) => {
            if (user.id === selection.id) {
                return { ...user, staged: !user.staged };
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
                showLoader={showLoader}
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiUserDelete ? 'Are you sure you want to delete these users?' : 'Are you sure you want to delete this user?' }
                </div>
                <div>
                    { selectedUsers?.map((user) => (
                        <div className={classes.cardContainer} key={user.id}>
                            <input type="checkbox" checked={user.staged} onChange={() => toggleSelect(user)} />
                            <Card icon="user" label={user.username} description={user.emailAddress} />
                        </div>
                    ))}
                </div>
            </Modal>
        </>

    );
};

UserDeleteModal.propTypes = {
    data: PropTypes.array,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func,
    setSelected: PropTypes.func
};

export default UserDeleteModal;
