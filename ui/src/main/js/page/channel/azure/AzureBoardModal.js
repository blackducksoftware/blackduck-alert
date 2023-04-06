import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import { AZURE_BOARDS_GLOBAL_FIELD_KEYS } from 'page/channel/azure/AzureBoardsModel';
import { clearAzureFieldErrors, fetchAzure, saveAzureBoard, validateAzure } from '../../../store/actions/azure';

import * as FieldModelUtilities from 'common/util/fieldModelUtilities';

const useStyles = createUseStyles({
    descriptorContainer: {
        display: 'flex',
        alignItems: 'center',
        padding: [0, 0, '20px', '60px']
    },
    descriptor: {
        fontSize: '14px',
        paddingLeft: '8px'
    }
});

const AzureBoardModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage, readonly }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { copyDescription, submitText, title, type } = modalOptions;
    const [azureModel, setAzureModel] = useState(type === 'CREATE' ? {} : data);
    const [showLoader, setShowLoader] = useState(false);
    const { saveStatus, error } = useSelector((state) => state.azure);

    function handleClose() {
        toggleModal(false);
        dispatch(clearAzureFieldErrors());
        dispatch(fetchAzure());
    }

    function handleSave() {
        dispatch(saveAzureBoard(azureModel));
    }

    function handleSubmit() {
        dispatch(validateAzure(azureModel));
    }

    useEffect(() => {
        console.log(saveStatus);
        if (saveStatus === 'VALIDATING' || saveStatus === 'SAVING') {
            setShowLoader(true);
        }

        if (saveStatus === 'VALIDATED') {
            handleSave();
        }

        if (saveStatus === 'SAVED') {
            setShowLoader(false);
            handleClose();
            setStatusMessage({
                message: successMessage,
                type: 'success'
            });
        }

        if (saveStatus === 'ERROR') {
            setShowLoader(false);
        }
    }, [saveStatus]);

    return (
        <Modal
            isOpen={isOpen}
            size="lg"
            title={title}
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText={submitText}
            showLoader={showLoader}
        >
            { type === 'COPY' && (
                <div className={classes.descriptorContainer}>
                    <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                    <span className={classes.descriptor}>
                        {copyDescription}
                    </span>
                </div>
            )}
            <div>
                <TextInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS.name}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS.name}
                    label="Name"
                    description="The name of the Azure Board for your identification purposes."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(azureModel, setAzureModel)}
                    value={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.name] || undefined}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS.name}
                    errorValue={error.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS.name]}
                />
                <TextInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS.organization}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS.organization}
                    label="Organization Name"
                    description="The name of the Azure DevOps organization."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(azureModel, setAzureModel)}
                    value={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.organization] || undefined}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS.organization}
                    errorValue={error.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS.organization]}
                />
                <PasswordInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS.appId}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS.appId}
                    label="App ID"
                    description="The App ID created for Alert when registering your Azure DevOps Client Application."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(azureModel, setAzureModel)}
                    value={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.appId] || undefined}
                    isSet={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.isAppIdSet]}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS.appId}
                    errorValue={error.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS.appId]}
                />
                <PasswordInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret}
                    label="Client Secret"
                    description="The Client secret created for Alert when registering your Azure DevOps Application."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(azureModel, setAzureModel)}
                    value={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret] || undefined}
                    isSet={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.isClientSecretSet]}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret}
                    errorValue={error.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret]}
                />
            </div>
        </Modal>
    );
};

AzureBoardModal.propTypes = {
    data: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.object),
        PropTypes.object
    ]),
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    modalOptions: PropTypes.shape({
        type: PropTypes.string,
        submitText: PropTypes.string,
        title: PropTypes.string,
        copyDescription: PropTypes.string
    }),
    setStatusMessage: PropTypes.func,
    successMessage: PropTypes.string
};

export default AzureBoardModal;
