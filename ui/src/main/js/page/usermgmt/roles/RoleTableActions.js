import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RoleDeleteModal from 'page/usermgmt/roles/RoleDeleteModal';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import StatusMessage from 'common/component/StatusMessage';
import { fetchRoles } from '../../../store/actions/roles';

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

const RoleTableActions = ({ canCreate, canDelete, data, selected }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.roles);

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);

    function handleCreateRole() {
        setShowCreateModal(true);
    }

    function handleDeleteRole() {
        setShowDeleteModal(true);
    }

    useEffect(() => {
        if (deleteStatus === 'SUCCESS') {
            dispatch(fetchRoles());
        }

        if (deleteStatus === 'FAIL') {
            setErrorMessage(error.message);
        }
    }, [deleteStatus]);

    useEffect(() => {
        setErrorMessage(null);
    }, [selected]);

    return (
        <>
            {errorMessage ? (
                <StatusMessage
                    id="roles-table-status-msg"
                    errorMessage={errorMessage}
                />
            ) : null }

            { canCreate ? (
                <button className={classes.createUserBtn} onClick={handleCreateRole} type="button">
                    <FontAwesomeIcon icon="plus" />
                    Create Role
                </button>
            ) : null }

            { canDelete ? (
                <button className={classes.deleteUserBtn} onClick={handleDeleteRole} disabled={selected.length === 0} type="button">
                    <FontAwesomeIcon icon="trash" />
                    Delete
                </button>
            ) : null }

            { showCreateModal ? (
                <RoleModal
                    data={data}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    type="create"
                    title="Create Role"
                    submitText="Save"
                />
            ) : null }

            { showDeleteModal ? (
                <RoleDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                />
            ) : null }

        </>

    );
};

RoleTableActions.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    data: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string,
        roleName: PropTypes.string,
        permission: PropTypes.arrayOf(PropTypes.shape({
            context: PropTypes.string,
            create: PropTypes.bool,
            delete: PropTypes.bool,
            descriptorName: PropTypes.string,
            execute: PropTypes.bool,
            read: PropTypes.bool,
            uploadDelete: PropTypes.bool,
            uploadRead: PropTypes.bool,
            uploadWrite: PropTypes.bool,
            write: PropTypes.bool
        }))
    })),
    selected: PropTypes.arrayOf(PropTypes.string)
};

export default RoleTableActions;
