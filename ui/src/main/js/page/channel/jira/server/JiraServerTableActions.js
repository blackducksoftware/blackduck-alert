import React, { useState } from 'react';
import PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import JiraServerModal from './JiraServerModal';
import JiraServerDeleteModal from 'page/channel/jira/server/JiraServerDeleteModal';
import CreateButton from 'common/component/button/CreateButton';
import DeleteButton from 'common/component/button/DeleteButton';

const JiraServerTableActions = ({ data, readonly, allowDelete, selected }) => {
    const modalOptions = {
        type: 'CREATE',
        submitText: 'Create',
        title: 'Create Jira Server Connection'
    };

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [statusMessage, setStatusMessage] = useState();

    function handleCreateJiraServerClick() {
        setStatusMessage();
        setShowCreateModal(true);
    }

    function handleJiraServerDeleteClick() {
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

            <CreateButton onClick={handleCreateJiraServerClick} type="button" icon="plus" text="Create Jira Server" />

            { allowDelete && (
                <DeleteButton onClick={handleJiraServerDeleteClick} isDisabled={selected.length === 0} type="button" icon="trash" text="Delete" />
            )}

            { showCreateModal && (
                <JiraServerModal
                    readonly={readonly}
                    isOpen={showCreateModal}
                    toggleModal={setShowCreateModal}
                    modalOptions={modalOptions}
                    setStatusMessage={setStatusMessage}
                    successMessage="Successfully added 1 new Jira Server connection."
                />
            )}

            { showDeleteModal && (
                <JiraServerDeleteModal
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

JiraServerTableActions.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    data: PropTypes.object,
    selected: PropTypes.array
};

export default JiraServerTableActions;
