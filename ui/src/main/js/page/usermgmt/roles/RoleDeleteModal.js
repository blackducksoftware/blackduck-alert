import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { bulkDeleteRoles, fetchRoles } from 'store/actions/roles';
import DeleteModal from 'common/component/modal/DeleteModal';

const RoleDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage }) => {
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.roles);

    function getStagedForDelete() {
        const staged = data.filter((role) => selected.includes(role.id));
        return staged.map((role) => ({ ...role, staged: true }));
    }

    const [selectedRoles, setSelectedRoles] = useState(getStagedForDelete());
    const [showLoader, setShowLoader] = useState(false);
    const isMultiRoleDelete = selectedRoles.length > 1;

    useEffect(() => {
        setSelectedRoles(getStagedForDelete());
    }, [selected]);

    function handleClose() {
        dispatch(fetchRoles());
        toggleModal(false);
    }

    function handleDelete() {
        const selectedDeleteIds = [];
        selectedRoles.forEach((role) => {
            if (role.staged) {
                selectedDeleteIds.push(role.id);
            }

            return null;
        });
        dispatch(bulkDeleteRoles(selectedDeleteIds));
    }

    useEffect(() => {
        if (deleteStatus === 'PROCESSING') {
            setShowLoader(true);
        }

        if (deleteStatus === 'SUCCESS') {
            setShowLoader(false);

            const stagedCount = selectedRoles.filter((role) => role.staged).length;
            if (stagedCount > 0) {
                const successMessage = isMultiRoleDelete
                    ? `Successfully deleted ${stagedCount} roles.`
                    : 'Successfully deleted 1 role.';

                setStatusMessage({
                    message: successMessage,
                    type: 'success'
                });
            }
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

    return (
        <DeleteModal
            isOpen={isOpen}
            title={isMultiRoleDelete ? 'Delete Roles' : 'Delete Role'}
            confirmationMessage={isMultiRoleDelete ? 'Are you sure you want to delete these roles?' : 'Are you sure you want to delete this role?'}
            onClose={handleClose}
            onDelete={handleDelete}
            isLoading={showLoader}
        />
    );
};

RoleDeleteModal.propTypes = {
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    data: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string
    })),
    selected: PropTypes.arrayOf(PropTypes.string),
    setStatusMessage: PropTypes.func,
    setSelected: PropTypes.func
};

export default RoleDeleteModal;
