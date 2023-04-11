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
import PageHeader from 'common/component/navigation/PageHeader';

const EmailGlobalConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, displayDelete
}) => {
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
            description={EMAIL_TEST_FIELD.description}
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
        <div>
            <PageHeader
                title={`${EMAIL_INFO.label}`}
                description="Configure the email server that Alert will send emails to."
                lastUpdated={emailConfig.lastUpdated}
            />
            <ConcreteConfigurationForm
                csrfToken={csrfToken}
                formDataId={emailConfig.id}
                setErrors={(formErrors) => setErrors(formErrors)}
                testFields={testField}
                clearTestForm={() => setTestEmailAddress('')}
                buttonIdPrefix={EMAIL_INFO.key}
                getRequest={fetchData}
                deleteRequest={() => ConfigurationRequestBuilder.createDeleteRequest(emailRequestUrl, csrfToken)}
                updateRequest={() => ConfigurationRequestBuilder.createUpdateWithoutIdRequest(emailRequestUrl, csrfToken, emailConfig)}
                createRequest={() => ConfigurationRequestBuilder.createNewConfigurationRequest(emailRequestUrl, csrfToken, emailConfig)}
                validateRequest={() => ConfigurationRequestBuilder.createValidateRequest(emailRequestUrl, csrfToken, emailConfig)}
                testRequest={() => ConfigurationRequestBuilder.createTestRequest(emailRequestUrl, csrfToken, emailConfig, 'sendTo', testEmailAddress)}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                displayDelete={displayDelete}
                errorHandler={errorHandler}
            >
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.host}
                    name="smtpHost"
                    label="SMTP Host"
                    description="The host name of the SMTP email server."
                    required
                    readOnly={readonly}
                    onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                    value={emailConfig.smtpHost || undefined}
                    errorName="smtpHost"
                    errorValue={errors.fieldErrors.host}
                />
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.from}
                    name="smtpFrom"
                    label="SMTP From"
                    description="The email address to use as the return address."
                    required
                    readOnly={readonly}
                    onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                    value={emailConfig.smtpFrom || undefined}
                    errorName="smtpFrom"
                    errorValue={errors.fieldErrors.from}
                />
                <NumberInput
                    id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.port}
                    name="smtpPort"
                    label="SMTP Port"
                    description="The SMTP server port to connect to."
                    readOnly={readonly}
                    onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                    value={emailConfig.smtpPort || undefined}
                    errorName="smtpPort"
                    errorValue={errors.fieldErrors.smtpPort}
                />
                <CheckboxInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.auth}
                    name="smtpAuth"
                    label="SMTP Auth"
                    description="Select this if your SMTP server requires authentication, then fill in the SMTP User and SMTP Password."
                    readOnly={readonly}
                    onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                    isChecked={(emailConfig.smtpAuth || 'false').toString().toLowerCase() === 'true'}
                    errorName="smtpAuth"
                    errorValue={errors.fieldErrors.smtpAuth}
                />
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.user}
                    name="smtpUsername"
                    label="SMTP User"
                    description="The username to authenticate with the SMTP server."
                    readOnly={readonly}
                    onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                    value={emailConfig.smtpUsername || undefined}
                    errorName="smtpUsername"
                    errorValue={errors.fieldErrors.user}
                />
                <PasswordInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.password}
                    name="smtpPassword"
                    label="SMTP Password"
                    description="The password to authenticate with the SMTP server."
                    readOnly={readonly}
                    onChange={fieldModelUtilities.handleTestChange(emailConfig, setEmailConfig)}
                    value={emailConfig.smtpPassword || undefined}
                    isSet={emailConfig.isSmtpPasswordSet}
                    errorName="smtpPassword"
                    errorValue={errors.fieldErrors.password}
                />
                <FluidFieldMappingField
                    id="additional.email.properties"
                    name={additionalPropertiesName}
                    label="Additional Email Properties"
                    buttonLabel="Add Property"
                    description="Mapping of additional properties that can be used to appropriately configure your email connection."
                    readonly={readonly}
                    value={emailConfig[additionalPropertiesName] || {}}
                    setValue={updateAdditionalProperties}
                    errorName="additionalJavaMailProperties"
                    errorValue={errors.fieldErrors[additionalPropertiesName]}
                />
            </ConcreteConfigurationForm>
        </div>
    );
};

EmailGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

EmailGlobalConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default EmailGlobalConfiguration;
