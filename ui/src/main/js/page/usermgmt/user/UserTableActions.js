import React, { useState } from 'react';
import PropTypes from 'prop-types';
import UserDeleteModal from 'page/usermgmt/user/UserDeleteModal';
import UserModal from 'page/usermgmt/user/UserModal';
import StatusMessage from 'common/component/StatusMessage';
import Button from 'common/component/button/Button';

const UserTableActions = ({ canCreate, canDelete, data, selected }) => {
    const modalOptions = {
        type: 'CREATE',
        submitText: 'Create',
        title: 'Create User'
    };

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateUserClick() {
        setStatusMessage();
        setShowCreateModal(true);
    }

    function handleDeleteUserClick() {
        setStatusMessage();
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
                <Button onClick={handleCreateUserClick} type="button" icon="plus" text="Create User" style="default" />
            )}

            { canDelete && (
                <Button onClick={handleDeleteUserClick} isDisabled={selected.length === 0} type="button" icon="trash" text="Delete" style="delete" />
            )}

            { showCreateModal && (
                <UserModal
                    data={data}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully created 1 new user."
                />
            )}

            { showDeleteModal && (
                <UserDeleteModal
                    data={data}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={selected}
                    setStatusMessage={setStatusMessage}
                />
            )}
        </>
    );
};

UserTableActions.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    data: PropTypes.arrayOf(PropTypes.object),
    selected: PropTypes.array
};

export default UserTableActions;
