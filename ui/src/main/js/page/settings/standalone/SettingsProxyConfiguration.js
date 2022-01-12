import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import ConfigurationForm from 'common/ConfigurationForm';
import { SETTINGS_FIELD_KEYS, SETTINGS_INFO, SETTINGS_PROXY_TEST_FIELD } from 'page/settings/SettingsModel';
import TextInput from 'common/input/TextInput';
import * as fieldModelUtilities from 'common/util/fieldModelUtilities';
import NumberInput from 'common/input/NumberInput';
import PasswordInput from 'common/input/PasswordInput';
import DynamicSelectInput from 'common/input/DynamicSelectInput';

const SettingsProxyConfiguration = ({
    csrfToken, errorHandler, readOnly, displayTest, displaySave, displayDelete
}) => {
    const proxyRequestUrl = `${ConfigurationRequestBuilder.PROXY_API_URL}`;

    const [settingsProxyConfig, setSettingsProxyConfig] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [testUrl, setTestUrl] = useState('');

    const testField = (
        <TextInput
            id={SETTINGS_PROXY_TEST_FIELD.key}
            name={SETTINGS_PROXY_TEST_FIELD.key}
            label={SETTINGS_PROXY_TEST_FIELD.label}
            description={SETTINGS_PROXY_TEST_FIELD.description}
            onChange={({ target }) => setTestUrl(target.value)}
            value={testUrl}
        />
    );

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(proxyRequestUrl, csrfToken);
        if (response.ok) {
            const data = await response.json();
            setSettingsProxyConfig(data);
        } else {
            setSettingsProxyConfig({ name: 'default-configuration' });
        }
    };

    const nonProxyHostOptions = [
        { label: 'Hosted Azure Boards OAuth (app.vssps.visualstudio.com)', value: 'app.vssps.visualstudio.com' },
        { label: 'Hosted Azure Boards API (dev.azure.com)', value: 'dev.azure.com' },
        { label: 'Jira Cloud (*.atlassian.net)', value: '*.atlassian.net' },
        { label: 'Hosted MS Teams (*.office.com)', value: '*.office.com' },
        { label: 'Hosted Slack (*.slack.com)', value: '*.slack.com' }
    ];

    return (
        <ConfigurationForm
            csrfToken={csrfToken}
            formDataId={settingsProxyConfig.id}
            setErrors={(formErrors) => setErrors(formErrors)}
            testFields={testField}
            clearTestForm={() => setTestUrl('')}
            buttonIdPrefix={SETTINGS_INFO.key}
            getRequest={fetchData}
            deleteRequest={() => ConfigurationRequestBuilder.createDeleteRequest(proxyRequestUrl, csrfToken)}
            createRequest={() => ConfigurationRequestBuilder.createNewConfigurationRequest(proxyRequestUrl, csrfToken, settingsProxyConfig)}
            updateRequest={() => ConfigurationRequestBuilder.createUpdateWithoutIdRequest(proxyRequestUrl, csrfToken, settingsProxyConfig)}
            validateRequest={() => ConfigurationRequestBuilder.createValidateRequest(proxyRequestUrl, csrfToken, settingsProxyConfig)}
            testRequest={() => ConfigurationRequestBuilder.createTestRequest(proxyRequestUrl, csrfToken, settingsProxyConfig, `testUrl=${testUrl}`)}
            readonly={readOnly}
            displaySave={displaySave}
            displayTest={displayTest}
            displayDelete={displayDelete}
            errorHandler={errorHandler}
        >
            <h2 key="settings-header">Proxy Configuration</h2>
            <TextInput
                id={SETTINGS_FIELD_KEYS.proxyHost}
                name="proxyHost"
                label="Proxy Host"
                description="The host name of the proxy server to use."
                readOnly={readOnly}
                onChange={fieldModelUtilities.handleTestChange(settingsProxyConfig, setSettingsProxyConfig)}
                value={settingsProxyConfig.proxyHost || undefined}
                errorName="proxyHost"
                errorValue={errors.fieldErrors.proxyHost}
            />
            <NumberInput
                id={SETTINGS_FIELD_KEYS.proxyPort}
                name="proxyPort"
                label="Proxy Port"
                description="The port of the proxy server to use."
                readOnly={readOnly}
                onChange={fieldModelUtilities.handleTestChange(settingsProxyConfig, setSettingsProxyConfig)}
                value={settingsProxyConfig.proxyPort || undefined}
                errorName="smtpPort"
                errorValue={errors.fieldErrors.proxyPort}
            />
            <TextInput
                id={SETTINGS_FIELD_KEYS.proxyUsername}
                name="proxyUsername"
                label="Proxy Username"
                description="If the proxy server requires authentication, the username to authenticate with the proxy server."
                readOnly={readOnly}
                onChange={fieldModelUtilities.handleTestChange(settingsProxyConfig, setSettingsProxyConfig)}
                value={settingsProxyConfig.proxyUsername || undefined}
                errorName="proxyUsername"
                errorValue={errors.fieldErrors.proxyUsername}
            />
            <PasswordInput
                id={SETTINGS_FIELD_KEYS.proxyPassword}
                name="proxyPassword"
                label="Proxy Password"
                description="If the proxy server requires authentication, the password to authenticate with the proxy server."
                readOnly={readOnly}
                onChange={fieldModelUtilities.handleTestChange(settingsProxyConfig, setSettingsProxyConfig)}
                value={settingsProxyConfig.proxyPassword || undefined}
                isSet={settingsProxyConfig.isProxyPasswordSet}
                errorName="proxyPassword"
                errorValue={errors.fieldErrors.proxyPassword}
            />
            <DynamicSelectInput
                id={SETTINGS_FIELD_KEYS.proxyNonProxyHosts}
                name="nonProxyHosts"
                label="Non-Proxy Hosts"
                description="Hosts whose network traffic should not go through the proxy."
                readOnly={readOnly}
                onChange={fieldModelUtilities.handleTestChange(settingsProxyConfig, setSettingsProxyConfig)}
                value={settingsProxyConfig.nonProxyHosts || []}
                creatable
                searchable
                multiSelect
                options={nonProxyHostOptions}
                placeholder="Choose an option or type to add your own"
            />
        </ConfigurationForm>
    );
};

SettingsProxyConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readOnly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

SettingsProxyConfiguration.defaultProps = {
    readOnly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default SettingsProxyConfiguration;
