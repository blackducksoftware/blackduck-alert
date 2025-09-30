import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';
import CheckboxInput from 'common/component/input/CheckboxInput';
import NumberInput from "common/component/input/NumberInput";
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import ButtonField from 'common/component/input/field/ButtonField';
import { clearJiraServerFieldErrors, fetchJiraServer, installJiraServerPlugin, saveJiraServer, testJiraServer, validateJiraServer } from 'store/actions/jira-server';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { JIRA_SERVER_GLOBAL_FIELD_KEYS } from 'page/channel/jira/server/JiraServerModel';
import RadioInput from 'common/component/input/RadioInput';

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

const radioOptions = [{
    name: 'Basic',
    value: 'BASIC',
    label: 'Basic'
}, {
    name: 'Personal Access Token',
    value: 'PERSONAL_ACCESS_TOKEN',
    label: 'Personal Access Token'
}];

function getInitialData(type, data) {
    if (type === 'CREATE') {
        return { authorizationMethod: 'BASIC', timeout: 300 }
    }
    if (type === 'EDIT') {
        return data;
    }
    if (type === 'COPY') {
        const { name, url, timeout, authorizationMethod, userName, disablePluginCheck } = data;
        return {
            name,
            url,
            timeout,
            authorizationMethod,
            userName,
            disablePluginCheck
        };
    }
    return {};
}

const JiraServerModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage, readonly, paramsConfig }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { copyDescription, submitText, title, type } = modalOptions;
    const [jiraServerModel, setJiraServerModel] = useState(getInitialData(type, data));
    const [showLoader, setShowLoader] = useState(false);
    const [requestType, setRequestType] = useState();
    const [notificationConfig, setNotificationConfig] = useState();
    const [showNotification, setShowNotification] = useState(false);
    const [buttonMessage, setButtonMessage] = useState('');
    const [buttonSuccess, setButtonSuccess] = useState(false);
    const { pluginStatus, saveStatus, testStatus, error } = useSelector((state) => state.jiraServer);

    function handleClose() {
        toggleModal(false);
        dispatch(clearJiraServerFieldErrors());
        dispatch(fetchJiraServer(paramsConfig));
    }

    function handleSave() {
        dispatch(saveJiraServer(jiraServerModel));
    }

    function handleSubmit(submitType) {
        setRequestType(submitType);
        setShowNotification(false);
        setNotificationConfig();
        dispatch(validateJiraServer(jiraServerModel));
    }

    function handleTest() {
        dispatch(testJiraServer(jiraServerModel));
    }

    function handleInstallJiraServerPlugin() {
        dispatch(installJiraServerPlugin(jiraServerModel));
    }

    const installPlugin = () => {
        setButtonSuccess(false);
        handleSubmit('installPlugin');
    };

    useEffect(() => {
        if (pluginStatus === 'FETCHING') {
            setShowLoader(true);
        }

        if (pluginStatus === 'SUCCESS') {
            setShowLoader(false);
            setNotificationConfig({
                title: 'Jira Server Plugin Install Successful.',
                type: 'success'
            });
            setShowNotification(true);
        }

        if (pluginStatus === 'ERROR') {
            setShowLoader(false);
            setNotificationConfig({
                title: 'Configure Jira Server Plugin Unsuccessful.',
                message: error?.message,
                type: 'error'
            });
            setShowNotification(true);
        }
    }, [pluginStatus]);

    useEffect(() => {
        if (saveStatus === 'VALIDATING' || saveStatus === 'SAVING' || testStatus === 'TESTING') {
            setShowLoader(true);
        }

        if (saveStatus === 'VALIDATED' && requestType === 'installPlugin') {
            handleInstallJiraServerPlugin();
        }

        if (saveStatus === 'VALIDATED' && requestType === 'save') {
            handleSave();
        }

        if (saveStatus === 'VALIDATED' && requestType === 'test') {
            handleTest();
        }

        if (saveStatus === 'SAVED') {
            setShowLoader(false);
            setButtonMessage();
            handleClose();
            setStatusMessage({
                message: successMessage,
                type: 'success'
            });
        }

        if (testStatus === 'ERROR' && requestType === 'test') {
            setShowLoader(false);
            setNotificationConfig({
                title: 'Jira Server Test Unsuccessful.',
                message: error?.message,
                type: 'error'
            });
            setShowNotification(true);
        }

        if (testStatus === 'SUCCESS' && requestType === 'test') {
            setShowLoader(false);
            setRequestType();
            setNotificationConfig({
                title: 'Jira Server Test Successful.',
                type: 'success'
            });
            setShowNotification(true);
            dispatch(clearJiraServerFieldErrors());
        }

        if (saveStatus === 'ERROR' && error.message.isBadRequest) {
            setNotificationConfig({
                title: error.message.error,
                message: error.message.message,
                type: 'error'
            });
            setShowNotification(true);
            setShowLoader(false);
        }

        if (saveStatus === 'ERROR') {
            setShowLoader(false);
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
            testText="Test Jira Server"
            submitText={submitText}
            showLoader={showLoader}
            notification={notificationConfig}
            showNotification={showNotification}
        >
            {type === 'COPY' && (
                <div className={classes.descriptorContainer}>
                    <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                    <span className={classes.descriptor}>
                        {copyDescription}
                    </span>
                </div>
            )}
            <div>

                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.name}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.name}
                    label="Name"
                    customDescription="The unique name for the Jira server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                    value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.name] || undefined}
                    errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.name}
                    errorValue={error.fieldErrors.name}
                />
                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.url}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.url}
                    label="URL"
                    customDescription="The URL of the Jira server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                    value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.url] || undefined}
                    errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.url}
                    errorValue={error.fieldErrors.url}
                />
                <RadioInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.authorizationMethod}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.authorizationMethod}
                    label="Authentication Method"
                    customDescription="Select the type of authentication that you would like to use for connecting to the intended Jira Server."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                    radioOptions={radioOptions}
                    checked={jiraServerModel.authorizationMethod}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(JIRA_SERVER_GLOBAL_FIELD_KEYS.authorizationMethod)}
                    errorValue={error.fieldErrors[JIRA_SERVER_GLOBAL_FIELD_KEYS.authorizationMethod]}
                    required
                    isInModal
                />
                {jiraServerModel.authorizationMethod === 'BASIC' ? (
                    <>
                        <TextInput
                            id={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                            name={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                            label="User Name"
                            customDescription="The username of the Jira Server user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin."
                            required
                            readOnly={readonly}
                            onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                            value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.username] || undefined}
                            errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                            errorValue={error.fieldErrors.userName}
                        />
                        <PasswordInput
                            id={JIRA_SERVER_GLOBAL_FIELD_KEYS.password}
                            name={JIRA_SERVER_GLOBAL_FIELD_KEYS.password}
                            label="Password"
                            customDescription="The password of the specified Jira Server user."
                            required
                            readOnly={readonly}
                            onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                            value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.password] || undefined}
                            isSet={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.isPasswordSet]}
                            errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.password}
                            errorValue={error.fieldErrors.password}
                        />
                    </>
                ) : (
                    <PasswordInput
                        id={JIRA_SERVER_GLOBAL_FIELD_KEYS.accessToken}
                        name={JIRA_SERVER_GLOBAL_FIELD_KEYS.accessToken}
                        label="Access Token"
                        customDescription="The Jira Server's access token used for authentication."
                        required
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                        value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.accessToken] || undefined}
                        isSet={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.isAccessTokenSet]}
                        errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.accessToken}
                        errorValue={error.fieldErrors.accessToken}
                    />
                )}

                <NumberInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.timeout}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.timeout}
                    label="Timeout"
                    customDescription="The timeout in seconds for connections to the Jira server instance."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                    value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.timeout] || undefined}
                    errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.timeout}
                    errorValue={error.fieldErrors.timeout}
                />
                <CheckboxInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    label="Disable Plugin Check"
                    customDescription="This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance. Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly, and duplicate issues may be created."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.createBooleanInputChangeHandler(jiraServerModel, setJiraServerModel)}
                    isChecked={jiraServerModel.disablePluginCheck}
                    errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    errorValue={error.fieldErrors.disablePluginCheck}
                />
                <ButtonField
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    label="Configure Jira server plugin"
                    buttonLabel="Install Plugin Remotely"
                    customDescription="Installs a required plugin on the Jira server."
                    onSendClick={installPlugin}
                    fieldKey={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    fieldError={error.fieldErrors[JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin]}
                    readOnly={readonly}
                    success={buttonSuccess}
                    statusMessage={buttonMessage}
                />
            </div>
        </Modal>
    );
};

JiraServerModal.propTypes = {
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
    successMessage: PropTypes.string,
    paramsConfig: PropTypes.object
};

export default JiraServerModal;
