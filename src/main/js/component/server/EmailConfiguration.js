import React from 'react';
import {connect} from "react-redux";
import PropTypes from 'prop-types';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import PasswordInput from '../../field/input/PasswordInput';
import TextInput from '../../field/input/TextInput';
import ConfigButtons from '../common/ConfigButtons';

import { getEmailConfig, updateEmailConfig, toggleAdvancedEmailOptions } from '../../store/actions/emailConfig';

class EmailConfiguration extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            errors: []
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

	componentDidMount() {
		this.props.getEmailConfig();
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
        this.props.updateEmailConfig(this.state);
    }

	render() {

        const showAdvanced = this.props.showAdvanced;
        const showAdvancedLabel = (showAdvanced) ? 'Hide Advanced' : 'Show Advanced';

        return (
        	<div>
                <h1>Server Configuration / Email</h1>
				<form className="form-horizontal" onSubmit={this.handleSubmit}>
					<TextInput
						label="SMTP Host"
						name="mailSmtpHost"
						value={this.props.mailSmtpHost}
						onChange={this.handleChange} />

					<TextInput
						label="SMTP From"
						name="mailSmtpFrom"
						value={this.props.mailSmtpFrom}
						onChange={this.handleChange} />

					<CheckboxInput
						label="SMTP Auth"
						name="mailSmtpAuth"
						value={this.props.mailSmtpAuth}
						onChange={this.handleChange} />

					<TextInput
						label="SMTP User"
						name="mailSmtpUser"
						value={this.props.mailSmtpUser}
						onChange={this.handleChange} />

					<PasswordInput
						label="SMTP Password"
						name="mailSmtpPassword"
						value={this.props.mailSmtpPassword}
						isSet={this.props.mailSmtpPasswordIsSet}
						onChange={this.handleChange} />

					<div className="form-group">
						<div className="col-sm-8 col-sm-offset-3">
							<button type="button" className="btn-link" onClick={() => { this.props.toggleAdvancedEmailOptions(!showAdvanced); return false;}}>
								{showAdvancedLabel}
							</button>
						</div>
					</div>

					{showAdvanced &&
					<div>
						<NumberInput
							label="SMTP Port"
							name="mailSmtpPort"
							value={this.props.mailSmtpPort}
							onChange={this.handleChange} />

						<NumberInput
							label="SMTP Connection Timeout"
							name="mailSmtpConnectionTimeout"
							value={this.props.mailSmtpConnectionTimeout}
							onChange={this.handleChange} />

						<NumberInput
							label="SMTP Timeout"
							name="mailSmtpTimeout"
							value={this.props.mailSmtpTimeout}
							onChange={this.handleChange} />

						<TextInput
							label="SMTP Localhost"
							name="mailSmtpLocalhost"
							value={this.props.mailSmtpLocalhost}
							onChange={this.handleChange} />

						<CheckboxInput
							label="SMTP Ehlo"
							name="mailSmtpEhlo"
							value={this.props.mailSmtpEhlo}
							onChange={this.handleChange} />

						<TextInput
							label="SMTP Dns Notify"
							name="mailSmtpDnsNotify"
							value={this.props.mailSmtpDnsNotify}
							onChange={this.handleChange} />

						<TextInput
							label="SMTP Dns Ret"
							name="mailSmtpDnsRet"
							value={this.props.mailSmtpDnsRet}
							onChange={this.handleChange} />

						<CheckboxInput
							label="SMTP Allow 8-bit Mime"
							name="mailSmtpAllow8bitmime"
							value={this.props.mailSmtpAllow8bitmime}
							onChange={this.handleChange} />

						<CheckboxInput
							label="SMTP Send Partial"
							name="mailSmtpSendPartial"
							value={this.props.mailSmtpSendPartial}
							onChange={this.handleChange} />

					</div>
					}
                    <ConfigButtons includeSave={true} includeTest={false} />
				</form>
			</div>
		);
	}
};

EmailConfiguration.propTypes = {
    mailSmtpHost: PropTypes.string,
    mailSmtpFrom: PropTypes.string,
    mailSmtpAuth: PropTypes.bool.isRequired,
    mailSmtpUser: PropTypes.string,
    mailSmtpPassword: PropTypes.string,
    mailSmtpPasswordIsSet: PropTypes.bool.isRequired,
    mailSmtpPort: PropTypes.number,
    mailSmtpConnectionTimeout: PropTypes.number,
    mailSmtpTimeout: PropTypes.number,
    mailSmtpLocalhost: PropTypes.string,
    mailSmtpEhlo: PropTypes.bool.isRequired,
    mailSmtpDnsNotify: PropTypes.string,
    mailSmtpDnsRet: PropTypes.string,
    mailSmtpAllow8bitmime: PropTypes.bool.isRequired,
    mailSmtpSendPartial: PropTypes.bool.isRequired,
	showAdvanced: PropTypes.bool.isRequired,
    toggleAdvancedEmailOptions: PropTypes.func.isRequired,
	getEmailConfig: PropTypes.func.isRequired
};

EmailConfiguration.defaultProps = {
    mailSmtpAuth: false,
    mailSmtpPasswordIsSet: false,
    mailSmtpEhlo: false,
    mailSmtpAllow8bitmime: false,
    mailSmtpSendPartial: false
};

const mapStateToProps = state => ({
    mailSmtpHost: state.emailConfig.mailSmtpHost,
    mailSmtpFrom: state.emailConfig.mailSmtpFrom,
    mailSmtpAuth: state.emailConfig.mailSmtpAuth,
    mailSmtpUser: state.emailConfig.mailSmtpUser,
    mailSmtpPassword: state.emailConfig.mailSmtpPassword,
    mailSmtpPasswordIsSet: state.emailConfig.mailSmtpPasswordIsSet,
    mailSmtpPort: state.emailConfig.mailSmtpPort,
    mailSmtpConnectionTimeout: state.emailConfig.mailSmtpConnectionTimeout,
    mailSmtpTimeout: state.emailConfig.mailSmtpTimeout,
    mailSmtpLocalhost: state.emailConfig.mailSmtpLocalhost,
    mailSmtpEhlo: state.emailConfig.mailSmtpEhlo,
    mailSmtpDnsNotify: state.emailConfig.mailSmtpDnsNotify,
    mailSmtpDnsRet: state.emailConfig.mailSmtpDnsRet,
    mailSmtpAllow8bitmime: state.emailConfig.mailSmtpAllow8bitmime,
    mailSmtpSendPartial: state.emailConfig.mailSmtpSendPartial,
	showAdvanced: state.emailConfig.showAdvanced
});

const mapDispatchToProps = dispatch => ({
    toggleAdvancedEmailOptions: (toggle) => dispatch(toggleAdvancedEmailOptions(toggle)),
    getEmailConfig: () => dispatch(getEmailConfig()),
	updateEmailConfig: (config) => dispatch(updateEmailConfig(config))

});

export default connect(mapStateToProps, mapDispatchToProps)(EmailConfiguration);
