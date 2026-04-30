import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import { EMAIL_GLOBAL_ADVANCED_FIELD_KEYS, EMAIL_GLOBAL_FIELD_KEYS, EMAIL_INFO, EMAIL_TEST_FIELD } from 'page/channel/email/EmailModels';
import TextInput from 'common/component/input/TextInput';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import CheckboxInput from 'common/component/input/CheckboxInput';
import PasswordInput from 'common/component/input/PasswordInput';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as fieldModelUtilities from 'common/util/fieldModelUtilities';
import FluidFieldMappingField from 'common/component/input/mapping/FluidFieldMappingField';
import NumberInput from 'common/component/input/NumberInput';
import PageLayout from 'common/component/PageLayout';
import FormCard from 'common/component/FormCard';
import useGetPermissions from 'common/hooks/useGetPermissions';

const EmailGlobalConfiguration = ({
    csrfToken, errorHandler, descriptor
}) => {
    const { readOnly, canDelete, canSave, canTest } = useGetPermissions(descriptor);
    const emailRequestUrl = `${ConfigurationRequestBuilder.CONFIG_API_URL}/email`;
    const additionalPropertiesName = 'additionalJavaMailProperties';

    const [emailConfig, setEmailConfig] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [testEmailAddress, setTestEmailAddress] = useState('');

    const testField = (
        <TextInput
            id={EMAIL_TEST_FIELD.key}
            name={EMAIL_TEST_FIELD.key}
            label={EMAIL_TEST_FIELD.label}
            fieldDescription={EMAIL_TEST_FIELD.description}
            onChange={({ target }) => setTestEmailAddress(target.value)}
            value={testEmailAddress}
        />
    );

    const updateAdditionalProperties = (additionalProperties) => {
        setEmailConfig({ ...emailConfig, [additionalPropertiesName]: additionalProperties });
    };

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(emailRequestUrl, csrfToken);
        if (response.ok) {
            const data = await response.json();
            setEmailConfig(data);
        } else {
            setEmailConfig({ name: 'default-configuration' });
        }
    };

    return (
        <PageLayout
            title={EMAIL_INFO.label}
            description="Configure the email server that Alert will send emails to."
            headerIcon="envelope"
            lastUpdated={emailConfig.lastUpdated}
        >
            <FormCard formTitle="Email Configuration">
                <ConcreteConfigurationForm
                    csrfToken={csrfToken}
                    formDataId={emailConfig.id}
                    setErrors={(formErrors) => setErrors(formErrors)}
                    testFields={testField}
                    testModalTitle="Send Test Email"
                    clearTestForm={() => setTestEmailAddress('')}
                    buttonIdPrefix={EMAIL_INFO.key}
                    getRequest={fetchData}
                    deleteRequest={() => ConfigurationRequestBuilder.createDeleteRequest(emailRequestUrl, csrfToken)}
                    updateRequest={() => ConfigurationRequestBuilder.createUpdateWithoutIdRequest(emailRequestUrl, csrfToken, emailConfig)}
                    createRequest={() => ConfigurationRequestBuilder.createNewConfigurationRequest(emailRequestUrl, csrfToken, emailConfig)}
                    validateRequest={() => ConfigurationRequestBuilder.createValidateRequest(emailRequestUrl, csrfToken, emailConfig)}
                    testRequest={() => ConfigurationRequestBuilder.createTestRequest(emailRequestUrl, csrfToken, emailConfig, 'sendTo', testEmailAddress)}
                    deleteLabel="Reset"
                    readOnly={readOnly}
                    displayTest={canTest}
                    displaySave={canSave}
                    displayDelete={canDelete}
                    errorHandler={errorHandler}
                >
                    <TextInput
                        id={EMAIL_GLOBAL_FIELD_KEYS.host}
                        name="smtpHost"
                        label="Hostname"
                        fieldDescription="Hostname of the SMTP email server (e.g. smtp.gmail.com)"
                        required
                        readOnly={readOnly}
                        onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                        value={emailConfig.smtpHost || undefined}
                        errorName="smtpHost"
                        errorValue={errors.fieldErrors.host}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_FIELD_KEYS.from}
                        name="smtpFrom"
                        label="From"
                        fieldDescription="The email address to use as the return address."
                        required
                        readOnly={readOnly}
                        onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                        value={emailConfig.smtpFrom || undefined}
                        errorName="smtpFrom"
                        errorValue={errors.fieldErrors.from}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.port}
                        name="smtpPort"
                        label="Port"
                        fieldDescription="The SMTP server port to connect to."
                        readOnly={readOnly}
                        onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                        value={emailConfig.smtpPort || undefined}
                        errorName="smtpPort"
                        errorValue={errors.fieldErrors.smtpPort}
                        width="25%"
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_FIELD_KEYS.auth}
                        name="smtpAuth"
                        label="Authentication"
                        checkboxValueDescription="Select this if your SMTP server requires authentication, then fill in the SMTP User and SMTP Password below."
                        readOnly={readOnly}
                        onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                        isChecked={(emailConfig.smtpAuth || 'false').toString().toLowerCase() === 'true'}
                        checkboxValueLabel="Enable Server Authentication"
                        errorName="smtpAuth"
                        errorValue={errors.fieldErrors.smtpAuth}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_FIELD_KEYS.user}
                        name="smtpUsername"
                        label="User"
                        fieldDescription="The username to authenticate with the SMTP server."
                        readOnly={readOnly}
                        onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                        value={emailConfig.smtpUsername || undefined}
                        errorName="smtpUsername"
                        errorValue={errors.fieldErrors.user}
                        isDisabled={emailConfig.smtpAuth === 'false' || !emailConfig.smtpAuth}
                    />
                    <PasswordInput
                        id={EMAIL_GLOBAL_FIELD_KEYS.password}
                        name="smtpPassword"
                        label="Password"
                        fieldDescription="The password to authenticate with the SMTP server."
                        readOnly={readOnly}
                        onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                        value={emailConfig.smtpPassword || undefined}
                        isSet={emailConfig.isSmtpPasswordSet}
                        errorName="smtpPassword"
                        errorValue={errors.fieldErrors.password}
                        isDisabled={emailConfig.smtpAuth === 'false' || !emailConfig.smtpAuth}
                    />
                    <FluidFieldMappingField
                        id="additional.email.properties"
                        name={additionalPropertiesName}
                        label="Additional Email Properties"
                        buttonLabel="Add Property"
                        description="Mapping of additional properties that can be used to appropriately configure your email connection."
                        readOnly={readOnly}
                        value={emailConfig[additionalPropertiesName] || {}}
                        setValue={updateAdditionalProperties}
                        errorName="additionalJavaMailProperties"
                        errorValue={errors.fieldErrors[additionalPropertiesName]}
                    />
                </ConcreteConfigurationForm>
            </FormCard>
        </PageLayout>
    );
};

EmailGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    descriptor: PropTypes.object
};

export default EmailGlobalConfiguration;
