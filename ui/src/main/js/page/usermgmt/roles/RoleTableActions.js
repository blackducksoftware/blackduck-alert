import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import RoleDeleteModal from 'page/usermgmt/roles/RoleDeleteModal';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import StatusMessage from 'common/component/StatusMessage';
import Button from 'common/component/button/Button';
import { fetchRoles } from 'store/actions/roles';
import { useDispatch, useSelector } from 'react-redux';

const CREATE_MODAL_OPTIONS = {
    type: 'CREATE',
    title: 'Create Role',
    submitText: 'Save'
};

const RoleTableActions = ({ canCreate, canDelete, data, selected, setSelected }) => {
    const dispatch = useDispatch();
    const { fetching } = useSelector((state) => state.roles);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    useEffect(() => {
        if (showCreateModal) {
            setStatusMessage(undefined);
        }
    }, [showCreateModal]);

    function handleCreateRole() {
        setShowCreateModal(true);
    }

    function handleDeleteRole() {
        setShowDeleteModal(true);
    }

    function handleRefresh() {
        dispatch(fetchRoles());
    }

    return (
        <>
            {statusMessage && (
                <StatusMessage
                    actionMessage={statusMessage.type === 'success' ? statusMessage.message : null}
                    errorMessage={statusMessage.type === 'error' ? statusMessage.message : null}
                />
            )}

            { canCreate && (
                <Button onClick={handleCreateRole} type="button" icon="plus" text="Create Role" />
            )}

            {canDelete && (
                <Button
                    onClick={handleDeleteRole}
                    isDisabled={selected.length === 0}
                    type="button"
                    icon="trash"
                    text="Delete"
                    buttonStyle="delete"
                />
            )}

            <Button onClick={handleRefresh} type="button" text="Refresh" isDisabled={fetching} showLoader={fetching} />

            {showCreateModal && (
                <RoleModal
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={CREATE_MODAL_OPTIONS}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully created 1 role."
                />
            )}

            { showDeleteModal && (
                <RoleDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
                    setSelected={setSelected}
                />
            ) }

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
    selected: PropTypes.arrayOf(PropTypes.string),
    setSelected: PropTypes.func
};

export default RoleTableActions;
