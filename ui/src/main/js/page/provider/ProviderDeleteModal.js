import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { bulkDeleteProviders, fetchProviders } from 'store/actions/provider';
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
    const staged = data.filter((provider) => selected.includes(provider.id));
    return staged.map((provider) => ({ ...provider, staged: true }));
}

const ProviderDeleteModal = ({ isOpen, toggleModal, data, selected, setStatusMessage }) => {
    const classes = useStyles();
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
        dispatch(bulkDeleteProviders(selectedProviders));
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

            const successMessage = isMultiProviderDelete
                ? `Successfully deleted ${selectedProviders.length} providers.`
                : 'Successfully deleted 1 provider.';

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
        const toggledProviders = selectedProviders.map((provider) => {
            if (provider.id === selection.id) {
                return { ...provider, staged: !provider.staged };
            }
            return provider;
        });

        setSelectedProviders(toggledProviders);
    }

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="sm"
                title={isMultiProviderDelete ? 'Delete Providers' : 'Delete Provider'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
                showLoader={showLoader}
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiProviderDelete ? 'Are you sure you want to delete these providers?' : 'Are you sure you want to delete this provider?' }
                </div>
                <div>
                    { selectedProviders?.map((provider) => (
                        <div className={classes.cardContainer} key={provider.id}>
                            <input type="checkbox" checked={provider.staged} onChange={() => toggleSelect(provider)} />
                            <div className={classes.userCard}>
                                <div className={classes.userIcon}>
                                    <FontAwesomeIcon icon="user" size="3x" />
                                </div>
                                <div className={classes.userInfo}>
                                    <div style={{ fontSize: '16px' }}>{provider.name}</div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </Modal>
        </>

    );
};

ProviderDeleteModal.propTypes = {
    data: PropTypes.array,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func
};

export default ProviderDeleteModal;
