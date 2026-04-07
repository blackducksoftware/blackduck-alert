import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { deleteJiraServer, fetchJiraServer } from 'store/actions/jira-server';
import DeleteModal from 'common/component/modal/DeleteModal';

function getStagedForDelete(data, selected) {
    const staged = data.models.filter((server) => selected.includes(server.id));
    return staged.map((server) => ({ ...server, staged: true }));
}

const JiraServerDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage, paramsConfig, setParamsConfig }) => {
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.jiraServer);
    const [selectedJiraServers, setSelectedJiraServers] = useState(getStagedForDelete(data, selected));
    const [showLoader, setShowLoader] = useState(false);
    const isMultiDelete = selectedJiraServers.length > 1;

    function handleClose() {
        const params = {
            pageNumber: 0,
            pageSize: paramsConfig?.pageSize,
            mutatorData: {
                searchTerm: paramsConfig?.mutatorData?.searchTerm,
                sortName: paramsConfig?.mutatorData?.name,
                sortOrder: paramsConfig?.mutatorData?.direction
            }
        };

        dispatch(fetchJiraServer(params));
        setParamsConfig(params);
        toggleModal(false);
    }

    function handleDelete() {
        dispatch(deleteJiraServer(selectedJiraServers.filter((jiraServer) => jiraServer.staged)));
    }

    useEffect(() => {
        setSelectedJiraServers(getStagedForDelete(data, selected));
    }, [selected]);

    useEffect(() => {
        if (deleteStatus === 'DELETING') {
            setShowLoader(true);
        }

        if (deleteStatus === 'DELETED') {
            setShowLoader(false);

            const stagedCount = selectedJiraServers.filter((jiraServer) => jiraServer.staged).length;
            if (stagedCount > 0) {
                const successMessage = isMultiDelete
                    ? `Successfully deleted ${stagedCount} Jira Servers.`
                    : 'Successfully deleted 1 Jira Server.';

                setStatusMessage({
                    message: successMessage,
                    type: 'success'
                });
            }
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

    return (
        <DeleteModal
            isOpen={isOpen}
            title={isMultiDelete ? 'Delete Jira Servers' : 'Delete Jira Server'}
            confirmationMessage={isMultiDelete ? 'Are you sure you want to delete these Jira Servers?' : 'Are you sure you want to delete this Jira Server?'}
            onClose={handleClose}
            onDelete={handleDelete}
            isLoading={showLoader}
        />
    );
};

JiraServerDeleteModal.propTypes = {
    data: PropTypes.object,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func,
    setSelected: PropTypes.func,
    paramsConfig: PropTypes.object,
    setParamsConfig: PropTypes.func
};

export default JiraServerDeleteModal;
