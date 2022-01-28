import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/configuration/global/CommonGlobalConfiguration';
import { JIRA_SERVER_GLOBAL_FIELD_KEYS, JIRA_SERVER_INFO, JIRA_SERVER_URLS } from 'page/channel/jira/server/JiraServerModel';
import TextInput from 'common/component/input/TextInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import PasswordInput from 'common/component/input/PasswordInput';
import CheckboxInput from 'common/component/input/CheckboxInput';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import ButtonField from 'common/component/input/field/ButtonField';
import { useHistory, useLocation, useParams } from 'react-router-dom';

const ConcreteJiraServerGlobalConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave
}) => {
    const { id } = useParams();
    const history = useHistory();
    const location = useLocation();

    const [jiraServerConfig, setJiraServerConfig] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [buttonErrorMessage, setButtonErrorMessage] = useState('');
    const [buttonSuccess, setButtonSuccess] = useState(false);
    const [buttonMessage, setButtonMessage] = useState('');

    const jiraServerRequestUrl = `${ConfigurationRequestBuilder.CONFIG_API_URL}/jira_server`;
    const jiraServerDisablePluginUrl = `${jiraServerRequestUrl}/install-plugin`;

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(jiraServerRequestUrl, csrfToken, id);
        const data = await response.json();
        if (data) {
            if (location.pathname.includes('/copy')) {
                delete data.id;
            }
            setJiraServerConfig(data);
        }
    };

    const installPlugin = async () => {
        setButtonErrorMessage('');
        setButtonSuccess(false);
        const response = await ConfigurationRequestBuilder.createNewConfigurationRequest(jiraServerDisablePluginUrl, csrfToken, jiraServerConfig);
        const data = await response.json();

        setButtonSuccess(!data.hasErrors);
        const stateUpdate = (data.hasErrors) ? setButtonErrorMessage : setButtonMessage;
        setErrors(HttpErrorUtilities.createErrorObject(data));
        stateUpdate(data.message);
    };

    return (
        <CommonGlobalConfiguration
            label={JIRA_SERVER_INFO.label}
            description="Configure the Jira Server instance that Alert will send issue updates to."
            lastUpdated={jiraServerConfig.lastUpdated}
        >
            <ConcreteConfigurationForm
                csrfToken={csrfToken}
                formDataId={id}
                setErrors={(formErrors) => setErrors(formErrors)}
                buttonIdPrefix={JIRA_SERVER_INFO.key}
                getRequest={fetchData}
                deleteRequest={() => ConfigurationRequestBuilder.createDeleteRequest(jiraServerRequestUrl, csrfToken, jiraServerConfig.id)}
                updateRequest={() => ConfigurationRequestBuilder.createUpdateRequest(jiraServerRequestUrl, csrfToken, id, jiraServerConfig)}
                createRequest={() => ConfigurationRequestBuilder.createNewConfigurationRequest(jiraServerRequestUrl, csrfToken, jiraServerConfig)}
                validateRequest={() => ConfigurationRequestBuilder.createValidateRequest(jiraServerRequestUrl, csrfToken, jiraServerConfig)}
                testRequest={() => ConfigurationRequestBuilder.createTestRequest(jiraServerRequestUrl, csrfToken, jiraServerConfig)}
                afterSuccessfulSave={() => history.push(JIRA_SERVER_URLS.jiraServerUrl)}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                displayDelete={false}
                errorHandler={errorHandler}
            >
                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.name}
                    name="name"
                    label="Name"
                    description="The unique name for the Jira Server server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerConfig, setJiraServerConfig)}
                    value={jiraServerConfig.name || undefined}
                    errorName="name"
                    errorValue={errors.fieldErrors.name}
                />
                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.url}
                    name="url"
                    label="URL"
                    description="The URL of the Jira Server server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerConfig, setJiraServerConfig)}
                    value={jiraServerConfig.url || undefined}
                    errorName="url"
                    errorValue={errors.fieldErrors.url}
                />
                <TextInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.username}
                    name="userName"
                    label="User Name"
                    description="The username of the Jira Server user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerConfig, setJiraServerConfig)}
                    value={jiraServerConfig.userName || undefined}
                    errorName="userName"
                    errorValue={errors.fieldErrors.userName}
                />
                <PasswordInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.password}
                    name="password"
                    label="Password"
                    description="The password of the specified Jira Server user."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerConfig, setJiraServerConfig)}
                    value={jiraServerConfig.password || undefined}
                    isSet={jiraServerConfig.isPasswordSet}
                    errorName="password"
                    errorValue={errors.fieldErrors.password}
                />
                <CheckboxInput
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.disablePluginCheck}
                    name="disablePluginCheck"
                    label="Disable Plugin Check"
                    description="This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance. Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly, and duplicate issues may be created."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(jiraServerConfig, setJiraServerConfig)}
                    isChecked={(jiraServerConfig.disablePluginCheck || 'false').toString().toLowerCase() === 'true'}
                    errorName="disablePluginCheck"
                    errorValue={errors.fieldErrors.disablePluginCheck}
                />
                <ButtonField
                    id={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    name={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    label="Configure Jira server plugin"
                    buttonLabel="Install Plugin Remotely"
                    description="Installs a required plugin on the Jira server."
                    onSendClick={installPlugin}
                    fieldKey={JIRA_SERVER_GLOBAL_FIELD_KEYS.configurePlugin}
                    fieldError={buttonErrorMessage}
                    readOnly={readonly || !displayTest}
                    success={buttonSuccess}
                    statusMessage={buttonMessage}
                />
            </ConcreteConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

ConcreteJiraServerGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool
};

ConcreteJiraServerGlobalConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true
};

export default ConcreteJiraServerGlobalConfiguration;
