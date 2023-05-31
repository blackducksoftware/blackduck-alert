import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import Modal from 'common/component/modal/Modal';
import { deleteDistribution, fetchDistibution } from 'store/actions/distribution';
import Card from 'common/component/Card';
import { channelTranslation } from 'page/distribution/DistributionModel';

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
    deleteOptions: {
        overflowY: 'auto'
    }
});

function getStagedForDelete(data, selected) {
    const staged = data.models.filter((distribution) => selected.includes(distribution.id));
    return staged.map((distribution) => ({ ...distribution, staged: true }));
}

const DistributionDeleteModal = ({ isOpen, toggleModal, data, selected, setSelected, setStatusMessage }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { deleteStatus, error } = useSelector((state) => state.distribution);
    const [selectedJobs, setSelectedJobs] = useState(getStagedForDelete(data, selected));
    const [showLoader, setShowLoader] = useState(false);
    const isMultiDelete = selectedJobs.length > 1;

    function handleClose() {
        dispatch(fetchDistibution());
        toggleModal(false);
    }

    function handleDelete() {
        dispatch(deleteDistribution(selectedJobs));
    }

    useEffect(() => {
        setSelectedJobs(getStagedForDelete(data, selected));
    }, [selected]);

    useEffect(() => {
        if (deleteStatus === 'DELETING') {
            setShowLoader(true);
        }

        if (deleteStatus === 'DELETED') {
            setShowLoader(false);

            const successMessage = isMultiDelete
                ? `Successfully deleted ${selectedJobs.length} distributions.`
                : 'Successfully deleted 1 distribution.';

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
        const toggledDistributions = selectedJobs.map((distribution) => {
            if (distribution.id === selection.id) {
                return { ...distribution, staged: !distribution.staged };
            }
            return distribution;
        });

        setSelectedJobs(toggledDistributions);
    }

    return (
        <>
            <Modal
                isOpen={isOpen}
                size="sm"
                title={isMultiDelete ? 'Delete Distributions' : 'Delete Distribution'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Delete"
                showLoader={showLoader}
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiDelete ? 'Are you sure you want to delete these distributions?' : 'Are you sure you want to delete this distribution?' }
                </div>
                <div>
                    { selectedJobs?.map((distribution) => (
                        <div className={classes.cardContainer} key={distribution.id}>
                            <input type="checkbox" checked={distribution.staged} onChange={() => toggleSelect(distribution)} />
                            <Card icon={['fas', 'tasks']} label={distribution.jobName} description={channelTranslation.label(distribution.channelName)} />
                        </div>
                    ))}
                </div>
            </Modal>
        </>

    );
};

DistributionDeleteModal.propTypes = {
    data: PropTypes.object,
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    selected: PropTypes.array,
    setStatusMessage: PropTypes.func,
    setSelected: PropTypes.func
};

export default DistributionDeleteModal;
