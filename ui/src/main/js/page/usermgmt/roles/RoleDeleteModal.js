import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { deleteRoleList, fetchRoles } from 'store/actions/roles';
import Modal from 'common/component/modal/Modal';

const useStyles = createUseStyles((theme) => ({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    },
    roleCard: {
        display: 'flex',
        border: `solid 1px ${theme.colors.grey.lightGrey}`,
        borderRadius: '5px',
        backgroundColor: theme.colors.grey.lighterGrey,
        padding: '8px',
        margin: [0, '50px', '10px', '20px'],
        width: '250px'
    },
    roleIcon: {
        flexBasis: '20%',
        backgroundColor: theme.colors.white.default,
        border: `solid 1px ${theme.colors.grey.lightGrey}`,
        borderRadius: '5px',
        height: '50px',
        paddingTop: '5px',
        textAlign: 'center'
    },
    roleInfo: {
        flexGrow: 1,
        paddingLeft: '15px',
        margin: 'auto'
    }
}));

const RoleDeleteModal = ({ isOpen, toggleModal, data, selected, setStatusMessage }) => {
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
                            <div className={classes.roleCard}>
                                <div className={classes.roleIcon}>
                                    <FontAwesomeIcon icon="user-cog" size="3x" />
                                </div>
                                <div className={classes.roleInfo}>
                                    <div style={{ fontSize: '16px' }}>{role.roleName}</div>
                                </div>
                            </div>
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
    setStatusMessage: PropTypes.func
};

export default RoleDeleteModal;
