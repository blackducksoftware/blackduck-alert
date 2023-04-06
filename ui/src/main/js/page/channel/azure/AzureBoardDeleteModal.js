import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { fetchAzure, deleteAzureBoards } from '../../../store/actions/azure';
import Modal from 'common/component/modal/Modal';

const useStyles = createUseStyles({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    },
    userCard: {
        display: 'flex',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        backgroundColor: '#e8e6e6',
        padding: '8px',
        margin: [0, '50px', '10px', '20px'],
        width: '250px'
    },
    userIcon: {
        flexBasis: '20%',
        backgroundColor: 'white',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        height: '50px',
        paddingTop: '5px',
        textAlign: 'center'
    },
    userInfo: {
        flexGrow: 1,
        padding: ['5px', 0, 0, '15px']
    }
});

function getStagedForDelete(data, selected) {
    const staged = data.models.filter((board) => selected.includes(board.id));
    return staged.map((board) => ({ ...board, staged: true }));
}

const AzureBoardDeleteModal = ({ isOpen, toggleModal, data, selected, setStatusMessage }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.azure);
    const [selectedAzureBoards, setSelectedAzureBoards] = useState(getStagedForDelete(data, selected));
    const [showLoader, setShowLoader] = useState(false);
    const isMultiDelete = selectedAzureBoards.length > 1;

    function handleClose() {
        dispatch(fetchAzure());
        toggleModal(false);
    }

    function handleDelete() {
        dispatch(deleteAzureBoards(selectedAzureBoards));
    }

    useEffect(() => {
        setSelectedAzureBoards(getStagedForDelete(data, selected));
    }, [selected]);

    useEffect(() => {
        if (deleteStatus === 'DELETING') {
            setShowLoader(true);
        }

        if (deleteStatus === 'DELETED') {
            setShowLoader(false);

            const successMessage = isMultiDelete
                ? `Successfully deleted ${selectedAzureBoards.length} Azure Boards.`
                : 'Successfully deleted 1 Azure Board.';

            setStatusMessage({
                message: successMessage,
                type: 'success'
            });

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
        const toggledBoards = selectedAzureBoards.map((board) => {
            if (board.id === selection.id) {
                return { ...board, staged: !board.staged };
            }
            return board;
        });

        setSelectedAzureBoards(toggledBoards);
    }

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="sm"
                title={isMultiDelete ? 'Delete Azure Boards' : 'Delete Azure Board'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
                showLoader={showLoader}
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiDelete ? 'Are you sure you want to delete these Azure Boards?' : 'Are you sure you want to delete this Azure Board?' }
                </div>
                <div>
                    { selectedAzureBoards?.map((board) => (
                        <div className={classes.cardContainer} key={board.id}>
                            <input type="checkbox" checked={board.staged} onChange={() => toggleSelect(board)} />
                            <div className={classes.userCard}>
                                <div className={classes.userIcon}>
                                    <FontAwesomeIcon icon="user" size="3x" />
                                </div>
                                <div className={classes.userInfo}>
                                    <div style={{ fontSize: '16px' }}>{board.name}</div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </Modal>
        </>

    );
};

AzureBoardDeleteModal.propTypes = {
    data: PropTypes.object,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func
};

export default AzureBoardDeleteModal;
