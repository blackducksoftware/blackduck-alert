import PasswordInput from 'field/input/PasswordInput';
import CheckboxInput from 'field/input/CheckboxInput';
import TextInput from 'field/input/TextInput';
import PropTypes from 'prop-types';
import React, { Component } from 'react';
import SelectInput from 'field/input/SelectInput';
import CollapsiblePane from 'component/common/CollapsiblePane';
import ConfigButtons from 'component/common/ConfigButtons';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

const KEY_DEFAULT_SYSTEM_ADMIN_EMAIL = 'settings.user.default.admin.email';
const KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD = 'settings.user.default.admin.password';
const KEY_ENCRYPTION_PASSWORD = 'settings.encryption.password';
const KEY_ENCRYPTION_GLOBAL_SALT = 'settings.encryption.global.salt';
const KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE = 'settings.startup.environment.variable.override';

// Proxy Keys
const KEY_PROXY_HOST = 'settings.proxy.host';
const KEY_PROXY_PORT = 'settings.proxy.port';
const KEY_PROXY_USERNAME = 'settings.proxy.username';
const KEY_PROXY_PASSWORD = 'settings.proxy.password';

// LDAP Keys
const KEY_LDAP_ENABLED = 'settings.ldap.enabled';
const KEY_LDAP_SERVER = 'settings.ldap.server';
const KEY_LDAP_MANAGER_DN = 'settings.ldap.manager.dn';
const KEY_LDAP_MANAGER_PASSWORD = 'settings.ldap.manager.password';
const KEY_LDAP_AUTHENTICATION_TYPE = 'settings.ldap.authentication.type';
const KEY_LDAP_REFERRAL = 'settings.ldap.referral';
const KEY_LDAP_USER_SEARCH_BASE = 'settings.ldap.user.search.base';
const KEY_LDAP_USER_SEARCH_FILTER = 'settings.ldap.user.search.filter';
const KEY_LDAP_USER_DN_PATTERNS = 'settings.ldap.user.dn.patterns';
const KEY_LDAP_USER_ATTRIBUTES = 'settings.ldap.user.attributes';
const KEY_LDAP_GROUP_SEARCH_BASE = 'settings.ldap.group.search.base';
const KEY_LDAP_GROUP_SEARCH_FILTER = 'settings.ldap.group.search.filter';
const KEY_LDAP_GROUP_ROLE_ATTRIBUTE = 'settings.ldap.group.role.attribute';
const KEY_LDAP_ROLE_PREFIX = 'settings.ldap.role.prefix';


const fieldDescriptions = {
    [KEY_DEFAULT_SYSTEM_ADMIN_EMAIL]: 'The email address of the Alert system administrator. Used in case a password reset is needed.',
    [KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD]: 'The password of the Alert system administrator. Used when logging in as the "sysadmin" user.',
    [KEY_ENCRYPTION_PASSWORD]: 'The password used when encrypting sensitive fields.',
    [KEY_ENCRYPTION_GLOBAL_SALT]: 'The salt used when encrypting sensitive fields.',
    [KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE]: 'If true, the Alert environment variables will override the stored configurations.',
    [KEY_PROXY_HOST]: 'The host name of the proxy server to use.',
    [KEY_PROXY_PORT]: 'The port of the proxy server to use.',
    [KEY_PROXY_USERNAME]: 'If the proxy server requires authentication, the username to authentication with the proxy server.',
    [KEY_PROXY_PASSWORD]: 'If the proxy server requires authentication, the password to authentication with the proxy server.',
    [KEY_LDAP_ENABLED]: 'If true, Alert with attempt to authenticate using the LDAP configuration.',
    [KEY_LDAP_SERVER]: 'The URL of the LDAP server.',
    [KEY_LDAP_MANAGER_DN]: 'The distinguished name of the LDAP manager.',
    [KEY_LDAP_MANAGER_PASSWORD]: 'The password of the LDAP manager.',
    [KEY_LDAP_AUTHENTICATION_TYPE]: 'The type of authentication required to connect to the LDAP server.',
    [KEY_LDAP_REFERRAL]: 'Set the method to handle referrals.',
    [KEY_LDAP_USER_SEARCH_BASE]: 'The part of the LDAP directory in which user searches should be done.',
    [KEY_LDAP_USER_SEARCH_FILTER]: 'The filter used to search for user membership.',
    [KEY_LDAP_USER_DN_PATTERNS]: 'The pattern used used to supply a DN for the user. The pattern should be the name relative to the root DN.',
    [KEY_LDAP_USER_ATTRIBUTES]: 'User attributes to retrieve for users.',
    [KEY_LDAP_GROUP_SEARCH_BASE]: 'The part of the LDAP directory in which group searches should be done.',
    [KEY_LDAP_GROUP_SEARCH_FILTER]: 'The filter used to search for group membership.',
    [KEY_LDAP_GROUP_ROLE_ATTRIBUTE]: 'The ID of the attribute which contains the role name for a group.',
    [KEY_LDAP_ROLE_PREFIX]: 'The prefix which will be prepended to the user roles.'
};

const fieldNames = [
    KEY_DEFAULT_SYSTEM_ADMIN_EMAIL,
    KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD,
    KEY_ENCRYPTION_PASSWORD,
    KEY_ENCRYPTION_GLOBAL_SALT,
    KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE,
    KEY_PROXY_HOST,
    KEY_PROXY_PORT,
    KEY_PROXY_USERNAME,
    KEY_PROXY_PASSWORD,
    KEY_LDAP_ENABLED,
    KEY_LDAP_SERVER,
    KEY_LDAP_MANAGER_DN,
    KEY_LDAP_MANAGER_PASSWORD,
    KEY_LDAP_AUTHENTICATION_TYPE,
    KEY_LDAP_REFERRAL,
    KEY_LDAP_USER_SEARCH_BASE,
    KEY_LDAP_USER_SEARCH_FILTER,
    KEY_LDAP_USER_DN_PATTERNS,
    KEY_LDAP_USER_ATTRIBUTES,
    KEY_LDAP_GROUP_SEARCH_BASE,
    KEY_LDAP_GROUP_SEARCH_FILTER,
    KEY_LDAP_GROUP_ROLE_ATTRIBUTE,
    KEY_LDAP_ROLE_PREFIX
];

class SettingsConfigurationForm extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.createSingleSelectHandler = this.createSingleSelectHandler.bind(this);
        this.state = {
            settingsData: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_SETTINGS)
        };
    }

    componentWillMount() {
        this.props.getSettings();
    }

    componentWillReceiveProps(nextProps) {
        if ((nextProps.fetchingSetupStatus === 'SYSTEM_SETUP_FETCHED' && nextProps.updateStatus === 'FETCHED') ||
            (this.props.fetchingSetupStatus === 'SYSTEM_SETUP_FETCHED' && this.props.updateStatus === 'FETCHED')) {
            const newState = FieldModelUtilities.checkModelOrCreateEmpty(nextProps.settingsData, fieldNames);
            this.setState({
                settingsData: newState
            });
        }
    }

    getAuthenticationTypes() {
        return [{ label: 'Simple', value: 'simple' },
            { label: 'None', value: 'none' },
            { label: 'Digest-MD5', value: 'digest' }];
    }

    getReferralOptions() {
        return [{ label: 'Ignore', value: 'ignore' },
            { label: 'Follow', value: 'follow' },
            { label: 'Throw', value: 'throw' }];
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.settingsData, target.name, value);
        this.setState({
            settingsData: newState
        });
    }

    createSingleSelectHandler(fieldKey) {
        return (selectedValue) => {
            if (selectedValue) {
                const selected = selectedValue.value;
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.settingsData, fieldKey, selected);
                this.setState({
                    settingsData: newState
                });
            } else {
                const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.settingsData, fieldKey, null);
                this.setState({
                    settingsData: newState
                });
            }
        };
    }

    handleSubmit(evt) {
        evt.preventDefault();
        this.props.saveSettings(this.state.settingsData);
    }

    render() {
        const fieldModel = this.state.settingsData;
        const selectedAuthenticationType = FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_AUTHENTICATION_TYPE);
        const authenticationTypeOptions = this.getAuthenticationTypes();
        const selectedAuthenticationOption = authenticationTypeOptions.filter(option => option.value === selectedAuthenticationType);

        const selectedReferral = FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_REFERRAL);
        const referralOptions = this.getReferralOptions();
        const selectedReferralOption = referralOptions.filter(option => option.value === selectedReferral);

        const saving = this.props.updateStatus === 'UPDATING' || this.props.updateStatus === 'FETCHING';
        return (
            <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <div className="col-sm-12">
                        <h2>Default System Administrator Configuration</h2>
                        <TextInput
                            id={KEY_DEFAULT_SYSTEM_ADMIN_EMAIL}
                            label="Email Address"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_DEFAULT_SYSTEM_ADMIN_EMAIL)}
                            name={KEY_DEFAULT_SYSTEM_ADMIN_EMAIL}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_DEFAULT_SYSTEM_ADMIN_EMAIL)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_DEFAULT_SYSTEM_ADMIN_EMAIL)}
                            errorValue={this.props.fieldErrors[KEY_DEFAULT_SYSTEM_ADMIN_EMAIL]}
                        />
                        <PasswordInput
                            id={KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD}
                            label="Password"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD)}
                            name={KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD)}
                            isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD)}
                            errorValue={this.props.fieldErrors[KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD]}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-sm-12">
                        <h2>Encryption Configuration</h2>
                        <PasswordInput
                            id={KEY_ENCRYPTION_PASSWORD}
                            label="Password"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_ENCRYPTION_PASSWORD)}
                            name={KEY_ENCRYPTION_PASSWORD}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_ENCRYPTION_PASSWORD)}
                            isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, KEY_ENCRYPTION_PASSWORD)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_ENCRYPTION_PASSWORD)}
                            errorValue={this.props.fieldErrors[KEY_ENCRYPTION_PASSWORD]}
                        />
                        <PasswordInput
                            id={KEY_ENCRYPTION_GLOBAL_SALT}
                            label="Salt"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_ENCRYPTION_GLOBAL_SALT)}
                            name={KEY_ENCRYPTION_GLOBAL_SALT}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_ENCRYPTION_GLOBAL_SALT)}
                            isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, KEY_ENCRYPTION_GLOBAL_SALT)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_ENCRYPTION_GLOBAL_SALT)}
                            errorValue={this.props.fieldErrors[KEY_ENCRYPTION_GLOBAL_SALT]}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-sm-12">
                        <CheckboxInput
                            id={KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE}
                            label="Startup Environment Variable Override"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE)}
                            name={KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE)}
                            errorValue={this.props.fieldErrors[KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE]}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-sm-12">
                        <CollapsiblePane title="Proxy Configuration">
                            <TextInput
                                id={KEY_PROXY_HOST}
                                label="Host Name"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PROXY_HOST)}
                                name={KEY_PROXY_HOST}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROXY_HOST)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROXY_HOST)}
                                errorValue={this.props.fieldErrors[KEY_PROXY_HOST]}
                            />
                            <TextInput
                                id={KEY_PROXY_PORT}
                                label="Port"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PROXY_PORT)}
                                name={KEY_PROXY_PORT}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROXY_PORT)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROXY_PORT)}
                                errorValue={this.props.fieldErrors[KEY_PROXY_PORT]}
                            />
                            <TextInput
                                id={KEY_PROXY_USERNAME}
                                label="Username"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PROXY_USERNAME)}
                                name={KEY_PROXY_USERNAME}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROXY_USERNAME)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROXY_USERNAME)}
                                errorValue={this.props.fieldErrors[KEY_PROXY_USERNAME]}
                            />
                            <PasswordInput
                                id={KEY_PROXY_PASSWORD}
                                label="Password"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PROXY_PASSWORD)}
                                name={KEY_PROXY_PASSWORD}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROXY_PASSWORD)}
                                isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, KEY_PROXY_PASSWORD)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROXY_PASSWORD)}
                                errorValue={this.props.fieldErrors[KEY_PROXY_PASSWORD]}
                            />
                        </CollapsiblePane>
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-sm-12">
                        <CollapsiblePane title="LDAP Configuration">
                            <CheckboxInput
                                id={KEY_LDAP_ENABLED}
                                label="Enabled"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_ENABLED)}
                                name={KEY_LDAP_ENABLED}
                                isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, KEY_LDAP_ENABLED)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_ENABLED)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_ENABLED]}
                            />
                            <TextInput
                                id={KEY_LDAP_SERVER}
                                label="Server"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_SERVER)}
                                name={KEY_LDAP_SERVER}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_SERVER)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_SERVER)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_SERVER]}
                            />
                            <TextInput
                                id={KEY_LDAP_MANAGER_DN}
                                label="Manager DN"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_MANAGER_DN)}
                                name={KEY_LDAP_MANAGER_DN}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_MANAGER_DN)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_MANAGER_DN)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_MANAGER_DN]}
                            />
                            <PasswordInput
                                id={KEY_LDAP_MANAGER_PASSWORD}
                                label="Manager Password"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_MANAGER_PASSWORD)}
                                name={KEY_LDAP_MANAGER_PASSWORD}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_MANAGER_PASSWORD)}
                                isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, KEY_LDAP_MANAGER_PASSWORD)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_MANAGER_PASSWORD)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_MANAGER_PASSWORD]}
                            />
                            <SelectInput
                                label="Authentication Type"
                                onChange={this.createSingleSelectHandler(KEY_LDAP_AUTHENTICATION_TYPE)}
                                id={KEY_LDAP_AUTHENTICATION_TYPE}
                                inputClass="typeAheadField"
                                labelClass="col-sm-3"
                                selectSpacingClass="col-sm-8"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_AUTHENTICATION_TYPE)}
                                options={this.getAuthenticationTypes()}
                                placeholder="Choose authentication type"
                                value={selectedAuthenticationOption}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_AUTHENTICATION_TYPE)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_AUTHENTICATION_TYPE]}
                            />

                            <SelectInput
                                label="Referral"
                                onChange={this.createSingleSelectHandler(KEY_LDAP_REFERRAL)}
                                id={KEY_LDAP_REFERRAL}
                                inputClass="typeAheadField"
                                labelClass="col-sm-3"
                                selectSpacingClass="col-sm-8"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_REFERRAL)}
                                options={this.getReferralOptions()}
                                placeholder="Choose referral type"
                                value={selectedReferralOption}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_REFERRAL)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_REFERRAL]}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_SEARCH_BASE}
                                label="User Search Base"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_USER_SEARCH_BASE)}
                                name={KEY_LDAP_USER_SEARCH_BASE}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_SEARCH_BASE)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_SEARCH_BASE)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_USER_SEARCH_BASE]}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_SEARCH_FILTER}
                                label="User Search Filter"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_USER_SEARCH_FILTER)}
                                name={KEY_LDAP_USER_SEARCH_FILTER}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_SEARCH_FILTER)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_SEARCH_FILTER)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_USER_SEARCH_FILTER]}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_DN_PATTERNS}
                                label="User DN Patterns"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_USER_DN_PATTERNS)}
                                name={KEY_LDAP_USER_DN_PATTERNS}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_DN_PATTERNS)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_DN_PATTERNS)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_USER_DN_PATTERNS]}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_ATTRIBUTES}
                                label="User Attributes"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_USER_ATTRIBUTES)}
                                name={KEY_LDAP_USER_ATTRIBUTES}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_ATTRIBUTES)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_ATTRIBUTES)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_USER_ATTRIBUTES]}
                            />
                            <TextInput
                                id={KEY_LDAP_GROUP_SEARCH_BASE}
                                label="Group Search Base"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_GROUP_SEARCH_BASE)}
                                name={KEY_LDAP_GROUP_SEARCH_BASE}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_GROUP_SEARCH_BASE)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_GROUP_SEARCH_BASE)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_GROUP_SEARCH_BASE]}
                            />
                            <TextInput
                                id={KEY_LDAP_GROUP_SEARCH_FILTER}
                                label="Group Search Filter"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_GROUP_SEARCH_FILTER)}
                                name={KEY_LDAP_GROUP_SEARCH_FILTER}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_GROUP_SEARCH_FILTER)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_GROUP_SEARCH_FILTER)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_GROUP_SEARCH_FILTER]}
                            />
                            <TextInput
                                id={KEY_LDAP_GROUP_ROLE_ATTRIBUTE}
                                label="Group Role Attribute"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_GROUP_ROLE_ATTRIBUTE)}
                                name={KEY_LDAP_GROUP_ROLE_ATTRIBUTE}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_GROUP_ROLE_ATTRIBUTE)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_GROUP_ROLE_ATTRIBUTE)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_GROUP_ROLE_ATTRIBUTE]}
                            />
                            <TextInput
                                id={KEY_LDAP_ROLE_PREFIX}
                                label="Role Prefix"
                                description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_LDAP_ROLE_PREFIX)}
                                name={KEY_LDAP_ROLE_PREFIX}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_ROLE_PREFIX)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_ROLE_PREFIX)}
                                errorValue={this.props.fieldErrors[KEY_LDAP_ROLE_PREFIX]}
                            />
                        </CollapsiblePane>
                    </div>
                </div>
                <ConfigButtons isFixed={false} includeSave type="submit" performingAction={saving} />
            </form>
        );
    }
}

SettingsConfigurationForm.propTypes = {
    fetchingSetupStatus: PropTypes.string.isRequired,
    getSettings: PropTypes.func.isRequired,
    saveSettings: PropTypes.func.isRequired,
    updateStatus: PropTypes.string,
    settingsData: PropTypes.object,
    fieldErrors: PropTypes.object
};

SettingsConfigurationForm.defaultProps = {
    fieldErrors: {},
    settingsData: {},
    updateStatus: ''
};

export default SettingsConfigurationForm;

