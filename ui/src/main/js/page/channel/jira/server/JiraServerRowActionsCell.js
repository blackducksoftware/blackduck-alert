import React, { useState } from 'react';
import PropTypes from 'prop-types';
import JiraServerModal from 'page/channel/jira/server/JiraServerModal';
import JiraServerDeleteModal from 'page/channel/jira/server/JiraServerDeleteModal';
import StatusMessage from 'common/component/StatusMessage';
import Dropdown from 'react-bootstrap/Dropdown';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const JiraServerRowActionsCell = ({ data, settings }) => {
    const [showCopyModal, setShowCopyModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedData, setSelectedData] = useState(data);
    const [statusMessage, setStatusMessage] = useState();
    const { readOnly, paramsConfig, setParamsConfig } = settings;
    const dataStagedForDelete = { models: [data] };

    const copyModalOptions = {
        type: 'COPY',
        title: 'Copy Jira Server',
        submitText: 'Save',
        copyDescription: `Performing this action will create a new Jira Server connection by using the same settings as '${data.name}'`
    };

    const editModalOptions = {
        type: 'EDIT',
        title: 'Edit Jira Server',
        submitText: 'Save'
    };

    function handleEditClick() {
        setStatusMessage();
        setShowEditModal(true);
    }

    function handleCopyClick() {
        setStatusMessage();
        setShowCopyModal(true);
        setSelectedData((rowData) => ({
            ...rowData,
            id: null,
            name: '',
            createdAt: null,
            lastUpdated: null
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
                <Dropdown.Item as="button" onClick={handleEditClick} disabled={readOnly}>
                    Edit
                </Dropdown.Item>
                <Dropdown.Item as="button" onClick={handleCopyClick} disabled={readOnly}>
                    Copy
                </Dropdown.Item>
                <Dropdown.Divider />
                <Dropdown.Item as="button" onClick={handleDeleteClick} disabled={readOnly}>
                    Delete
                </Dropdown.Item>
            </RowActionsCell>

            { showCopyModal && (
                <JiraServerModal
                    data={selectedData}
                    isOpen={showCopyModal}
                    toggleModal={setShowCopyModal}
                    modalOptions={copyModalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully created 1 Jira Server Board."
                    paramsConfig={paramsConfig}
                />
            )}

            { showEditModal && (
                <JiraServerModal
                    data={data}
                    isOpen={showEditModal}
                    toggleModal={setShowEditModal}
                    modalOptions={editModalOptions}
                    setStatusMessage={setStatusMessage}
                    statusMessage="Successfully edited 1 Jira Server."
                    paramsConfig={paramsConfig}
                />
            )}

            { showDeleteModal && (
                <JiraServerDeleteModal
                    data={dataStagedForDelete}
                    isOpen={showDeleteModal}
                    toggleModal={setShowDeleteModal}
                    selected={[data.id]}
                    setStatusMessage={setStatusMessage}
                    paramsConfig={paramsConfig}
                    setParamsConfig={setParamsConfig}
                />
            )}
        </>

    );
};

JiraServerRowActionsCell.propTypes = {
    data: PropTypes.object,
    settings: PropTypes.shape({
        readonly: PropTypes.bool,
        paramsConfig: PropTypes.object,
        setParamsConfig: PropTypes.func,
    })
};

export default JiraServerRowActionsCell;
