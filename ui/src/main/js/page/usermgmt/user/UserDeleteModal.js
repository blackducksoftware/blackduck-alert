import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { bulkDeleteUsers, fetchUsers } from 'store/actions/users';
import DeleteModal from 'common/component/modal/DeleteModal';

function getStagedForDelete(data, selected) {
    const staged = data.filter((user) => selected.includes(user.id));
    return staged.map((user) => ({ ...user, staged: true }));
}

const UserDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage }) => {
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
        dispatch(bulkDeleteUsers(selectedUsers.filter((user) => user.staged)));
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

            const stagedCount = selectedUsers.filter((user) => user.staged).length;
            if (stagedCount > 0) {
                const successMessage = isMultiUserDelete
                    ? `Successfully deleted ${stagedCount} users.`
                    : 'Successfully deleted 1 user.';

                setStatusMessage({
                    message: successMessage,
                    type: 'success'
                });
            }
            setSelected?.([]);
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

    return (
        <DeleteModal
            isOpen={isOpen}
            title={isMultiUserDelete ? 'Delete Users' : 'Delete User'}
            confirmationMessage={isMultiUserDelete ? 'Are you sure you want to delete these users?' : 'Are you sure you want to delete this user?'}
            onClose={handleClose}
            onDelete={handleDelete}
            isLoading={showLoader}
        />
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
