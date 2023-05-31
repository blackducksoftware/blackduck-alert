import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import Modal from 'common/component/modal/Modal';
import CheckboxInput from 'common/component/input/CheckboxInput';
import NumberInput from 'common/component/input/NumberInput';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { BLACKDUCK_GLOBAL_FIELD_KEYS } from './blackduck/BlackDuckModel';
import { clearProviderFieldErrors, fetchProviders, saveProvider, testProvider, validateProvider } from '../../store/actions/provider';

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

function getDefaultProviderModel() {
    return {
        context: 'GLOBAL',
        descriptorName: 'provider_blackduck',
        keyToValues: {
            'blackduck.timeout': { isSet: false, values: ['300'] },
            'provider.common.config.enabled': { isSet: false, values: ['true'] }
        }
    };
}

function transformData(data, type) {
    const transformedData = {
        context: 'GLOBAL',
        descriptorName: 'provider_blackduck',
        keyToValues: {
            'provider.common.config.enabled': { isSet: true, values: [data.enabled] },
            'provider.common.config.name': { isSet: true, values: [data.name] },
            'blackduck.url': { isSet: true, values: [data.url] },
            'blackduck.timeout': { isSet: true, values: [data.timeout] }
        }
    };

    if (type === 'EDIT') {
        transformedData.id = data.id;
        transformedData.keyToValues[BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey] = {
            isSet: true,
            values: []
        };
    }

    return transformedData;
}

const ProviderModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage, readonly }) => {
    const classes = useStyles();
    const dispatch = useDispatch();

    const { copyDescription, submitText, title, type } = modalOptions;
    const [showLoader, setShowLoader] = useState();
    const [requestType, setRequestType] = useState();
    const [notificationConfig, setNotificationConfig] = useState();
    const [showNotification, setShowNotification] = useState(false);

    const { saveStatus, testStatus, error } = useSelector((state) => state.provider);
    const [providerModel, setProviderModel] = useState(type === 'CREATE' ? getDefaultProviderModel() : transformData(data, type));

    function handleClose() {
        toggleModal(false);
        dispatch(fetchProviders());
        dispatch(clearProviderFieldErrors());
    }

    function handleSave() {
        dispatch(saveProvider(providerModel));
    }

    function handleSubmit(submitType) {
        setRequestType(submitType);
        setShowNotification(false);
        setNotificationConfig();
        dispatch(validateProvider(providerModel));
    }

    function handleTest() {
        dispatch(testProvider(providerModel));
    }

    useEffect(() => {
        if (saveStatus === 'VALIDATING' || saveStatus === 'SAVING' || testStatus === 'TESTING') {
            setShowLoader(requestType);
        }

        if (saveStatus === 'VALIDATED' && requestType === 'save') {
            handleSave();
        }

        if (saveStatus === 'VALIDATED' && requestType === 'test') {
            handleTest();
        }

        if (saveStatus === 'SAVED' && requestType === 'save') {
            setShowLoader();
            setRequestType();
            handleClose();
            setStatusMessage({
                message: successMessage,
                type: 'success'
            });
        }

        if (testStatus === 'ERROR' && requestType === 'test') {
            setShowLoader();
            setNotificationConfig({
                title: 'Provider Test Unsuccessful.',
                message: error.message,
                type: 'error'
            });
            setShowNotification(true);
        }

        if (testStatus === 'SUCCESS' && requestType === 'test') {
            setShowLoader();
            setRequestType();
            setNotificationConfig({
                title: 'Provider Test Successful.',
                type: 'success'
            });
            setShowNotification(true);
            dispatch(clearProviderFieldErrors());
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
            submitText={submitText}
            testText="Test Provider"
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
            <CheckboxInput
                id={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                name={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                label="Enabled"
                customDescription="If selected, this provider configuration will be able to pull data into Alert and available to configure with distribution jobs, otherwise, it will not be available for those usages."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                isChecked={FieldModelUtilities.getFieldModelBooleanValue(providerModel, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                errorValue={error.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.enabled]}
            />
            <TextInput
                id={BLACKDUCK_GLOBAL_FIELD_KEYS.name}
                name={BLACKDUCK_GLOBAL_FIELD_KEYS.name}
                label="Provider Configuration"
                customDescription="The name of this provider configuration. Must be unique."
                required
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                value={FieldModelUtilities.getFieldModelSingleValue(providerModel, BLACKDUCK_GLOBAL_FIELD_KEYS.name)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.name)}
                errorValue={error.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.name]}
            />
            <TextInput
                id={BLACKDUCK_GLOBAL_FIELD_KEYS.url}
                name={BLACKDUCK_GLOBAL_FIELD_KEYS.url}
                label="URL"
                customDescription="The URL of the Black Duck server."
                required
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                value={FieldModelUtilities.getFieldModelSingleValue(providerModel, BLACKDUCK_GLOBAL_FIELD_KEYS.url)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.url)}
                errorValue={error.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.url]}
            />
            <PasswordInput
                id={BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey}
                name={BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey}
                label="API Token"
                customDescription="The API token used to retrieve data from the Black Duck server. The API token should be for a super user."
                required
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                value={FieldModelUtilities.getFieldModelSingleValue(providerModel, BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                isSet={FieldModelUtilities.isFieldModelValueSet(providerModel, BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey)}
                errorValue={error.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.apiKey]}
            />
            <NumberInput
                id={BLACKDUCK_GLOBAL_FIELD_KEYS.timeout}
                name={BLACKDUCK_GLOBAL_FIELD_KEYS.timeout}
                label="Timeout"
                customDescription="The timeout in seconds for all connections to the Black Duck server."
                required
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                value={FieldModelUtilities.getFieldModelNumberValue(providerModel, BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.timeout)}
                errorValue={error.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.timeout]}
            />
        </Modal>
    );
};

ProviderModal.propTypes = {
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

export default ProviderModal;
