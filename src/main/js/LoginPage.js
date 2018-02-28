'use strict';

import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from "react-redux";
import CheckboxInput from './field/input/CheckboxInput';
import NumberInput from './field/input/NumberInput';
import PasswordInput from './field/input/PasswordInput';
import TextInput from './field/input/TextInput';
import SubmitButton from './field/input/SubmitButton';
import ReadOnlyField from './field/ReadOnlyField';
import Header from './component/common/Header';

import {loggedIn, loggingIn, login, toggleAdvancedOptions} from './store/actions/session';

class LoginPage extends Component {

	constructor(props) {
		super(props);

		this.state = {
			errors: []
		};

		this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
	}

    handleChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleSubmit(evt) {
    	evt.preventDefault();

    	const { hubUrl } = this.props;
    	const { hubUsername, hubPassword } = this.state;

    	this.props.login(hubUrl, hubUsername, hubPassword);
	}

	render() {
		var progressIndicator = null;
		if (this.props.loggingIn) {
        	const fontAwesomeIcon = "fa fa-spinner fa-pulse fa-fw";
			progressIndicator = <div className="progressIcon">
									<i className={fontAwesomeIcon} aria-hidden='true'></i>
								</div>;
		}

		const showAdvanced = this.props.showAdvanced;
		const showAdvancedLabel = (showAdvanced) ? 'Hide Advanced' : 'Show Advanced';

		return (
				<div className="wrapper">
					<div className="loginContainer">
						<div className="loginBox">
                            <Header />
							<form method='POST' className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
								{ this.props.errorMessage && <div className="alert alert-danger">
									<p name="configurationMessage">{this.props.errorMessage}</p>
								</div> }

								<ReadOnlyField label="Hub Url" name="hubUrl" readOnly="true" value={this.props.hubUrl} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}/>
								<TextInput label="Username" name="hubUsername" onChange={this.handleChange} errorName="usernameError" autoFocus={true} />
								<PasswordInput label="Password" name="hubPassword" onChange={this.handleChange} errorName="passwordError" />

								<div className="form-group">
									<div className="col-sm-8 col-sm-offset-3">
										<button type="button" className="btn-link" onClick={() => { this.props.toggleAdvancedOptions(!showAdvanced); }}>
											{showAdvancedLabel }
										</button>
									</div>
								</div>

								{ showAdvanced &&
								<div>
									<NumberInput label="Timeout" name="hubTimeout" value={this.props.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></NumberInput>
									<CheckboxInput label="Trust TLS Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.props.hubAlwaysTrustCertificate} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></CheckboxInput>
									<ReadOnlyField label="Proxy Host Name" name="hubProxyHost" readOnly="true" value={this.props.hubProxyHost} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}/>
									<ReadOnlyField label="Proxy Port" name="hubProxyPort" readOnly="true" value={this.props.hubProxyPort} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}/>
									<ReadOnlyField label="Proxy Username" name="hubProxyUsername" readOnly="true" value={this.props.hubProxyUsername} errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}/>
									<ReadOnlyField label="Proxy Password" name="hubProxyPassword" readOnly="true"  isSet={this.props.hubProxyPasswordIsSet} errorName="hubProxyPasswordError" errorValue={this.state.errors.hubProxyPassword}/>
								</div> }

								<div className="row">
									<div className="col-sm-12 text-right">
										<SubmitButton>Login</SubmitButton>
									</div>
								</div>
                                <div className="row">
									<div className="col-sm-12">
										{progressIndicator}
									</div>
								</div>
							</form>
						</div>
					</div>
				</div>
		)
	}
}

LoginPage.propTypes = {
	loggingIn: PropTypes.bool.isRequired,
    hubTimeout: PropTypes.number.isRequired,
	hubUrl: PropTypes.string,
    baseUrl: PropTypes.string,
    showAdvanced: PropTypes.bool.isRequired,
	errorMessage: PropTypes.string,
	toggleAdvancedOptions: PropTypes.func.isRequired,
};

LoginPage.defaultProps = {
    hubTimeout: 60,
    errorMessage: '',
    hubProxyHost: 'hubProxyHost'
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    hubAlwaysTrustCertificate: state.config.hubAlwaysTrustCertificate,
    hubApiKey: state.config.hubApiKey,
    hubApiKeyIsSet: state.config.hubApiKeyIsSet,
    hubProxyHost: state.config.hubProxyHost,
    hubProxyPassword: state.config.hubProxyPassword,
    hubProxyPasswordIsSet: state.config.hubProxyPasswordIsSet,
    hubProxyPort: state.config.hubProxyPort,
    hubProxyUsername: state.config.hubProxyUsername,
    hubTimeout: state.config.hubTimeout,
    hubUrl: state.config.hubUrl,
	loggingIn: state.session.fetching,
	showAdvanced: state.session.showAdvanced,
	errorMessage: state.session.errorMessage
});

const mapDispatchToProps = dispatch => ({
    toggleAdvancedOptions: (toggle) => dispatch(toggleAdvancedOptions(toggle)),
	login: (url, username, password) => dispatch(login(url, username, password))
});

export default connect(mapStateToProps, mapDispatchToProps)(LoginPage);
