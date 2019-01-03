import PasswordInput from "../../../field/input/PasswordInput";
import CheckboxInput from "../../../field/input/CheckboxInput";
import TextInput from "../../../field/input/TextInput";
import SubmitButton from "../../../field/input/SubmitButton";
import PropTypes from "prop-types";
import React, { Component } from "react";
import CollapsiblePane from "../../common/CollapsiblePane";


class SettingsConfigurationForm extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.state = {
            setupData: {
                defaultAdminPassword: '',
                defaultAdminPasswordSet: false,
                globalEncryptionPassword: '',
                globalEncryptionPasswordSet: false,
                globalEncryptionSalt: '',
                globalEncryptionSaltSet: false,
                blackDuckProviderUrl: '',
                blackDuckApiToken: '',
                blackDuckApiTokenSet: false,
                blackDuckConnectionTimeout: 300,
                proxyHost: '',
                proxyPort: '',
                proxyUsername: '',
                proxyPassword: '',
                proxyPasswordSet: false
            },
            proxyPaneOpen: false,
            ldapPaneOpen: false
        }
    }

    componentWillMount() {
        this.props.getSettings();
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.fetchingSetupStatus === 'SYSTEM SETUP FETCHED' && nextProps.updateStatus === 'FETCHED' ||
            this.props.fetchingSetupStatus === 'SYSTEM SETUP FETCHED' && this.props.updateStatus === 'FETCHED') {
            const newState = Object.assign({}, this.state.setupData, {
                defaultAdminPassword: nextProps.currentSetupData.defaultAdminPassword || '',
                defaultAdminPasswordSet: nextProps.currentSetupData.defaultAdminPasswordSet || false,
                globalEncryptionPassword: nextProps.currentSetupData.globalEncryptionPassword || '',
                globalEncryptionPasswordSet: nextProps.currentSetupData.globalEncryptionPasswordSet || false,
                globalEncryptionSalt: nextProps.currentSetupData.globalEncryptionSalt || '',
                globalEncryptionSaltSet: nextProps.currentSetupData.globalEncryptionSaltSet || false,
                blackDuckProviderUrl: nextProps.currentSetupData.blackDuckProviderUrl || '',
                blackDuckApiToken: nextProps.currentSetupData.blackDuckApiToken || '',
                blackDuckApiTokenSet: nextProps.currentSetupData.blackDuckApiTokenSet || false,
                blackDuckConnectionTimeout: nextProps.currentSetupData.blackDuckConnectionTimeout || 300,
                proxyHost: nextProps.currentSetupData.proxyHost || '',
                proxyPort: nextProps.currentSetupData.proxyPort || '',
                proxyUsername: nextProps.currentSetupData.proxyUsername || '',
                proxyPassword: nextProps.currentSetupData.proxyPassword || '',
                proxyPasswordSet: nextProps.currentSetupData.proxyPasswordSet || false
            });
            this.setState({
                setupData: newState
            })
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = Object.assign({}, this.state.setupData, {
            [target.name]: value
        });
        this.setState({
            setupData: newState
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        this.props.saveSettings(this.state.setupData);
    }

    toggleProxyPane() {
        this.setState({ proxyPaneOpen: !this.state.proxyPaneOpen })
    }

    toggleLdapPane() {
        this.setState({ ldapPaneOpen: !this.state.ldapPaneOpen })
    }

    render() {
        return (
            <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <div className="col-sm-12">
                        <h2>Default Admin Configuration</h2>
                        <PasswordInput
                            id="defaultAdminPassword"
                            label="Password"
                            name="defaultAdminPassword"
                            value={this.state.setupData.defaultAdminPassword}
                            isSet={this.state.setupData.defaultAdminPasswordSet}
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
                            value={this.state.setupData.globalEncryptionPassword}
                            isSet={this.state.setupData.globalEncryptionPasswordSet}
                            onChange={this.handleChange}
                            errorName="globalEncryptionPasswordError"
                            errorValue={this.props.fieldErrors.globalEncryptionPasswordError}
                        />
                        <PasswordInput
                            id="encryptionSalt"
                            label="Salt"
                            name="globalEncryptionSalt"
                            value={this.state.setupData.globalEncryptionSalt}
                            isSet={this.state.setupData.globalEncryptionSaltSet}
                            onChange={this.handleChange}
                            errorName="globalEncryptionSaltError"
                            errorValue={this.props.fieldErrors.globalEncryptionSaltError}
                        />
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-sm-12">
                        <CollapsiblePane titleComponent={<h2>Proxy Configuration</h2>}>
                            <TextInput
                                id="proxyHost"
                                label="Host Name"
                                name="proxyHost"
                                value={this.state.setupData.proxyHost}
                                onChange={this.handleChange}
                                errorName="proxyHostError"
                                errorValue={this.props.fieldErrors.proxyHostError}
                            />
                            <TextInput
                                id="proxyPort"
                                label="Port"
                                name="proxyPort"
                                value={this.state.setupData.proxyPort}
                                onChange={this.handleChange}
                                errorName="proxyPortError"
                                errorValue={this.props.fieldErrors.proxyPortError}
                            />
                            <TextInput
                                id="proxyUserName"
                                label="Username"
                                name="proxyUsername"
                                value={this.state.setupData.proxyUsername}
                                onChange={this.handleChange}
                                errorName="proxyUsernameError"
                                errorValue={this.props.fieldErrors.proxyUsernameError}
                            />
                            <PasswordInput
                                id="proxyPassword"
                                label="Password"
                                name="proxyPassword"
                                value={this.state.setupData.proxyPassword}
                                isSet={this.state.setupData.proxyPasswordSet}
                                onChange={this.handleChange}
                                errorName="proxyPasswordError"
                                errorValue={this.props.fieldErrors.proxyPasswordError}
                            />
                        </CollapsiblePane>
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-sm-12">
                        <CollapsiblePane titleComponent={<h2>LDAP Configuration</h2>}>
                            <CheckboxInput
                                id="ldapEnabled"
                                label="Enabled"
                                name="ldapEnabled"
                                value={this.state.setupData.ldapEnabled}
                                onChange={this.handleChange}
                                errorName="ldapEnabledError"
                                errorValue={this.props.fieldErrors.ldapEnabledError}
                            />
                            <TextInput
                                id="ldapServer"
                                label="Server"
                                name="ldapServer"
                                value={this.state.setupData.ldapServer}
                                onChange={this.handleChange}
                                errorName="ldapServerError"
                                errorValue={this.props.fieldErrors.ldapServerError}
                            />
                            <TextInput
                                id="ldapManagerDn"
                                label="Manager DN"
                                name="ldapManagerDn"
                                value={this.state.setupData.ldapManagerDn}
                                onChange={this.handleChange}
                                errorName="ldapManagerDnError"
                                errorValue={this.props.fieldErrors.ldapManagerDnError}
                            />
                            <PasswordInput
                                id="ldapManagerPassword"
                                label="Manager Password"
                                name="ldapManagerPassword"
                                value={this.state.setupData.ldapManagerPassword}
                                isSet={this.state.setupData.ldapManagerPasswordSet}
                                onChange={this.handleChange}
                                errorName="ldapManagerPasswordError"
                                errorValue={this.props.fieldErrors.ldapManagerPasswordError}
                            />
                            <TextInput
                                id="ldapAuthenticationType"
                                label="Authentication Type"
                                name="ldapAuthenticationType"
                                value={this.state.setupData.ldapAuthenticationType}
                                onChange={this.handleChange}
                                errorName="ldapAuthenticationTypeError"
                                errorValue={this.props.fieldErrors.ldapAuthenticationTypeError}
                            />
                            <TextInput
                                id="ldapReferral"
                                label="Referral"
                                name="ldapReferral"
                                value={this.state.setupData.ldapReferral}
                                onChange={this.handleChange}
                                errorName="ldapReferralError"
                                errorValue={this.props.fieldErrors.ldapReferralError}
                            />
                            <TextInput
                                id="ldapUserSearchBase"
                                label="User Search Base"
                                name="ldapUserSearchBase"
                                value={this.state.setupData.ldapUserSearchBase}
                                onChange={this.handleChange}
                                errorName="ldapUserSearchBaseError"
                                errorValue={this.props.fieldErrors.ldapUserSearchBaseError}
                            />
                            <TextInput
                                id="ldapUserSearchFilter"
                                label="User Search Filter"
                                name="ldapUserSearchFilter"
                                value={this.state.setupData.ldapUserSearchFilter}
                                onChange={this.handleChange}
                                errorName="ldapUserSearchFilterError"
                                errorValue={this.props.fieldErrors.ldapUserSearchFilterError}
                            />
                            <TextInput
                                id="ldapUserDnPatterns"
                                label="User DN Patterns"
                                name="ldapUserDnPatterns"
                                value={this.state.setupData.ldapUserDnPatterns}
                                onChange={this.handleChange}
                                errorName="ldapUserDnPatternsError"
                                errorValue={this.props.fieldErrors.ldapUserDnPatternsError}
                            />
                            <TextInput
                                id="ldapUserAttributes"
                                label="User Attributes"
                                name="ldapUserAttributes"
                                value={this.state.setupData.ldapUserAttributes}
                                onChange={this.handleChange}
                                errorName="ldapUserAttributesError"
                                errorValue={this.props.fieldErrors.ldapUserAttributesError}
                            />
                            <TextInput
                                id="ldapGroupSearchBase"
                                label="Group Search Base"
                                name="ldapGroupSearchBase"
                                value={this.state.setupData.ldapGroupSearchBase}
                                onChange={this.handleChange}
                                errorName="ldapGroupSearchBaseError"
                                errorValue={this.props.fieldErrors.ldapGroupSearchBaseError}
                            />
                            <TextInput
                                id="ldapGroupSearchFilter"
                                label="Group Search Filter"
                                name="ldapGroupSearchFilter"
                                value={this.state.setupData.ldapGroupSearchFilter}
                                onChange={this.handleChange}
                                errorName="ldapGroupSearchFilterError"
                                errorValue={this.props.fieldErrors.ldapGroupSearchFilterError}
                            />
                            <TextInput
                                id="ldapGroupRoleAttribute"
                                label="Group Role Attribute"
                                name="ldapGroupRoleAttribute"
                                value={this.state.setupData.ldapGroupRoleAttribute}
                                onChange={this.handleChange}
                                errorName="ldapGroupRoleAttributeError"
                                errorValue={this.props.fieldErrors.ldapGroupRoleAttributeError}
                            />
                            <TextInput
                                id="ldapRolePrefix"
                                label="Role Prefix"
                                name="ldapRolePrefix"
                                value={this.state.setupData.ldapRolePrefix}
                                onChange={this.handleChange}
                                errorName="ldapRolePrefixError"
                                errorValue={this.props.fieldErrors.ldapRolePrefixError}
                            />
                        </CollapsiblePane>
                    </div>
                </div>
                <div className="row">
                    <div className="col-sm-11 text-right">
                        {this.props.fetchingSetup &&
                        <div className="progressIcon">
                            <span className="fa fa-spinner fa-pulse" aria-hidden="true" />
                        </div>
                        }
                        <SubmitButton id="setupSubmit">Save</SubmitButton>
                    </div>
                </div>
            </form>
        )
    }
}

SettingsConfigurationForm.propTypes = {
    fetchingSetupStatus: PropTypes.string.isRequired,
    getSettings: PropTypes.func.isRequired,
    saveSettings: PropTypes.func.isRequired,
    updateStatus: PropTypes.string,
    currentSetupData: PropTypes.object,
    fieldErrors: PropTypes.object
};

SettingsConfigurationForm.defaultProps = {
    currentSetupData: {},
    fieldErrors: {},
    fetchingSetupStatus: '',
    updateStatus: ''
};

export default SettingsConfigurationForm;

