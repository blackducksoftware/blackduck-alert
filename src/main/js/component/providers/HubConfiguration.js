import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import TextInput from '../../field/input/TextInput';
import ReadOnlyField from '../../field/ReadOnlyField';
import ConfigButtons from '../common/ConfigButtons';

import { getConfig, testConfig, updateConfig } from '../../store/actions/config';

class HubConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
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

    componentWillReceiveProps(nextProps) {
        if (nextProps.updateStatus === 'FETCHED' || nextProps.updateStatus === 'UPDATED') {
            this.setState({
                hubApiKey: nextProps.hubApiKey,
                hubApiKeyIsSet: nextProps.hubApiKeyIsSet,
                hubProxyHost: nextProps.hubProxyHost,
                hubProxyPassword: nextProps.hubProxyPassword,
                hubProxyPasswordIsSet: nextProps.hubProxyPasswordIsSet,
                hubProxyPort: nextProps.hubProxyPort,
                hubProxyUsername: nextProps.hubProxyUsername,
                hubTimeout: nextProps.hubTimeout,
                hubUrl: nextProps.hubUrl
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleTest() {
        const { id } = this.props;
        this.props.testConfig({ id, ...this.state });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        const { id } = this.props;
        this.props.updateConfig({ id, ...this.state });
    }

    render() {
        const { errorMessage, testStatus, updateStatus } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-laptop" />
                    Hub
                </h1>

                { testStatus && testStatus === 'SUCCESS' && <div className="alert alert-success">
                    <div>Test was successful!</div>
                </div>}

                { errorMessage && <div className="alert alert-danger">
                    { errorMessage }
                </div> }

                { updateStatus === 'UPDATED' && <div className="alert alert-success">
                    { 'Update successful' }
                </div> }

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div>
                        <ReadOnlyField label="Url" name="hubUrl" readOnly="true" value={this.props.hubUrl} />
                        <TextInput id="hubconfiguration-apikey" label="API Token" name="hubApiKey" value={this.state.hubApiKey} isSet={this.state.hubApiKeyIsSet} onChange={this.handleChange} errorMessage={this.props.fieldErrors.apiKey || this.props.fieldErrors.hubApiKey} />
                        <NumberInput id="hubconfiguration-timeout" label="Timeout" name="hubTimeout" value={this.state.hubTimeout} onChange={this.handleChange} />
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
                    <ConfigButtons isFixed={false} includeSave includeTest type="submit" onTestClick={this.handleTest} />
                </form>
            </div>
        );
    }
}

// Used for compile/validation of properties
HubConfiguration.propTypes = {
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
    fieldErrors: PropTypes.arrayOf(PropTypes.object),
    updateStatus: PropTypes.string,
    errorMessage: PropTypes.string,
    testStatus: PropTypes.string,
    getConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired,
    testConfig: PropTypes.func.isRequired
};

// Default values
HubConfiguration.defaultProps = {
    hubApiKey: '',
    id: null,
    hubProxyHost: null,
    hubProxyPassword: null,
    hubProxyPort: null,
    hubProxyUsername: null,
    errorMessage: null,
    updateStatus: null,
    fieldErrors: [],
    testStatus: ''
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
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
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(HubConfiguration);
