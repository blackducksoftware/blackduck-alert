import PasswordInput from "../../../field/input/PasswordInput";
import CheckboxInput from "../../../field/input/CheckboxInput";
import TextInput from "../../../field/input/TextInput";
import PropTypes from "prop-types";
import React, { Component } from "react";
import CollapsiblePane from "../../common/CollapsiblePane";
import ConfigButtons from "../../common/ConfigButtons";


class SettingsConfigurationForm extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        const fieldNames = [
            'user.default.admin.password',
            'encryption.password',
            'encryption.global.salt',
            'proxy.host',
            'proxy.port',
            'proxy.username',
            'proxy.password',
            'ldap.enabled',
            'ldap.server',
            'ldap.manager.dn',
            'ldap.manager.password',
            'ldap.authentication.type',
            'ldap.referral',
            'ldap.user.search.base',
            'ldap.user.search.filter',
            'ldap.user.dn.patterns',
            'ldap.user.attributes',
            'ldap.group.search.base',
            'ldap.group.search.filter',
            'ldap.group.role.attribute',
            'ldap.role.prefix'
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
            console.log("Next props: ", nextProps);
            const newState = Object.assign({}, this.state.settingsData, {});
            this.setState({
                settingsData: newState
            })
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = Object.assign({}, this.state.settingsData, {
            [target.name]: value
        });
        this.setState({
            settingsData: newState
        });
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
                            id="defaultAdminPassword"
                            label="Password"
                            name="defaultAdminPassword"
                            value={this.getFieldModelSingleValue('user.default.admin.password')}
                            isSet={this.getFieldModelValueSet('user.default.admin.password')}
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
                            id="encryptionPassword"
                            label="Password"
                            name="globalEncryptionPassword"
                            value={this.getFieldModelSingleValue('encryption.password')}
                            isSet={this.getFieldModelValueSet('encryption.password')}
                            onChange={this.handleChange}
                            errorName="globalEncryptionPasswordError"
                            errorValue={this.props.fieldErrors.globalEncryptionPasswordError}
                        />
                        <PasswordInput
                            id="encryptionSalt"
                            label="Salt"
                            name="globalEncryptionSalt"
                            value={this.getFieldModelSingleValue('encryption.global.salt')}
                            isSet={this.getFieldModelValueSet('encryption.global.salt')}
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
                                id="proxyHost"
                                label="Host Name"
                                name="proxyHost"
                                value={this.getFieldModelSingleValue('proxy.host')}
                                onChange={this.handleChange}
                                errorName="proxyHostError"
                                errorValue={this.props.fieldErrors.proxyHostError}
                            />
                            <TextInput
                                id="proxyPort"
                                label="Port"
                                name="proxyPort"
                                value={this.getFieldModelSingleValue('proxy.port')}
                                onChange={this.handleChange}
                                errorName="proxyPortError"
                                errorValue={this.props.fieldErrors.proxyPortError}
                            />
                            <TextInput
                                id="proxyUserName"
                                label="Username"
                                name="proxyUsername"
                                value={this.getFieldModelSingleValue('proxy.username')}
                                onChange={this.handleChange}
                                errorName="proxyUsernameError"
                                errorValue={this.props.fieldErrors.proxyUsernameError}
                            />
                            <PasswordInput
                                id="proxyPassword"
                                label="Password"
                                name="proxyPassword"
                                value={this.getFieldModelSingleValue('proxy.password')}
                                isSet={this.getFieldModelValueSet('proxy.password')}
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
                                id="ldapEnabled"
                                label="Enabled"
                                name="ldapEnabled"
                                value={this.getFieldModelSingleValue('ldap.enabled')}
                                onChange={this.handleChange}
                                errorName="ldapEnabledError"
                                errorValue={this.props.fieldErrors.ldapEnabledError}
                            />
                            <TextInput
                                id="ldapServer"
                                label="Server"
                                name="ldapServer"
                                value={this.getFieldModelSingleValue('ldap.server')}
                                onChange={this.handleChange}
                                errorName="ldapServerError"
                                errorValue={this.props.fieldErrors.ldapServerError}
                            />
                            <TextInput
                                id="ldapManagerDn"
                                label="Manager DN"
                                name="ldapManagerDn"
                                value={this.getFieldModelSingleValue('ldap.manager.dn')}
                                onChange={this.handleChange}
                                errorName="ldapManagerDnError"
                                errorValue={this.props.fieldErrors.ldapManagerDnError}
                            />
                            <PasswordInput
                                id="ldapManagerPassword"
                                label="Manager Password"
                                name="ldapManagerPassword"
                                value={this.getFieldModelSingleValue('ldap.manager.password')}
                                isSet={this.getFieldModelValueSet('ldap.manager.password')}
                                onChange={this.handleChange}
                                errorName="ldapManagerPasswordError"
                                errorValue={this.props.fieldErrors.ldapManagerPasswordError}
                            />
                            // TODO: this should be a multi-select field
                            <TextInput
                                id="ldapAuthenticationType"
                                label="Authentication Type"
                                name="ldapAuthenticationType"
                                value={this.getFieldModelSingleValue('ldap.authentication.type')}
                                onChange={this.handleChange}
                                errorName="ldapAuthenticationTypeError"
                                errorValue={this.props.fieldErrors.ldapAuthenticationTypeError}
                            />
                            <TextInput
                                id="ldapReferral"
                                label="Referral"
                                name="ldapReferral"
                                value={this.getFieldModelSingleValue('ldap.referral')}
                                onChange={this.handleChange}
                                errorName="ldapReferralError"
                                errorValue={this.props.fieldErrors.ldapReferralError}
                            />
                            <TextInput
                                id="ldapUserSearchBase"
                                label="User Search Base"
                                name="ldapUserSearchBase"
                                value={this.getFieldModelSingleValue('ldap.user.search.base')}
                                onChange={this.handleChange}
                                errorName="ldapUserSearchBaseError"
                                errorValue={this.props.fieldErrors.ldapUserSearchBaseError}
                            />
                            <TextInput
                                id="ldapUserSearchFilter"
                                label="User Search Filter"
                                name="ldapUserSearchFilter"
                                value={this.getFieldModelSingleValue('ldap.user.search.filter')}
                                onChange={this.handleChange}
                                errorName="ldapUserSearchFilterError"
                                errorValue={this.props.fieldErrors.ldapUserSearchFilterError}
                            />
                            <TextInput
                                id="ldapUserDnPatterns"
                                label="User DN Patterns"
                                name="ldapUserDnPatterns"
                                value={this.getFieldModelSingleValue('ldap.user.dn.patterns')}
                                onChange={this.handleChange}
                                errorName="ldapUserDnPatternsError"
                                errorValue={this.props.fieldErrors.ldapUserDnPatternsError}
                            />
                            <TextInput
                                id="ldapUserAttributes"
                                label="User Attributes"
                                name="ldapUserAttributes"
                                value={this.getFieldModelSingleValue('ldap.user.attributes')}
                                onChange={this.handleChange}
                                errorName="ldapUserAttributesError"
                                errorValue={this.props.fieldErrors.ldapUserAttributesError}
                            />
                            <TextInput
                                id="ldapGroupSearchBase"
                                label="Group Search Base"
                                name="ldapGroupSearchBase"
                                value={this.getFieldModelSingleValue('ldap.group.search.base')}
                                onChange={this.handleChange}
                                errorName="ldapGroupSearchBaseError"
                                errorValue={this.props.fieldErrors.ldapGroupSearchBaseError}
                            />
                            <TextInput
                                id="ldapGroupSearchFilter"
                                label="Group Search Filter"
                                name="ldapGroupSearchFilter"
                                value={this.getFieldModelSingleValue('ldap.group.search.filter')}
                                onChange={this.handleChange}
                                errorName="ldapGroupSearchFilterError"
                                errorValue={this.props.fieldErrors.ldapGroupSearchFilterError}
                            />
                            <TextInput
                                id="ldapGroupRoleAttribute"
                                label="Group Role Attribute"
                                name="ldapGroupRoleAttribute"
                                value={this.getFieldModelSingleValue('ldap.group.role.attribute')}
                                onChange={this.handleChange}
                                errorName="ldapGroupRoleAttributeError"
                                errorValue={this.props.fieldErrors.ldapGroupRoleAttributeError}
                            />
                            <TextInput
                                id="ldapRolePrefix"
                                label="Role Prefix"
                                name="ldapRolePrefix"
                                value={this.getFieldModelSingleValue('ldap.role.prefix')}
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

