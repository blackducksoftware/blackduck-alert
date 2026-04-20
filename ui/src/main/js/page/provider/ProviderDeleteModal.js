import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { bulkDeleteProviders, fetchProviders } from 'store/actions/provider';
import DeleteModal from 'common/component/modal/DeleteModal';

function getStagedForDelete(data, selected) {
    const staged = data.filter((provider) => selected.includes(provider.id));
    return staged.map((provider) => ({ ...provider, staged: true }));
}

const ProviderDeleteModal = ({ isOpen, toggleModal, data, selected, setStatusMessage, setSelected }) => {
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.provider);
    const [selectedProviders, setSelectedProviders] = useState(getStagedForDelete(data, selected));
    const [showLoader, setShowLoader] = useState(false);
    const isMultiProviderDelete = selectedProviders.length > 1;

    function handleClose() {
        dispatch(fetchProviders());
        toggleModal(false);
    }

    function handleDelete() {
        dispatch(bulkDeleteProviders(selectedProviders.filter((provider) => provider.staged)));
    }

    useEffect(() => {
        setSelectedProviders(getStagedForDelete(data, selected));
    }, [selected]);

    useEffect(() => {
        if (deleteStatus === 'DELETING') {
            setShowLoader(true);
        }

        if (deleteStatus === 'SUCCESS') {
            setShowLoader(false);

            const stagedCount = selectedProviders.filter((provider) => provider.staged).length;
            if (stagedCount > 0) {
                const successMessage = isMultiProviderDelete
                    ? `Successfully deleted ${stagedCount} providers.`
                    : 'Successfully deleted 1 provider.';

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
            title={isMultiProviderDelete ? 'Delete Providers' : 'Delete Provider'}
            confirmationMessage={isMultiProviderDelete ? 'Are you sure you want to delete these providers?' : 'Are you sure you want to delete this provider?'}
            onClose={handleClose}
            onDelete={handleDelete}
            isLoading={showLoader}
        />
    );
};

ProviderDeleteModal.propTypes = {
    data: PropTypes.array,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func,
    setSelected: PropTypes.func
};

export default ProviderDeleteModal;
