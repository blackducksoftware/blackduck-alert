import PasswordInput from 'field/input/PasswordInput';
import CheckboxInput from 'field/input/CheckboxInput';
import TextInput from 'field/input/TextInput';
import PropTypes from 'prop-types';
import React, { Component } from 'react';
import Select from 'react-select';
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
    KEY_LDAP_GROUP_ROLE_ATTRIBUTE
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
        const { errorMessage, actionMessage } = this.props;

        const fieldModel = this.state.settingsData;
        let selectedAuthenticationType = FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_AUTHENTICATION_TYPE);
        if (selectedAuthenticationType) {
            selectedAuthenticationType = selectedAuthenticationType.toString().toLowerCase();
        }
        const authenticationTypeOptions = this.getAuthenticationTypes();

        const selectedAuthenticationOption = authenticationTypeOptions.filter(option => option.value === selectedAuthenticationType);

        const selectedReferral = FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_REFERRAL);
        const referralOptions = this.getReferralOptions();
        const selectedReferralOption = referralOptions.filter(option => option.value === selectedReferral);

        const saving = this.props.updateStatus === 'UPDATING' || this.props.updateStatus === 'FETCHING';
        return (
            <div>
                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}
                <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                    <div className="form-group">
                        <div className="col-sm-12">
                            <h2>Default System Administrator Configuration</h2>
                            <TextInput
                                id={KEY_DEFAULT_SYSTEM_ADMIN_EMAIL}
                                label="Email Address"
                                name={KEY_DEFAULT_SYSTEM_ADMIN_EMAIL}
                                value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_DEFAULT_SYSTEM_ADMIN_EMAIL)}
                                onChange={this.handleChange}
                                errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_DEFAULT_SYSTEM_ADMIN_EMAIL)}
                                errorValue={this.props.fieldErrors[KEY_DEFAULT_SYSTEM_ADMIN_EMAIL]}
                            />
                            <PasswordInput
                                id={KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD}
                                label="Password"
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
                            <CollapsiblePane
                                title="Proxy Configuration"
                                expanded={() => FieldModelUtilities.keysHaveValueOrIsSet(fieldModel, [KEY_PROXY_HOST, KEY_PROXY_PORT, KEY_PROXY_USERNAME, KEY_PROXY_PASSWORD])}
                            >
                                <TextInput
                                    id={KEY_PROXY_HOST}
                                    label="Host Name"
                                    name={KEY_PROXY_HOST}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROXY_HOST)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROXY_HOST)}
                                    errorValue={this.props.fieldErrors[KEY_PROXY_HOST]}
                                />
                                <TextInput
                                    id={KEY_PROXY_PORT}
                                    label="Port"
                                    name={KEY_PROXY_PORT}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROXY_PORT)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROXY_PORT)}
                                    errorValue={this.props.fieldErrors[KEY_PROXY_PORT]}
                                />
                                <TextInput
                                    id={KEY_PROXY_USERNAME}
                                    label="Username"
                                    name={KEY_PROXY_USERNAME}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PROXY_USERNAME)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROXY_USERNAME)}
                                    errorValue={this.props.fieldErrors[KEY_PROXY_USERNAME]}
                                />
                                <PasswordInput
                                    id={KEY_PROXY_PASSWORD}
                                    label="Password"
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
                            <CollapsiblePane
                                title="LDAP Configuration"
                                expanded={() => FieldModelUtilities.keysHaveValueOrIsSet(fieldModel, [KEY_LDAP_ENABLED, KEY_LDAP_SERVER, KEY_LDAP_MANAGER_DN, KEY_LDAP_MANAGER_PASSWORD,
                                    KEY_LDAP_AUTHENTICATION_TYPE, KEY_LDAP_REFERRAL, KEY_LDAP_USER_SEARCH_BASE, KEY_LDAP_USER_SEARCH_FILTER, KEY_LDAP_USER_DN_PATTERNS, KEY_LDAP_USER_ATTRIBUTES,
                                    KEY_LDAP_GROUP_SEARCH_BASE, KEY_LDAP_GROUP_SEARCH_FILTER, KEY_LDAP_GROUP_ROLE_ATTRIBUTE])}
                            >
                                <CheckboxInput
                                    id={KEY_LDAP_ENABLED}
                                    label="Enabled"
                                    name={KEY_LDAP_ENABLED}
                                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, KEY_LDAP_ENABLED)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_ENABLED)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_ENABLED]}
                                />
                                <TextInput
                                    id={KEY_LDAP_SERVER}
                                    label="Server"
                                    name={KEY_LDAP_SERVER}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_SERVER)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_SERVER)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_SERVER]}
                                />
                                <TextInput
                                    id={KEY_LDAP_MANAGER_DN}
                                    label="Manager DN"
                                    name={KEY_LDAP_MANAGER_DN}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_MANAGER_DN)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_MANAGER_DN)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_MANAGER_DN]}
                                />
                                <PasswordInput
                                    id={KEY_LDAP_MANAGER_PASSWORD}
                                    label="Manager Password"
                                    name={KEY_LDAP_MANAGER_PASSWORD}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_MANAGER_PASSWORD)}
                                    isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, KEY_LDAP_MANAGER_PASSWORD)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_MANAGER_PASSWORD)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_MANAGER_PASSWORD]}
                                />
                                <label className="col-sm-3 col-form-label text-right">Authentication Type</label>
                                <div className="d-inline-flex flex-column p-2 col-sm-9">
                                    <Select
                                        id={KEY_LDAP_AUTHENTICATION_TYPE}
                                        className="typeAheadField"
                                        onChange={this.createSingleSelectHandler(KEY_LDAP_AUTHENTICATION_TYPE)}
                                        options={this.getAuthenticationTypes()}
                                        placeholder="Choose authentication type"
                                        value={selectedAuthenticationOption}
                                    />
                                </div>
                                <label className="fieldError">{this.props.fieldErrors[KEY_LDAP_AUTHENTICATION_TYPE]}</label>
                                <label className="col-sm-3 col-form-label text-right">Referral</label>
                                <div className="d-inline-flex flex-column p-2 col-sm-9">
                                    <Select
                                        id={KEY_LDAP_REFERRAL}
                                        className="typeAheadField"
                                        onChange={this.createSingleSelectHandler(KEY_LDAP_REFERRAL)}
                                        options={this.getReferralOptions()}
                                        placeholder="Choose referral type"
                                        value={selectedReferralOption}
                                    />
                                </div>
                                <label className="fieldError">{this.props.fieldErrors[KEY_LDAP_REFERRAL]}</label>
                                <TextInput
                                    id={KEY_LDAP_USER_SEARCH_BASE}
                                    label="User Search Base"
                                    name={KEY_LDAP_USER_SEARCH_BASE}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_SEARCH_BASE)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_SEARCH_BASE)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_USER_SEARCH_BASE]}
                                />
                                <TextInput
                                    id={KEY_LDAP_USER_SEARCH_FILTER}
                                    label="User Search Filter"
                                    name={KEY_LDAP_USER_SEARCH_FILTER}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_SEARCH_FILTER)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_SEARCH_FILTER)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_USER_SEARCH_FILTER]}
                                />
                                <TextInput
                                    id={KEY_LDAP_USER_DN_PATTERNS}
                                    label="User DN Patterns"
                                    name={KEY_LDAP_USER_DN_PATTERNS}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_DN_PATTERNS)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_DN_PATTERNS)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_USER_DN_PATTERNS]}
                                />
                                <TextInput
                                    id={KEY_LDAP_USER_ATTRIBUTES}
                                    label="User Attributes"
                                    name={KEY_LDAP_USER_ATTRIBUTES}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_USER_ATTRIBUTES)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_USER_ATTRIBUTES)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_USER_ATTRIBUTES]}
                                />
                                <TextInput
                                    id={KEY_LDAP_GROUP_SEARCH_BASE}
                                    label="Group Search Base"
                                    name={KEY_LDAP_GROUP_SEARCH_BASE}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_GROUP_SEARCH_BASE)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_GROUP_SEARCH_BASE)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_GROUP_SEARCH_BASE]}
                                />
                                <TextInput
                                    id={KEY_LDAP_GROUP_SEARCH_FILTER}
                                    label="Group Search Filter"
                                    name={KEY_LDAP_GROUP_SEARCH_FILTER}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_GROUP_SEARCH_FILTER)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_GROUP_SEARCH_FILTER)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_GROUP_SEARCH_FILTER]}
                                />
                                <TextInput
                                    id={KEY_LDAP_GROUP_ROLE_ATTRIBUTE}
                                    label="Group Role Attribute"
                                    name={KEY_LDAP_GROUP_ROLE_ATTRIBUTE}
                                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_LDAP_GROUP_ROLE_ATTRIBUTE)}
                                    onChange={this.handleChange}
                                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_LDAP_GROUP_ROLE_ATTRIBUTE)}
                                    errorValue={this.props.fieldErrors[KEY_LDAP_GROUP_ROLE_ATTRIBUTE]}
                                />
                            </CollapsiblePane>
                        </div>
                    </div>
                    <ConfigButtons isFixed={false} includeSave type="submit" performingAction={saving} />
                </form>
            </div>
        );
    }
}

SettingsConfigurationForm.propTypes = {
    fetchingSetupStatus: PropTypes.string.isRequired,
    getSettings: PropTypes.func.isRequired,
    saveSettings: PropTypes.func.isRequired,
    updateStatus: PropTypes.string,
    settingsData: PropTypes.object,
    fieldErrors: PropTypes.object,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string
};

SettingsConfigurationForm.defaultProps = {
    fieldErrors: {},
    settingsData: {},
    updateStatus: '',
    errorMessage: null,
    actionMessage: null
};

export default SettingsConfigurationForm;

