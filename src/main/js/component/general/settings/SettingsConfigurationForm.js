import PasswordInput from "../../../field/input/PasswordInput";
import CheckboxInput from "../../../field/input/CheckboxInput";
import TextInput from "../../../field/input/TextInput";
import PropTypes from "prop-types";
import React, { Component } from "react";
import CollapsiblePane from "../../common/CollapsiblePane";
import ConfigButtons from "../../common/ConfigButtons";


const KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD = 'user.default.admin.password';
const KEY_ENCRYPTION_PASSWORD = 'encryption.password';
const KEY_ENCRYPTION_GLOBAL_SALT = 'encryption.global.salt';

// Proxy Keys
const KEY_PROXY_HOST = 'proxy.host';
const KEY_PROXY_PORT = 'proxy.port';
const KEY_PROXY_USERNAME = 'proxy.username';
const KEY_PROXY_PASSWORD = 'proxy.password';

// LDAP Keys
const KEY_LDAP_ENABLED = 'ldap.enabled';
const KEY_LDAP_SERVER = 'ldap.server';
const KEY_LDAP_MANAGER_DN = 'ldap.manager.dn';
const KEY_LDAP_MANAGER_PASSWORD = 'ldap.manager.password';
const KEY_LDAP_AUTHENTICATION_TYPE = 'ldap.authentication.type';
const KEY_LDAP_REFERRAL = 'ldap.referral';
const KEY_LDAP_USER_SEARCH_BASE = 'ldap.user.search.base';
const KEY_LDAP_USER_SEARCH_FILTER = 'ldap.user.search.filter';
const KEY_LDAP_USER_DN_PATTERNS = 'ldap.user.dn.patterns';
const KEY_LDAP_USER_ATTRIBUTES = 'ldap.user.attributes';
const KEY_LDAP_GROUP_SEARCH_BASE = 'ldap.group.search.base';
const KEY_LDAP_GROUP_SEARCH_FILTER = 'ldap.group.search.filter';
const KEY_LDAP_GROUP_ROLE_ATTRIBUTE = 'ldap.group.role.attribute';
const KEY_LDAP_ROLE_PREFIX = 'ldap.role.prefix';

class SettingsConfigurationForm extends Component {

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        const fieldNames = [
            KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD,
            KEY_ENCRYPTION_PASSWORD,
            KEY_ENCRYPTION_GLOBAL_SALT,
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
        this.state = {
            settingsData: this.createEmptyFieldModel(fieldNames)
        }
    }

    createEmptyFieldModel(fields) {
        const emptySettings = {};
        emptySettings['context'] = 'GLOBAL';
        emptySettings['descriptorName'] = 'component_settings';
        emptySettings['keyToValues'] = {};
        for (let index in fields) {
            emptySettings.keyToValues[fields[index]] = {
                values: [''],
                isSet: false
            };
        }
        return emptySettings;
    }

    getFieldModelSingleValue(key) {
        return this.state.settingsData.keyToValues[key].values[0];
    }

    getFieldModelValueSet(key) {
        return this.state.settingsData.keyToValues[key].isSet;
    }


    componentWillMount() {
        this.props.getSettings();
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.fetchingSetupStatus === 'SYSTEM SETUP FETCHED' && nextProps.updateStatus === 'FETCHED' ||
            this.props.fetchingSetupStatus === 'SYSTEM SETUP FETCHED' && this.props.updateStatus === 'FETCHED') {
            const newState = Object.assign({}, this.state.settingsData, nextProps.settingsData);
            this.setState({
                settingsData: newState
            })
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const stateCopy = Object.assign({}, this.state.settingsData);
        stateCopy.keyToValues[target.name].values[0] = value;
        const newState = Object.assign({}, stateCopy);
        console.log("new state", newState)
        this.setState({
            settingsData: newState
        });
        console.log("Handle Change New State", this.state);
    }

    handleSubmit(evt) {
        evt.preventDefault();
        this.props.saveSettings(this.state.settingsData);
    }

    render() {
        return (
            <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <div className="col-sm-12">
                        <h2>Default System Administrator Configuration</h2>
                        <PasswordInput
                            id={KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD}
                            label="Password"
                            name={KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD}
                            value={this.getFieldModelSingleValue(KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD)}
                            isSet={this.getFieldModelValueSet(KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD)}
                            onChange={this.handleChange}
                            errorName="defaultAdminPasswordError"
                            errorValue={this.props.fieldErrors.defaultAdminPassword}
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
                            value={this.getFieldModelSingleValue(KEY_ENCRYPTION_PASSWORD)}
                            isSet={this.getFieldModelValueSet(KEY_ENCRYPTION_PASSWORD)}
                            onChange={this.handleChange}
                            errorName="globalEncryptionPasswordError"
                            errorValue={this.props.fieldErrors.globalEncryptionPasswordError}
                        />
                        <PasswordInput
                            id={KEY_ENCRYPTION_GLOBAL_SALT}
                            label="Salt"
                            name={KEY_ENCRYPTION_GLOBAL_SALT}
                            value={this.getFieldModelSingleValue(KEY_ENCRYPTION_GLOBAL_SALT)}
                            isSet={this.getFieldModelValueSet(KEY_ENCRYPTION_GLOBAL_SALT)}
                            onChange={this.handleChange}
                            errorName="globalEncryptionSaltError"
                            errorValue={this.props.fieldErrors.globalEncryptionSaltError}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-sm-12">
                        <CollapsiblePane title="Proxy Configuration">
                            <TextInput
                                id={KEY_PROXY_HOST}
                                label="Host Name"
                                name={KEY_PROXY_HOST}
                                value={this.getFieldModelSingleValue(KEY_PROXY_HOST)}
                                onChange={this.handleChange}
                                errorName="proxyHostError"
                                errorValue={this.props.fieldErrors.proxyHostError}
                            />
                            <TextInput
                                id={KEY_PROXY_PORT}
                                label="Port"
                                name={KEY_PROXY_PORT}
                                value={this.getFieldModelSingleValue(KEY_PROXY_PORT)}
                                onChange={this.handleChange}
                                errorName="proxyPortError"
                                errorValue={this.props.fieldErrors.proxyPortError}
                            />
                            <TextInput
                                id={KEY_PROXY_USERNAME}
                                label="Username"
                                name={KEY_PROXY_USERNAME}
                                value={this.getFieldModelSingleValue(KEY_PROXY_USERNAME)}
                                onChange={this.handleChange}
                                errorName="proxyUsernameError"
                                errorValue={this.props.fieldErrors.proxyUsernameError}
                            />
                            <PasswordInput
                                id={KEY_PROXY_PASSWORD}
                                label="Password"
                                name={KEY_PROXY_PASSWORD}
                                value={this.getFieldModelSingleValue(KEY_PROXY_PASSWORD)}
                                isSet={this.getFieldModelValueSet(KEY_PROXY_PASSWORD)}
                                onChange={this.handleChange}
                                errorName="proxyPasswordError"
                                errorValue={this.props.fieldErrors.proxyPasswordError}
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
                                name={KEY_LDAP_ENABLED}
                                value={this.getFieldModelSingleValue(KEY_LDAP_ENABLED)}
                                onChange={this.handleChange}
                                errorName="ldapEnabledError"
                                errorValue={this.props.fieldErrors.ldapEnabledError}
                            />
                            <TextInput
                                id={KEY_LDAP_SERVER}
                                label="Server"
                                name={KEY_LDAP_SERVER}
                                value={this.getFieldModelSingleValue(KEY_LDAP_SERVER)}
                                onChange={this.handleChange}
                                errorName="ldapServerError"
                                errorValue={this.props.fieldErrors.ldapServerError}
                            />
                            <TextInput
                                id={KEY_LDAP_MANAGER_DN}
                                label="Manager DN"
                                name={KEY_LDAP_MANAGER_DN}
                                value={this.getFieldModelSingleValue(KEY_LDAP_MANAGER_DN)}
                                onChange={this.handleChange}
                                errorName="ldapManagerDnError"
                                errorValue={this.props.fieldErrors.ldapManagerDnError}
                            />
                            <PasswordInput
                                id={KEY_LDAP_MANAGER_PASSWORD}
                                label="Manager Password"
                                name={KEY_LDAP_MANAGER_PASSWORD}
                                value={this.getFieldModelSingleValue(KEY_LDAP_MANAGER_PASSWORD)}
                                isSet={this.getFieldModelValueSet(KEY_LDAP_MANAGER_PASSWORD)}
                                onChange={this.handleChange}
                                errorName="ldapManagerPasswordError"
                                errorValue={this.props.fieldErrors.ldapManagerPasswordError}
                            />
                            // TODO: this should be a multi-select field
                            <TextInput
                                id={KEY_LDAP_AUTHENTICATION_TYPE}
                                label="Authentication Type"
                                name={KEY_LDAP_AUTHENTICATION_TYPE}
                                value={this.getFieldModelSingleValue(KEY_LDAP_AUTHENTICATION_TYPE)}
                                onChange={this.handleChange}
                                errorName="ldapAuthenticationTypeError"
                                errorValue={this.props.fieldErrors.ldapAuthenticationTypeError}
                            />
                            <TextInput
                                id={KEY_LDAP_REFERRAL}
                                label="Referral"
                                name={KEY_LDAP_REFERRAL}
                                value={this.getFieldModelSingleValue(KEY_LDAP_REFERRAL)}
                                onChange={this.handleChange}
                                errorName="ldapReferralError"
                                errorValue={this.props.fieldErrors.ldapReferralError}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_SEARCH_BASE}
                                label="User Search Base"
                                name={KEY_LDAP_USER_SEARCH_BASE}
                                value={this.getFieldModelSingleValue(KEY_LDAP_USER_SEARCH_BASE)}
                                onChange={this.handleChange}
                                errorName="ldapUserSearchBaseError"
                                errorValue={this.props.fieldErrors.ldapUserSearchBaseError}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_SEARCH_FILTER}
                                label="User Search Filter"
                                name={KEY_LDAP_USER_SEARCH_FILTER}
                                value={this.getFieldModelSingleValue(KEY_LDAP_USER_SEARCH_FILTER)}
                                onChange={this.handleChange}
                                errorName="ldapUserSearchFilterError"
                                errorValue={this.props.fieldErrors.ldapUserSearchFilterError}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_DN_PATTERNS}
                                label="User DN Patterns"
                                name={KEY_LDAP_USER_DN_PATTERNS}
                                value={this.getFieldModelSingleValue(KEY_LDAP_USER_DN_PATTERNS)}
                                onChange={this.handleChange}
                                errorName="ldapUserDnPatternsError"
                                errorValue={this.props.fieldErrors.ldapUserDnPatternsError}
                            />
                            <TextInput
                                id={KEY_LDAP_USER_ATTRIBUTES}
                                label="User Attributes"
                                name={KEY_LDAP_USER_ATTRIBUTES}
                                value={this.getFieldModelSingleValue(KEY_LDAP_USER_ATTRIBUTES)}
                                onChange={this.handleChange}
                                errorName="ldapUserAttributesError"
                                errorValue={this.props.fieldErrors.ldapUserAttributesError}
                            />
                            <TextInput
                                id={KEY_LDAP_GROUP_SEARCH_BASE}
                                label="Group Search Base"
                                name={KEY_LDAP_GROUP_SEARCH_BASE}
                                value={this.getFieldModelSingleValue(KEY_LDAP_GROUP_SEARCH_BASE)}
                                onChange={this.handleChange}
                                errorName="ldapGroupSearchBaseError"
                                errorValue={this.props.fieldErrors.ldapGroupSearchBaseError}
                            />
                            <TextInput
                                id={KEY_LDAP_GROUP_SEARCH_FILTER}
                                label="Group Search Filter"
                                name={KEY_LDAP_GROUP_SEARCH_FILTER}
                                value={this.getFieldModelSingleValue(KEY_LDAP_GROUP_SEARCH_FILTER)}
                                onChange={this.handleChange}
                                errorName="ldapGroupSearchFilterError"
                                errorValue={this.props.fieldErrors.ldapGroupSearchFilterError}
                            />
                            <TextInput
                                id={KEY_LDAP_GROUP_ROLE_ATTRIBUTE}
                                label="Group Role Attribute"
                                name={KEY_LDAP_GROUP_ROLE_ATTRIBUTE}
                                value={this.getFieldModelSingleValue(KEY_LDAP_GROUP_ROLE_ATTRIBUTE)}
                                onChange={this.handleChange}
                                errorName="ldapGroupRoleAttributeError"
                                errorValue={this.props.fieldErrors.ldapGroupRoleAttributeError}
                            />
                            <TextInput
                                id={KEY_LDAP_ROLE_PREFIX}
                                label="Role Prefix"
                                name={KEY_LDAP_ROLE_PREFIX}
                                value={this.getFieldModelSingleValue(KEY_LDAP_ROLE_PREFIX)}
                                onChange={this.handleChange}
                                errorName="ldapRolePrefixError"
                                errorValue={this.props.fieldErrors.ldapRolePrefixError}
                            />
                        </CollapsiblePane>
                    </div>
                </div>
                <ConfigButtons isFixed={false} includeSave type="submit" />
            </form>
        )
    }
}

SettingsConfigurationForm.propTypes = {
    fetchingSetupStatus: PropTypes.string.isRequired,
    getSettings: PropTypes.func.isRequired,
    saveSettings: PropTypes.func.isRequired,
    updateStatus: PropTypes.string,
    currentSettingsData: PropTypes.object,
    fieldErrors: PropTypes.object
};

SettingsConfigurationForm.defaultProps = {
    currentSettingsData: {},
    fieldErrors: {},
    fetchingSetupStatus: '',
    updateStatus: ''
};

export default SettingsConfigurationForm;

