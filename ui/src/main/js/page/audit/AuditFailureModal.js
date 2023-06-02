import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Modal from 'common/component/modal/Modal';
import LabelValuePair from 'common/component/LabelValuePair';
import { EXISTING_CHANNELS, EXISTING_PROVIDERS } from 'common/DescriptorInfo';
import AuditFailureModalContent from 'page/audit/AuditFailureModalContent';

const descriptorOptions = {
    ...EXISTING_PROVIDERS,
    ...EXISTING_CHANNELS
};

const useStyles = createUseStyles({
    content: {
        display: 'flex',
        flexDirection: 'column',
        minHeight: '350px'
    },
    metaData: {
        display: 'grid',
        gridTemplateAreas: `
            "provider createdAt"
            "name lastSent"
            "type jobCount"
        `,
        gridRowGap: '5px',
        backgroundColor: '#E1E1E1',
        borderRadius: '2px',
        margin: '0 25px 20px',
        padding: '12px',
        minWidth: 'fit-content'
    },
    provider: {
        gridArea: 'provider'
    },
    createdAt: {
        gridArea: 'createdAt'
    },
    name: {
        gridArea: 'name'
    },
    lastSent: {
        gridArea: 'lastSent'
    },
    type: {
        gridArea: 'type'
    },
    jobCount: {
        gridArea: 'jobCount'
    },
    distributionContainer: {
        margin: '0 25px'
    }
});

const AuditFailureModal = ({ data, isOpen, toggleModal }) => {
    const classes = useStyles();
    const provider = descriptorOptions[data.notification.provider];

    function handleClose() {
        toggleModal(false);
    }

    return (
        <Modal
            isOpen={isOpen}
            size="lg"
            title="Audit Failures"
            closeModal={handleClose}
            handleSubmit={handleClose}
            submitText="Close"
        >
            <div className={classes.content}>
                <div className={classes.metaData}>
                    <div className={classes.provider}>
                        <LabelValuePair label="Provider" value={provider.label} />
                    </div>
                    <div className={classes.createdAt}>
                        <LabelValuePair label="Created At" value={data.notification.providerCreationTime} />
                    </div>
                    <div className={classes.name}>
                        <LabelValuePair label="Provider Name" value={data.notification.providerConfigName} />
                    </div>
                    <div className={classes.type}>
                        <LabelValuePair label="Notification Type" value={data.notification.notificationType} />
                    </div>
                    <div className={classes.lastSent}>
                        <LabelValuePair label="Last Sent" value={data.lastSent} />
                    </div>
                    <div className={classes.jobCount}>
                        <LabelValuePair label="Number of Jobs" value={data.jobs.length} />
                    </div>
                </div>
                <div className={classes.distributionContainer}>
                    <AuditFailureModalContent data={data} />
                </div>
            </div>
        </Modal>
    );
};

AuditFailureModal.propTypes = {
    data: PropTypes.shape({
        fullyQualifiedType: PropTypes.string,
        nextRunTime: PropTypes.string,
        properties: PropTypes.array,
        type: PropTypes.string,
        notification: PropTypes.shape({
            providerCreationTime: PropTypes.string,
            providerConfigName: PropTypes.string,
            notificationType: PropTypes.string
        }),
        lastSent: PropTypes.string,
        jobs: PropTypes.array
    }),
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func
};

export default AuditFailureModal;
