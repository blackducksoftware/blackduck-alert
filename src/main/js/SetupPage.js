import connect from "react-redux/es/connect/connect";
import React, {Component} from "react";
import SubmitButton from "./field/input/SubmitButton";
import TextInput from "./field/input/TextInput";
import PasswordInput from "./field/input/PasswordInput";
import PropTypes from "prop-types";
import {saveSystemSetup} from "./store/actions/system"

class SetupPage extends Component {
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
            }
        }
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

    handleChange({target}) {
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
        this.props.saveSystemSetup(this.state.setupData);
    }

    render() {
        return (
            <div className="wrapper">
                <div className="loginContainer">
                    <div className="loginBox">
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
                                <div className="form-group">
                                    <div className="col-sm-12">
                                        <h2>Proxy Configuration</h2>
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
                                    </div>
                                </div>
                            </div>
                            <div className="row">
                                <div className="col-sm-11 text-right">
                                    {this.props.fetchingSetup &&
                                    <div className="progressIcon">
                                        <span className="fa fa-spinner fa-pulse" aria-hidden="true"/>
                                    </div>
                                    }
                                    <SubmitButton id="setupSubmit">Apply</SubmitButton>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        )
    }
}

SetupPage.propTypes = {
    fetchingSetupStatus: PropTypes.string.isRequired,
    updateStatus: PropTypes.string,
    currentSetupData: PropTypes.object,
    fieldErrors: PropTypes.object
};

SetupPage.defaultProps = {
    currentSetupData: {},
    fieldErrors: {},
    fetchingSetupStatus: '',
    updateStatus: ''
};

const mapStateToProps = state => ({
    fetchingSetupStatus: state.system.fetchingSetupStatus,
    updateStatus: state.system.updateStatus,
    currentSetupData: state.system.setupData,
    fieldErrors: state.system.error
});

const mapDispatchToProps = dispatch => ({
    saveSystemSetup: (setupData) => dispatch(saveSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
