import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import ButtonField from 'common/component/input/field/ButtonField';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { AZURE_BOARDS_GLOBAL_FIELD_KEYS } from 'page/channel/azure/AzureBoardsModel';
import { clearAzureFieldErrors, fetchAzure, saveAzureBoard, sendOAuth, testAzureBoard, validateAzure } from 'store/actions/azure';

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

function getInitialData(type, data) {
    if (type === 'EDIT') {
        return data;
    } else if (type === 'COPY') {
        const { name, organizationName } = data;
        return {
            name,
            organizationName
        };
    } else {
        return {};
    }
}

const AzureBoardModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage, readonly }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { copyDescription, submitText, title, type } = modalOptions;
    const [azureModel, setAzureModel] = useState(getInitialData(type, data));
    const [showLoader, setShowLoader] = useState();
    const [oAuthClick, setOAuthclick] = useState(false);
    const [buttonMessage, setButtonMessage] = useState('');
    const [buttonSuccess, setButtonSuccess] = useState(false);
    const [notificationConfig, setNotificationConfig] = useState();
    const [showNotification, setShowNotification] = useState(false);
    const [requestType, setRequestType] = useState();
    const { saveStatus, testStatus, oAuthStatus, oAuthLink, error } = useSelector((state) => state.azure);

    function handleClose() {
        toggleModal(false);
        dispatch(fetchAzure());
        dispatch(clearAzureFieldErrors());
    }

    function handleSave() {
        dispatch(saveAzureBoard(azureModel));
    }

    function handleSubmit(submitType) {
        setRequestType(submitType);
        setShowNotification(false);
        setNotificationConfig();
        dispatch(validateAzure(azureModel));
    }

    function handleTest() {
        dispatch(testAzureBoard(azureModel));
    }

    function handleOAuth() {
        dispatch(sendOAuth(azureModel));
    }

    const authenticateAzureForm = () => {
        setButtonSuccess(false);
        setOAuthclick(true);
        handleSubmit();
    };

    useEffect(() => {
        setOAuthclick(false);

        if (oAuthStatus === 'FETCHING') {
            setShowLoader(requestType);
        }

        if (oAuthStatus === 'SUCCESS') {
            window.location.replace(oAuthLink);
        }

        if (oAuthStatus === 'ERROR') {
            setShowLoader();
        }
    }, [oAuthStatus, oAuthLink]);

    useEffect(() => {
        if (saveStatus === 'VALIDATING' || saveStatus === 'SAVING' || testStatus === 'TESTING') {
            setShowLoader(requestType);
        }

        if (saveStatus === 'VALIDATED' && requestType === 'save') {
            handleSave();
        }

        if (saveStatus === 'VALIDATED' && oAuthClick) {
            handleOAuth();
        }

        if (saveStatus === 'VALIDATED' && requestType === 'test') {
            handleTest();
        }

        if (saveStatus === 'SAVED' && requestType === 'save') {
            setShowLoader();
            setRequestType();
            setButtonMessage();
            handleClose();
            setStatusMessage({
                message: successMessage,
                type: 'success'
            });
        }

        if (testStatus === 'ERROR' && requestType === 'test') {
            setShowLoader();
            setNotificationConfig({
                title: 'Azure Board Connection Unsuccessful.',
                message: error.message,
                type: 'error'
            });
            setShowNotification(true);
        }

        if (testStatus === 'SUCCESS' && requestType === 'test') {
            setShowLoader();
            setRequestType();
            setNotificationConfig({
                title: 'Azure Board Connection Successful.',
                type: 'success'
            });
            setShowNotification(true);
            dispatch(clearAzureFieldErrors());
        }

        if (saveStatus === 'ERROR') {
            setShowLoader();
        }
    }, [saveStatus, testStatus]);

    return (
        <Modal
            isOpen={isOpen}
            size="lg"
            title={title}
            closeModal={handleClose}
            handleSubmit={() => handleSubmit('save')}
            handleTest={() => handleSubmit('test')}
            testText="Test Connection"
            submitText={submitText}
            showLoader={showLoader}
            notification={notificationConfig}
            showNotification={showNotification}
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
                    customDescription="The name of the Azure Board for your identification purposes."
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
                    customDescription="The name of the Azure DevOps organization."
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
                    customDescription="The App ID created for Alert when registering your Azure DevOps Client Application."
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
                    customDescription="The Client secret created for Alert when registering your Azure DevOps Application."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(azureModel, setAzureModel)}
                    value={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret] || undefined}
                    isSet={azureModel[AZURE_BOARDS_GLOBAL_FIELD_KEYS.isClientSecretSet]}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret}
                    errorValue={error.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS.clientSecret]}
                />
                <ButtonField
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS.configureOAuth}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS.configureOAuth}
                    label="Microsoft OAuth"
                    buttonLabel="Save/Authenticate"
                    customDescription="This will redirect you to Microsoft's OAuth login. To clear the Oauth request cache, please delete and reconfigure the Azure fields.  Please note you will remain logged in; for security reasons you may want to logout of your Microsoft account after authenticating the application."
                    onSendClick={authenticateAzureForm}
                    fieldKey={AZURE_BOARDS_GLOBAL_FIELD_KEYS.configureOAuth}
                    fieldError={error.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS.configureOAuth]}
                    readOnly={readonly}
                    success={buttonSuccess}
                    statusMessage={buttonMessage}
                />
            </div>
        </Modal>
    );
};

AzureBoardModal.propTypes = {
    readonly: PropTypes.bool,
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
