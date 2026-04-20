import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { deleteAzureBoards, fetchAzureBoards } from 'store/actions/azure-boards';
import DeleteModal from 'common/component/modal/DeleteModal';

function getStagedForDelete(data, selected) {
    const staged = data.models.filter((board) => selected.includes(board.id));
    return staged.map((board) => ({ ...board, staged: true }));
}

const AzureBoardsDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage, paramsConfig, setParamsConfig }) => {
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.azureBoards);
    const [selectedAzureBoards, setSelectedAzureBoards] = useState(getStagedForDelete(data, selected));
    const [showLoader, setShowLoader] = useState(false);
    const isMultiDelete = selectedAzureBoards.length > 1;

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

        dispatch(fetchAzureBoards(params));
        setParamsConfig(params);
        toggleModal(false);
    }

    function handleDelete() {
        dispatch(deleteAzureBoards(selectedAzureBoards.filter((board) => board.staged)));
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

            const stagedCount = selectedAzureBoards.filter((board) => board.staged).length;
            if (stagedCount > 0) {
                const successMessage = isMultiDelete
                    ? `Successfully deleted ${stagedCount} Azure Boards.`
                    : 'Successfully deleted 1 Azure Board.';

                setStatusMessage({
                    message: successMessage,
                    type: 'success'
                });
            }
            setSelected?.([]);
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
            title={isMultiDelete ? 'Delete Azure Boards' : 'Delete Azure Board'}
            confirmationMessage={isMultiDelete ? 'Are you sure you want to delete these Azure Boards?' : 'Are you sure you want to delete this Azure Board?'}
            onClose={handleClose}
            onDelete={handleDelete}
            isLoading={showLoader}
        />
    );
};

AzureBoardsDeleteModal.propTypes = {
    data: PropTypes.object,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func,
    setSelected: PropTypes.func,
    paramsConfig: PropTypes.object,
    setParamsConfig: PropTypes.func
};

export default AzureBoardsDeleteModal;
