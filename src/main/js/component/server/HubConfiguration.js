'use strict';
import React from 'react';
import {connect} from "react-redux";
import PropTypes from 'prop-types';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import TextInput from '../../field/input/TextInput';
import ReadOnlyField from '../../field/ReadOnlyField'
import ConfigButtons from '../ConfigButtons';

import {getConfig, testConfig, updateConfig} from "../../store/actions/config";

class HubConfiguration extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
            hubAlwaysTrustCertificate: this.props.hubAlwaysTrustCertificate,
            hubApiKey: this.props.hubApiKey,
            hubApiKeyIsSet: this.props.hubApiKeyIsSet,
            hubProxyHost: this.props.hubProxyHost,
            hubProxyPassword: this.props.hubProxyPassword,
            hubProxyPasswordIsSet: this.props.hubProxyPasswordIsSet,
            hubProxyPort: this.props.hubProxyPort,
            hubProxyUsername: this.props.hubProxyUsername,
            hubTimeout: this.props.hubTimeout,
			hubUrl: this.props.hubUrl
		};
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
    }

	componentDidMount() {
		this.props.getConfig();
	}

    handleChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleTest() {
		const { id } = this.props;
        this.props.testConfig({ id, ...this.state});
	}

    handleSubmit(evt) {
		evt.preventDefault();
        const { id } = this.props;
        this.props.updateConfig({ id, ...this.state});
	}

    render() {
		const { errorMessage, testStatus, updateStatus } = this.props;
		return (
			<div>
				<form className="form-horizontal" onSubmit={this.handleSubmit}>
					<h1>Server Configuration / Hub Configuration</h1>
                    { testStatus && testStatus === 'SUCCESS' && <div className="alert alert-success">
                        <div>Test was successful!</div>
                    </div>}
                    { errorMessage && <div className="alert alert-danger">
                        { errorMessage }
                    </div> }
                    { updateStatus === 'UPDATED' && <div className="alert alert-success">
                        { 'Update successful' }
					</div> }
					<div>
						<ReadOnlyField label="Url" name="hubUrl" readOnly="true" value={this.props.hubUrl} />
						<TextInput label="API Key" name="hubApiKey" value={this.state.hubApiKey} isSet={this.state.hubApiKeyIsSet} onChange={this.handleChange} errorMessage={this.props.fieldErrors.apiKey || this.props.fieldErrors.hubApiKey} />
						<NumberInput label="Timeout" name="hubTimeout" value={this.state.hubTimeout} onChange={this.handleChange} />
						<CheckboxInput label="Trust Https Certificates" name="hubAlwaysTrustCertificate" readOnly="true" value={this.props.hubAlwaysTrustCertificate} />
						<div className="form-group">
							<div className="col-sm-12">
								<h2>Proxy Configuration <small>(Read-Only)</small></h2>
							</div>
						</div>
						<ReadOnlyField label="Host Name" name="hubProxyHost" readOnly="true" value={this.props.hubProxyHost} />
						<ReadOnlyField label="Port" name="hubProxyPort" readOnly="true" value={this.props.hubProxyPort} />
						<ReadOnlyField label="Username" name="hubProxyUsername" readOnly="true" value={this.props.hubProxyUsername} />
						<ReadOnlyField label="Proxy Password" name="hubProxyPassword" readOnly="true" isSet={this.props.hubProxyPasswordIsSet} />
					</div>
					<ConfigButtons isFixed={false} includeSave={true} includeTest={true} type="submit" onTestClick={this.handleTest} />
				</form>
			</div>
		);
	}
};

// Used for compile/validation of properties
HubConfiguration.propTypes = {
    hubAlwaysTrustCertificate: PropTypes.bool.isRequired,
    hubApiKey: PropTypes.string,
    hubApiKeyIsSet: PropTypes.bool.isRequired,
    hubProxyHost: PropTypes.string,
	hubProxyPassword: PropTypes.string,
    hubProxyPasswordIsSet: PropTypes.bool.isRequired,
    hubProxyPort: PropTypes.string,
    hubProxyUsername: PropTypes.string,
    hubTimeout: PropTypes.number.isRequired,
	hubUrl: PropTypes.string.isRequired,
	id: PropTypes.string,
	errorMessage: PropTypes.string,
	testStatus: PropTypes.string,
	getConfig: PropTypes.func.isRequired,
	updateConfig: PropTypes.func.isRequired,
	testConfig: PropTypes.func.isRequired
};

// Default values
HubConfiguration.defaultProps = {
	hubApiKey: '',
	id: null
};

// Mapping redux state -> react props
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
	testStatus: state.config.testStatus,
    updateStatus: state.config.updateStatus,
	errorMessage: state.config.error.message,
	fieldErrors: state.config.error.fieldErrors,
	id: state.config.id
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: (config) => dispatch(updateConfig(config)),
	testConfig: (config) => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(HubConfiguration);
