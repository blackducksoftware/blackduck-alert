import React, { useState } from 'react';
import PropTypes from 'prop-types';
import RoleDeleteModal from 'page/usermgmt/roles/RoleDeleteModal';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import StatusMessage from 'common/component/StatusMessage';
import CreateButton from '../../../common/component/button/CreateButton';
import DeleteButton from '../../../common/component/button/DeleteButton';

const CREATE_MODAL_OPTIONS = {
    type: 'CREATE',
    title: 'Create Role',
    submitText: 'Save'
};

const RoleTableActions = ({ canCreate, canDelete, data, selected }) => {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateRole() {
        setShowCreateModal(true);
    }

    function handleDeleteRole() {
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

            { canCreate && (
                <CreateButton onClick={handleCreateRole} type="button" icon="plus" text="Create Role" />
            )}

            { canDelete && (
                <DeleteButton onClick={handleDeleteRole} isDisabled={selected.length === 0} type="button" icon="trash" text="Delete" />
            )}

            { showCreateModal && (
                <RoleModal
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={CREATE_MODAL_OPTIONS}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully created 1 role."
                />
            ) }

            { showDeleteModal && (
                <RoleDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
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
    selected: PropTypes.arrayOf(PropTypes.string)
};

export default RoleTableActions;
