import connect from "react-redux/es/connect/connect";
import React, {Component} from "react";
import SubmitButton from "./field/input/SubmitButton";
import TextInput from "./field/input/TextInput";
import PasswordInput from "./field/input/PasswordInput";
import NumberInput from "./field/input/NumberInput";
import PropTypes from "prop-types";
import {saveSystemSetup} from "./store/actions/system"

class SetupPage extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.state = {
            setupData: {
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
        if (nextProps.updateStatus === 'FETCHED' && this.props.updateStatus != nextProps.updateStatus) {
            const newState = Object.assign({}, this.state.setupData, {
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
            console.log("New State: ", newState);
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
        console.log("Submitting", this.state.setupData);
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
                                    <h2>Encryption Configuration</h2>
                                    <PasswordInput
                                        id="encryptionPassword"
                                        label="Password"
                                        name="globalEncryptionPassword"
                                        value={this.state.setupData.globalEncryptionPassword}
                                        isSet={this.state.setupData.globalEncryptionPasswordSet}
                                        onChange={this.handleChange}
                                        errorName="globalEncryptionPasswordError"
                                        errorValue={this.props.fieldErrors.globalEncryptionPassword}
                                    />
                                    <PasswordInput
                                        id="encryptionSalt"
                                        label="Salt"
                                        name="globalEncryptionSalt"
                                        value={this.state.setupData.globalEncryptionSalt}
                                        isSet={this.state.setupData.globalEncryptionSaltSet}
                                        onChange={this.handleChange}
                                        errorName="globalEncryptionSaltError"
                                        errorValue={this.props.fieldErrors.globalEncryptionSalt}
                                    />
                                </div>
                                <div className="form-group">
                                    <div className="col-sm-12">
                                        <h2>BlackDuck Configuration</h2>
                                        <TextInput
                                            id="blackDuckProviderUrl"
                                            label="Url"
                                            name="blackDuckProviderUrl"
                                            value={this.state.setupData.blackDuckProviderUrl}
                                            onChange={this.handleChange}
                                            errorName="blackDuckProviderUrlError"
                                            errorValue={this.props.fieldErrors.blackDuckProviderUrl}
                                        />
                                        <PasswordInput id="blackDuckConfigurationApiToken" label="API Token" name="blackDuckApiToken" value={this.state.setupData.blackDuckApiToken} isSet={this.state.setupData.blackDuckApiTokenSet}
                                                       onChange={this.handleChange}
                                                       errorMessage={this.props.fieldErrors.apiKey || this.props.fieldErrors.blackDuckApiToken}/>
                                        <NumberInput id="blackDuckConfigurationTimeout" label="Timeout" name="blackDuckConnectionTimeout" value={this.state.setupData.blackDuckConnectionTimeout} onChange={this.handleChange}/>
                                    </div>
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
                                            errorValue={this.props.fieldErrors.proxyHost}
                                        />
                                        <TextInput
                                            id="proxyPort"
                                            label="Port"
                                            name="proxyPort"
                                            value={this.state.setupData.proxyPort}
                                            onChange={this.handleChange}
                                            errorName="proxyPortError"
                                            errorValue={this.props.fieldErrors.proxyPort}
                                        />
                                        <TextInput
                                            id="proxyUserName"
                                            label="Username"
                                            name="proxyUsername"
                                            value={this.state.setupData.proxyUsername}
                                            onChange={this.handleChange}
                                            errorName="proxyUsernameError"
                                            errorValue={this.props.fieldErrors.proxyUsername}
                                        />
                                        <PasswordInput
                                            id="proxyPassword"
                                            label="Password"
                                            name="proxyPassword"
                                            value={this.state.setupData.proxyPassword}
                                            isSet={this.state.setupData.proxyPasswordSet}
                                            onChange={this.handleChange}
                                            errorName="proxyPasswordError"
                                            errorValue={this.props.fieldErrors.proxyPassword}
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
    fetchingSetup: PropTypes.bool.isRequired,
    updateStatus: PropTypes.string,
    currentSetupData: PropTypes.object,
    fieldErrors: PropTypes.object
};

SetupPage.defaultProps = {
    currentSetupData: {},
    fieldErrors: {}
};

const mapStateToProps = state => ({
    fetchingSetup: state.system.fetching,
    updateStatus: state.system.updateStatus,
    currentSetupData: state.system.setupData,
    fieldErrors: state.system.fieldErrors
});

const mapDispatchToProps = dispatch => ({
    saveSystemSetup: (setupData) => dispatch(saveSystemSetup(setupData))
});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
