import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import PropTypes from 'prop-types';
import { deleteRoleList, fetchRoles } from 'store/actions/roles';
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

const RoleDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage }) => {
    const classes = useStyles();
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

        dispatch(deleteRoleList(selectedDeleteIds));
    }

    useEffect(() => {
        if (deleteStatus === 'PROCESSING') {
            setShowLoader(true);
        }

        if (deleteStatus === 'SUCCESS') {
            setShowLoader(false);

            const successMessage = isMultiRoleDelete
                ? `Successfully deleted ${selectedRoles.length} roles.`
                : 'Successfully deleted 1 role.';

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
        const toggledRoles = selectedRoles.map((role) => {
            if (role.id === selection.id) {
                return { ...role, staged: !role.staged };
            }
            return role;
        });

        setSelectedRoles(toggledRoles);
    }

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="sm"
                title={isMultiRoleDelete ? 'Delete Roles' : 'Delete Role'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
                showLoader={showLoader}
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiRoleDelete ? 'Are you sure you want to delete these roles?' : 'Are you sure you want to delete this role?' }
                </div>
                <div>
                    { selectedRoles?.map((role) => (
                        <div className={classes.cardContainer}>
                            <input type="checkbox" checked={role.staged} onChange={() => toggleSelect(role)} />
                            <Card icon="user-cog" label={role.roleName} />
                        </div>
                    )) }
                </div>
            </Modal>
        </>

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
