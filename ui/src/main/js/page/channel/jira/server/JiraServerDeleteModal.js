import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import Modal from 'common/component/modal/Modal';
import { fetchJiraServer, deleteJiraServer } from 'store/actions/jira-server';
import Card from 'common/component/Card';

const useStyles = createUseStyles({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    }
});

function getStagedForDelete(data, selected) {
    const staged = data.models.filter((server) => selected.includes(server.id));
    return staged.map((server) => ({ ...server, staged: true }));
}

const JiraServerDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.jiraServer);
    const [selectedJiraServers, setSelectedJiraServers] = useState(getStagedForDelete(data, selected));
    const [showLoader, setShowLoader] = useState(false);
    const isMultiDelete = selectedJiraServers.length > 1;

    function handleClose() {
        dispatch(fetchJiraServer());
        toggleModal(false);
    }

    function handleDelete() {
        dispatch(deleteJiraServer(selectedJiraServers));
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

            const successMessage = isMultiDelete
                ? `Successfully deleted ${selectedJiraServers.length} Jira Servers.`
                : 'Successfully deleted 1 Jira Server.';

            setStatusMessage({
                message: successMessage,
                type: 'success'
            });

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

    function toggleSelect(selection) {
        const toggledServers = selectedJiraServers.map((server) => {
            if (server.id === selection.id) {
                return { ...server, staged: !server.staged };
            }
            return server;
        });

        setSelectedJiraServers(toggledServers);
    }

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="sm"
                title={isMultiDelete ? 'Delete Jira Servers' : 'Delete Jira Server'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
                showLoader={showLoader}
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiDelete ? 'Are you sure you want to delete these Jira Servers?' : 'Are you sure you want to delete this Jira Server?' }
                </div>
                <div>
                    { selectedJiraServers?.map((server) => (
                        <div className={classes.cardContainer} key={server.id}>
                            <input type="checkbox" checked={server.staged} onChange={() => toggleSelect(server)} />
                            <Card icon="server" label={server.name} />
                        </div>
                    ))}
                </div>
            </Modal>
        </>

    );
};

JiraServerDeleteModal.propTypes = {
    data: PropTypes.object,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func,
    setSelected: PropTypes.func
};

export default JiraServerDeleteModal;
