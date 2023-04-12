import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';
import CheckboxInput from 'common/component/input/CheckboxInput';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import ButtonField from 'common/component/input/field/ButtonField';
import { clearJiraServerFieldErrors, fetchJiraServer, saveJiraServer, 
    sendJiraServerPlugin, testJiraServer, validateJiraServer 
} from 'store/actions/jira-server';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { JIRA_SERVER_GLOBAL_FIELD_KEYS } from 'page/channel/jira/server/JiraServerModel';

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

const JiraServerModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage, readonly }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { copyDescription, submitText, title, type } = modalOptions;
    const [jiraServerModel, setJiraServerModel] = useState(type === 'CREATE' ? {} : data);
    const [showLoader, setShowLoader] = useState(false);
    const [requestType, setRequestType] = useState();
    const [notificationConfig, setNotificationConfig] = useState();
    const [showNotification, setShowNotification] = useState(false);
    const [installPluginClick, setInstallPluginClick] = useState(false);
    const [buttonMessage, setButtonMessage] = useState('');
    const [buttonSuccess, setButtonSuccess] = useState(false);
    const { pluginStatus, saveStatus, testStatus, oAuthLink, error } = useSelector((state) => state.jiraServer);

    function handleClose() {
        toggleModal(false);
        dispatch(clearJiraServerFieldErrors());
        dispatch(fetchJiraServer());
    }

    function handleSave() {
        dispatch(saveJiraServer(jiraServerModel));
    }

    function handleSubmit(submitType) {
        setRequestType(submitType);
        setShowNotification(false);
        setNotificationConfig();
        dispatch(clearJiraServerFieldErrors());
        dispatch(validateJiraServer(jiraServerModel));
    }

    function handleTest() {
        dispatch(testJiraServer(jiraServerModel));
    }

    function handleOAuth() {
        dispatch(sendJiraServerPlugin(jiraServerModel));
    }

    const installPlugin = () => {
        setButtonSuccess(false);
        setInstallPluginClick(true);
        handleSubmit('save');
    };

    useEffect(() => {
        if (pluginStatus === 'FETCHING') {
            setShowLoader(true);
        }

        if (pluginStatus === 'SUCCESS') {
            handleClose();
            setStatusMessage({
                message: successMessage,
                type: 'success'
            });
        }

        if (pluginStatus === 'ERROR') {
            setShowLoader(false);
            setNotificationConfig({
                title: 'Configure Jira Server Plugin Unsuccessful.',
                message: error?.message,
                type: 'error'
            })
            setShowNotification(true);
        }
    }, [pluginStatus, oAuthLink]);

    useEffect(() => {
        if (saveStatus === 'VALIDATING' || saveStatus === 'SAVING' || testStatus === 'TESTING') {
            setShowLoader(true);
        }

        if (saveStatus === 'VALIDATED' && requestType === 'save') {
            if (installPluginClick) {
                handleOAuth();
            } else {
                handleSave();
            }
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
            })
            setShowNotification(true);
        }

        if (testStatus === 'SUCCESS' && requestType === 'test') {
            setShowLoader(false);
            setRequestType();
            setNotificationConfig({
                title: 'Jira Server Test Successful.',
                type: 'success'
            })
            setShowNotification(true);
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
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.name}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.name}
                    label="Name"
                    description="The unique name for the Jira Server server."
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
                    description="The URL of the Jira Server server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                    value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.url] || undefined}
                    errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.url}
                    errorValue={error.fieldErrors.url}
                />
                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                    label="User Name"
                    description="The username of the Jira Server user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin."
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
                    description="The password of the specified Jira Server user."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                    value={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.password] || undefined}
                    isSet={jiraServerModel[JIRA_SERVER_GLOBAL_FIELD_KEYS.isPasswordSet]}
                    errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.password}
                    errorValue={error.fieldErrors.password}
                />
                <CheckboxInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    label="Disable Plugin Check"
                    description="This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance. Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly, and duplicate issues may be created."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerModel, setJiraServerModel)}
                    isChecked={jiraServerModel.disablePluginCheck === 'true'}
                    errorName={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    errorValue={error.fieldErrors.disablePluginCheck}
                />
                <ButtonField
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    label="Configure Jira server plugin"
                    buttonLabel="Install Plugin Remotely"
                    description="Installs a required plugin on the Jira server."
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
    successMessage: PropTypes.string
};

export default JiraServerModal;
