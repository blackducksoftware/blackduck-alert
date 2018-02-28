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
    	const { hubUsername, hubPassword } = this.state;

    	this.props.login(hubUsername, hubPassword);
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

								<TextInput label="Username" name="hubUsername" onChange={this.handleChange} errorName="usernameError" autoFocus={true} />
								<PasswordInput label="Password" name="hubPassword" onChange={this.handleChange} errorName="passwordError" />

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
    baseUrl: PropTypes.string,
	errorMessage: PropTypes.string,
};

LoginPage.defaultProps = {
    errorMessage: ''
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
	loggingIn: state.session.fetching,
	errorMessage: state.session.errorMessage
});

const mapDispatchToProps = dispatch => ({
	login: (username, password) => dispatch(login(username, password))
});

export default connect(mapStateToProps, mapDispatchToProps)(LoginPage);
