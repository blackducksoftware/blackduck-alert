import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import NumberInput from '../../field/input/NumberInput';
import PasswordInput from '../../field/input/PasswordInput';
import ReadOnlyField from '../../field/ReadOnlyField';
import TextInput from '../../field/input/TextInput';
import ConfigButtons from '../common/ConfigButtons';

import {getConfig, testConfig, updateConfig} from '../../store/actions/config';

class BlackDuckConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            blackDuckApiKey: this.props.blackDuckApiKey,
            blackDuckApiKeyIsSet: this.props.blackDuckApiKeyIsSet,
            blackDuckProxyHost: this.props.blackDuckProxyHost,
            blackDuckProxyPassword: this.props.blackDuckProxyPassword,
            blackDuckProxyPasswordIsSet: this.props.blackDuckProxyPasswordIsSet,
            blackDuckProxyPort: this.props.blackDuckProxyPort,
            blackDuckProxyUsername: this.props.blackDuckProxyUsername,
            blackDuckTimeout: this.props.blackDuckTimeout,
            blackDuckUrl: this.props.blackDuckUrl
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
                blackDuckApiKey: nextProps.blackDuckApiKey,
                blackDuckApiKeyIsSet: nextProps.blackDuckApiKeyIsSet,
                blackDuckProxyHost: nextProps.blackDuckProxyHost,
                blackDuckProxyPassword: nextProps.blackDuckProxyPassword,
                blackDuckProxyPasswordIsSet: nextProps.blackDuckProxyPasswordIsSet,
                blackDuckProxyPort: nextProps.blackDuckProxyPort,
                blackDuckProxyUsername: nextProps.blackDuckProxyUsername,
                blackDuckTimeout: nextProps.blackDuckTimeout,
                blackDuckUrl: nextProps.blackDuckUrl
            });
        }
    }

    handleChange({target}) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleTest() {
        const {id} = this.props;
        this.props.testConfig({id, ...this.state});
    }

    handleSubmit(evt) {
        evt.preventDefault();
        const {id} = this.props;
        this.props.updateConfig({id, ...this.state});
    }

    render() {
        const {errorMessage, testStatus, updateStatus} = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-laptop"/>
                    Black Duck
                </h1>

                {testStatus && testStatus === 'SUCCESS' && <div className="alert alert-success">
                    <div>Test was successful!</div>
                </div>}

                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {updateStatus === 'UPDATED' && <div className="alert alert-success">
                    {'Update successful'}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div>
                        <TextInput id="blackDuckUrl"
                                   label="Url"
                                   name="blackDuckUrl"
                                   value={this.state.blackDuckUrl}
                                   onChange={this.handleChange}
                                   errorName="blackDuckUrlError"
                                   errorValue={this.props.fieldErrors.blackDuckUrl}/>
                        <PasswordInput id="blackDuckConfigurationApiToken" label="API Token" name="blackDuckApiKey" value={this.state.blackDuckApiKey} isSet={this.state.blackDuckApiKeyIsSet} onChange={this.handleChange}
                                       errorMessage={this.props.fieldErrors.apiKey || this.props.fieldErrors.blackDuckApiKey}/>
                        <NumberInput id="blackDuckConfigurationTimeout" label="Timeout" name="blackDuckTimeout" value={this.state.blackDuckTimeout} onChange={this.handleChange}/>
                        <div className="form-group">
                            <div className="col-sm-12">
                                <h2>Proxy Configuration <small>(Read-Only)</small></h2>
                            </div>
                        </div>
                        <ReadOnlyField label="Host Name" name="blackDuckProxyHost" value={this.props.blackDuckProxyHost}/>
                        <ReadOnlyField label="Port" name="blackDuckProxyPort" value={this.props.blackDuckProxyPort}/>
                        <ReadOnlyField label="Username" name="blackDuckProxyUsername" value={this.props.blackDuckProxyUsername}/>
                        <ReadOnlyField label="Proxy Password" name="blackDuckProxyPassword" isSet={this.props.blackDuckProxyPasswordIsSet}/>
                    </div>
                    <ConfigButtons isFixed={false} includeSave includeTest type="submit" onTestClick={this.handleTest}/>
                </form>
            </div>
        );
    }
}

// Used for compile/validation of properties
BlackDuckConfiguration.propTypes = {
    blackDuckApiKey: PropTypes.string,
    blackDuckApiKeyIsSet: PropTypes.bool.isRequired,
    blackDuckProxyHost: PropTypes.string,
    blackDuckProxyPassword: PropTypes.string,
    blackDuckProxyPasswordIsSet: PropTypes.bool.isRequired,
    blackDuckProxyPort: PropTypes.string,
    blackDuckProxyUsername: PropTypes.string,
    blackDuckTimeout: PropTypes.number.isRequired,
    blackDuckUrl: PropTypes.string.isRequired,
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
BlackDuckConfiguration.defaultProps = {
    blackDuckApiKey: '',
    id: null,
    blackDuckProxyHost: null,
    blackDuckProxyPassword: null,
    blackDuckProxyPort: null,
    blackDuckProxyUsername: null,
    errorMessage: null,
    updateStatus: null,
    fieldErrors: [],
    testStatus: ''
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    blackDuckApiKey: state.config.blackDuckApiKey,
    blackDuckApiKeyIsSet: state.config.blackDuckApiKeyIsSet,
    blackDuckProxyHost: state.config.blackDuckProxyHost,
    blackDuckProxyPassword: state.config.blackDuckProxyPassword,
    blackDuckProxyPasswordIsSet: state.config.blackDuckProxyPasswordIsSet,
    blackDuckProxyPort: state.config.blackDuckProxyPort,
    blackDuckProxyUsername: state.config.blackDuckProxyUsername,
    blackDuckTimeout: state.config.blackDuckTimeout,
    blackDuckUrl: state.config.blackDuckUrl,
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

export default connect(mapStateToProps, mapDispatchToProps)(BlackDuckConfiguration);
