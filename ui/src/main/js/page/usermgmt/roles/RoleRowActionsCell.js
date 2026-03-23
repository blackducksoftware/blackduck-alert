import React, { useState } from 'react';
import PropTypes from 'prop-types';
import RoleModal from 'page/usermgmt/roles/RoleModal';
import RoleDeleteModal from 'page/usermgmt/roles/RoleDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const NON_DELETABLE_ROLES = ['ALERT_ADMIN', 'ALERT_JOB_MANAGER', 'ALERT_USER'];

const RoleRowActionsCell = ({ data, settings }) => {
    const [showCopyModal, setShowCopyModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const isAdministrativeRole = NON_DELETABLE_ROLES.includes(data.roleName);

    const copyModalOptions = {
        type: 'COPY',
        title: 'Copy Role',
        submitText: 'Save',
        copyDescription: `Performing this action will create a new role by using the same settings as '${data.roleName}'`
    };

    const editModalOptions = {
        type: 'EDIT',
        title: 'Edit Role',
        submitText: 'Save'
    };

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
        setSelectedData(data);
    }

    function handleCopyClick() {
        setStatusMessage();
        setShowCopyModal(true);
        setSelectedData((roleData) => ({ ...roleData, id: null }));
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
                <Dropdown.Item as="button" onClick={handleCopyClick} disabled={settings.readonly}>
                    Copy
                </Dropdown.Item>
                
                {!isAdministrativeRole && (
                    <>
                        <Dropdown.Divider />
                        <Dropdown.Item as="button" onClick={handleDeleteClick} disabled={settings.readonly}>
                            Delete
                        </Dropdown.Item>
                    </>
                )}
                
            </RowActionsCell>

            { showCopyModal && (
                <RoleModal
                    data={selectedData}
                    isOpen={showCopyModal}
                    toggleModal={setShowCopyModal}
                    modalOptions={copyModalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully created 1 role."
                    readonly={settings.readonly}
                />
            )}

            { showEditModal && (
                <RoleModal
                    data={selectedData}
                    isOpen={showEditModal}
                    toggleModal={setShowEditModal}
                    modalOptions={editModalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully edited 1 Role."
                />
            )}

            { showDeleteModal && (
                <RoleDeleteModal
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

RoleRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool
    })
};

export default RoleRowActionsCell;
