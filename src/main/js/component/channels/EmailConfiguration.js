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

    // componentWillReceiveProps(nextProps) {
    //     this.setState({
    //         mailSmtpHost: nextProps.mailSmtpHost || '',
    //         mailSmtpFrom: nextProps.mailSmtpFrom || '',
    //         mailSmtpAuth: nextProps.mailSmtpAuth || false,
    //         mailSmtpUser: nextProps.mailSmtpUser || '',
    //         mailSmtpPassword: nextProps.mailSmtpPassword || '',
    //         mailSmtpPasswordIsSet: nextProps.mailSmtpPasswordIsSet || false,
    //         mailSmtpPort: nextProps.mailSmtpPort || undefined,
    //         mailSmtpConnectionTimeout: nextProps.mailSmtpConnectionTimeout || undefined,
    //         mailSmtpTimeout: nextProps.mailSmtpTimeout || undefined,
    //         mailSmtpLocalhost: nextProps.mailSmtpLocalhost || '',
    //         mailSmtpEhlo: nextProps.mailSmtpEhlo || false,
    //         mailSmtpDnsNotify: nextProps.mailSmtpDnsNotify || '',
    //         mailSmtpDnsRet: nextProps.mailSmtpDnsRet || '',
    //         mailSmtpAllow8bitmime: nextProps.mailSmtpAllow8bitmime || false,
    //         mailSmtpSendPartial: nextProps.mailSmtpSendPartial || false
    //     });
    // }

    handleChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        const { id } = this.props;
        this.props.updateEmailConfig({id,...this.state});
    }

	render() {

        const showAdvanced = this.props.showAdvanced;
        const showAdvancedLabel = (showAdvanced) ? 'Hide Advanced' : 'Show Advanced';
        const {errorMessage, updateStatus } = this.props;
        return (
        	<div>
                <h1>Alert / Channels / Email</h1>
				<form className="form-horizontal" onSubmit={this.handleSubmit}>
                    { errorMessage && <div className="alert alert-danger">
                        { errorMessage }
                    </div> }

                    { updateStatus === 'UPDATED' && <div className="alert alert-success">
                        { 'Update successful' }
                    </div> }
					<TextInput
						label="SMTP Host"
						name="mailSmtpHost"
						value={this.state.mailSmtpHost}
						onChange={this.handleChange}
                        errorName="mailSmtpHostError"
                        errorValue={this.props.fieldErrors.mailSmtpHost} />

					<TextInput
						label="SMTP From"
						name="mailSmtpFrom"
						value={this.state.mailSmtpFrom}
						onChange={this.handleChange}
                        errorName="mailSmtpFromError"
                        errorValue={this.props.fieldErrors.mailSmtpFrom} />

					<CheckboxInput
						label="SMTP Auth"
						name="mailSmtpAuth"
						value={this.state.mailSmtpAuth}
						onChange={this.handleChange}
                        errorName="mailSmtpAuthError"
                        errorValue={this.props.fieldErrors.mailSmtpAuth} />

					<TextInput
						label="SMTP User"
						name="mailSmtpUser"
						value={this.state.mailSmtpUser}
						onChange={this.handleChange}
                        errorName="mailSmtpUserError"
                        errorValue={this.props.fieldErrors.mailSmtpUser} />

					<PasswordInput
						label="SMTP Password"
						name="mailSmtpPassword"
						value={this.state.mailSmtpPassword}
						isSet={this.state.mailSmtpPasswordIsSet}
						onChange={this.handleChange}
                        errorName="mailSmtpPasswordError"
                        errorValue={this.props.fieldErrors.mailSmtpPassword} />

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
							value={this.state.mailSmtpPort}
							onChange={this.handleChange}
                            errorName="mailSmtpPortError"
                            errorValue={this.props.fieldErrors.mailSmtpPort} />

						<NumberInput
							label="SMTP Connection Timeout"
							name="mailSmtpConnectionTimeout"
							value={this.state.mailSmtpConnectionTimeout}
							onChange={this.handleChange}
                            errorName="mailSmtpConnectionTimeoutError"
                            errorValue={this.props.fieldErrors.mailSmtpConnectionTimeout} />

						<NumberInput
							label="SMTP Timeout"
							name="mailSmtpTimeout"
							value={this.state.mailSmtpTimeout}
							onChange={this.handleChange}
                            errorName="mailSmtpTimeoutError"
                            errorValue={this.props.fieldErrors.mailSmtpTimeout} />

						<TextInput
							label="SMTP Localhost"
							name="mailSmtpLocalhost"
							value={this.state.mailSmtpLocalhost}
							onChange={this.handleChange}
                            errorName="mailSmtpLocalhostError"
                            errorValue={this.props.fieldErrors.mailSmtpLocalhost} />

						<CheckboxInput
							label="SMTP Ehlo"
							name="mailSmtpEhlo"
							value={this.state.mailSmtpEhlo}
							onChange={this.handleChange}
                            errorName="mailSmtpEhloError"
                            errorValue={this.props.fieldErrors.mailSmtpEhlo} />

						<TextInput
							label="SMTP Dns Notify"
							name="mailSmtpDnsNotify"
							value={this.state.mailSmtpDnsNotify}
							onChange={this.handleChange}
                            errorName="mailSmtpDnsNotifyError"
                            errorValue={this.props.fieldErrors.mailSmtpDnsNotify} />

						<TextInput
							label="SMTP Dns Ret"
							name="mailSmtpDnsRet"
							value={this.state.mailSmtpDnsRet}
							onChange={this.handleChange}
                            errorName="mailSmtpDnsRetError"
                            errorValue={this.props.fieldErrors.mailSmtpDnsRet} />

						<CheckboxInput
							label="SMTP Allow 8-bit Mime"
							name="mailSmtpAllow8bitmime"
							value={this.state.mailSmtpAllow8bitmime}
							onChange={this.handleChange}
                            errorName="mailSmtpAllow8bitmimeError"
                            errorValue={this.props.fieldErrors.mailSmtpAllow8bitmime} />

						<CheckboxInput
							label="SMTP Send Partial"
							name="mailSmtpSendPartial"
							value={this.state.mailSmtpSendPartial}
							onChange={this.handleChange}
                            errorName="mailSmtpSendPartialError"
                            errorValue={this.props.fieldErrors.mailSmtpSendPartial} />

					</div>
					}
                    <ConfigButtons includeSave={true} includeTest={false} />
				</form>
			</div>
		);
	}
};

EmailConfiguration.propTypes = {
    id: PropTypes.string,
    mailSmtpHost: PropTypes.string,
    mailSmtpFrom: PropTypes.string,
    mailSmtpAuth: PropTypes.bool,
    mailSmtpUser: PropTypes.string,
    mailSmtpPassword: PropTypes.string,
    mailSmtpPasswordIsSet: PropTypes.bool.isRequired,
    mailSmtpPort: PropTypes.number,
    mailSmtpConnectionTimeout: PropTypes.number,
    mailSmtpTimeout: PropTypes.number,
    mailSmtpLocalhost: PropTypes.string,
    mailSmtpEhlo: PropTypes.bool,
    mailSmtpDnsNotify: PropTypes.string,
    mailSmtpDnsRet: PropTypes.string,
    mailSmtpAllow8bitmime: PropTypes.bool,
    mailSmtpSendPartial: PropTypes.bool,
	showAdvanced: PropTypes.bool.isRequired,
    toggleAdvancedEmailOptions: PropTypes.func.isRequired,
	getEmailConfig: PropTypes.func.isRequired,
    errorMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    fieldErrors: PropTypes.arrayOf(PropTypes.any)
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
	showAdvanced: state.emailConfig.showAdvanced,
    id: state.emailConfig.id,
    errorMessage: state.emailConfig.error.message,
    fieldErrors: state.hipChatConfig.error.fieldErrors,
    updateStatus: state.emailConfig.updateStatus,
});

const mapDispatchToProps = dispatch => ({
    toggleAdvancedEmailOptions: (toggle) => dispatch(toggleAdvancedEmailOptions(toggle)),
    getEmailConfig: () => dispatch(getEmailConfig()),
	updateEmailConfig: (config) => dispatch(updateEmailConfig(config))

});

export default connect(mapStateToProps, mapDispatchToProps)(EmailConfiguration);
