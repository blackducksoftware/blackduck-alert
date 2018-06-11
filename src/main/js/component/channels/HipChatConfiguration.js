import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import TextInput from '../../field/input/TextInput';
import ConfigButtons from '../common/ConfigButtons';
import { getConfig, testConfig, updateConfig, toggleShowHostServer } from '../../store/actions/hipChatConfig';

class HipChatConfiguration extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            apiKey: '',
            apiKeyIsSet: false,
            hostServer: '',
            dataLoaded: false
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
                dataLoaded: true,
                apiKey: nextProps.apiKey || '',
                apiKeyIsSet: nextProps.apiKeyIsSet,
                hostServer: nextProps.hostServer || ''
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();

        const { id } = this.props;
        this.props.updateConfig({ id, ...this.state });
    }

    handleTest() {
        const { id } = this.props;
        this.props.testConfig({ id, ...this.state });
    }

    render() {
        const disabled = this.props.fetching || !this.state.dataLoaded;
        const { errorMessage, testStatus, updateStatus } = this.props;
        const showAdvanced = this.props.showAdvanced;
        const showAdvancedLabel = (showAdvanced) ? 'Hide Advanced' : 'Show Advanced';
        return (
            <div>
                <h1>
                    <span className="fa fa-comments" />
                    HipChat
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

                <form className="form-horizontal" disabled={disabled} onSubmit={this.handleSubmit}>
                    <TextInput id="hipChat-key" label="Api Key" name="apiKey" readOnly={disabled} value={this.state.apiKey} isSet={this.state.apiKeyIsSet} onChange={this.handleChange} errorName="apiKeyError" errorValue={this.props.fieldErrors.apiKey} />

                    <div className="form-group">
                        <div className="col-sm-8 col-sm-offset-3">
                            <button type="button" className="btn-link" onClick={() => { this.props.toggleShowHostServer(!showAdvanced); return false; }}>
                                {showAdvancedLabel}
                            </button>
                        </div>
                    </div>

                    {showAdvanced &&
                    <div>
                        <TextInput id="hipChat-serverUrl" label="HipChat Host Server Url" name="hostServer" value={this.state.hostServer} onChange={this.handleChange} errorName="hostServerError" errorValue={this.props.fieldErrors.hostServer} />
                    </div>
                    }
                    
                    <ConfigButtons submitId="hipChat-submit" cancelId="hipChat-cancel" includeSave includeTest onTestClick={this.handleTest} />
                </form>
            </div>
        );
    }
}

HipChatConfiguration.propTypes = {
    hostServer: PropTypes.string,
    apiKey: PropTypes.string,
    apiKeyIsSet: PropTypes.bool,
    id: PropTypes.string,
    testStatus: PropTypes.string,
    errorMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    fieldErrors: PropTypes.arrayOf(PropTypes.any),
    fetching: PropTypes.bool.isRequired,
    getConfig: PropTypes.func.isRequired,
    testConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired,
    showAdvanced: PropTypes.bool.isRequired,
    toggleShowHostServer: PropTypes.func.isRequired,
};

HipChatConfiguration.defaultProps = {
    hostServer: null,
    apiKey: null,
    apiKeyIsSet: false,
    id: null,
    testStatus: null,
    errorMessage: null,
    updateStatus: null,
    fieldErrors: []
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    hostServer: state.hipChatConfig.hostServer,
    apiKey: state.hipChatConfig.apiKey,
    apiKeyIsSet: state.hipChatConfig.apiKeyIsSet,
    testStatus: state.hipChatConfig.testStatus,
    updateStatus: state.hipChatConfig.updateStatus,
    errorMessage: state.hipChatConfig.error.message,
    fieldErrors: state.hipChatConfig.error.fieldErrors,
    id: state.hipChatConfig.id,
    fetching: state.hipChatConfig.fetching,
    showAdvanced: state.hipChatConfig.showAdvanced
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    toggleShowHostServer: toggle => dispatch(toggleShowHostServer(toggle)),
    getConfig: () => dispatch(getConfig()),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatConfiguration);
