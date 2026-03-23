import React, { useState } from 'react';
import PropTypes from 'prop-types';
import UserModal from 'page/usermgmt/user/UserModal';
import UserDeleteModal from 'page/usermgmt/user/UserDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const NON_DELETABLE_USERNAMES = ['alertuser', 'jobmanager', 'sysadmin'];

const UserRowActionsCell = ({ data, settings }) => {
    const [showCopyModal, setShowCopyModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const isExternalUser = data.authenticationType !== 'DATABASE';
    const isAdministrativeUser = NON_DELETABLE_USERNAMES.includes(data.username);

    const copyModalOptions = {
        type: 'COPY',
        submitText: 'Create',
        title: `Copy User, '${data.username}'`,
        copyDescription: `Performing this action will create a new user by using the same settings as '${data.username}'`
    };

    const editModalOptions = {
        type: 'EDIT',
        submitText: 'Save Edit',
        title: 'Edit User',
        successMessage: `Successfully updated ${data.username}`
    };

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
        setSelectedData(data);
    }

    function handleCopyClick() {
        setStatusMessage();
        setShowCopyModal(true);
        setSelectedData((rowData) => ({
            ...rowData,
            id: null,
            username: '',
            password: '',
            passwordSet: false,
            emailAddress: selectedData.emailAddress || '',
            roleNames: selectedData.roleNames
        }));
    }
    
    function handleDeleteClick() {
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

            <RowActionsCell>
                <Dropdown.Item as="button" onClick={handleEditClick} disabled={settings.readonly}>
                    Edit
                </Dropdown.Item>
                { !isExternalUser && (
                    <Dropdown.Item as="button" onClick={handleCopyClick} disabled={settings.readonly}>
                        Copy
                    </Dropdown.Item>
                )}
                
                {!isAdministrativeUser && (
                    <>
                        <Dropdown.Divider />
                        <Dropdown.Item as="button" onClick={handleDeleteClick} disabled={settings.readonly}>
                            Delete
                        </Dropdown.Item>
                    </>
                )}
                
            </RowActionsCell>

            { showCopyModal && (
                <UserModal
                    data={selectedData}
                    isOpen={showCopyModal}
                    toggleModal={setShowCopyModal}
                    modalOptions={copyModalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully created 1 new provider."
                    readonly={settings.readonly}
                />
            )}

            { showEditModal && (
                <UserModal
                    data={selectedData}
                    isOpen={showEditModal}
                    toggleModal={setShowEditModal}
                    modalOptions={editModalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage={editModalOptions.successMessage}
                />
            )}

            { showDeleteModal && (
                <UserDeleteModal
                    data={[data]}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={[data.id]}
                    setStatusMessage={setStatusMessage}
                    setSelected={() => console.log('setting')}
                />
            )}
        </>

    );
};

UserRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool
    })
};

export default UserRowActionsCell;
