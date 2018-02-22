import React from 'react';
import PropTypes from 'prop-types';
import {connect} from "react-redux";

import TextInput from '../../field/input/TextInput';
import ConfigButtons from '../ConfigButtons';
import {getConfig, testConfig, updateConfig} from "../../store/actions/hipChatConfig";

class HipChatConfiguration extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
            apiKey: '',
            apiKeyIsSet: false
        };

		this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
	}

	componentDidMount() {
	    this.props.getConfig();
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            apiKey: nextProps.apiKey || '',
            apiKeyIsSet: nextProps.apiKeyIsSet
        });
    }

    handleChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();

        const { id } = this.props;
        this.props.updateConfig({id, ...this.state});
    }

    handleTest() {
        const { id } = this.props;
        this.props.testConfig({ id, ...this.state});
    }

    render() {

        const { errorMessage, testStatus, updateStatus } = this.props;
        return (
            <div>
                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <h1>Server Configuration / HipChat</h1>

                    { testStatus && testStatus === 'SUCCESS' && <div className="alert alert-success">
                        <div>Test was successful!</div>
                    </div>}

                    { errorMessage && <div className="alert alert-danger">
                        { errorMessage }
                    </div> }

                    { updateStatus === 'UPDATED' && <div className="alert alert-success">
                        { 'Update successful' }
                    </div> }

                    <TextInput label="Api Key" type="text" name="apiKey" value={this.state.apiKey} isSet={this.state.apiKeyIsSet} onChange={this.handleChange} errorName="apiKeyError" errorValue={this.props.fieldErrors.apiKey}></TextInput>
                    <ConfigButtons includeSave={true} includeTest={true} onTestClick={ this.handleTest } />
                </form>
            </div>
        );
	}
};

HipChatConfiguration.propTypes = {
    apiKey: PropTypes.string,
    apiKeyIsSet: PropTypes.bool,
    id: PropTypes.string,
    testStatus: PropTypes.string,
    errorMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    fieldErrors: PropTypes.arrayOf(PropTypes.any)
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    apiKey: state.hipChatConfig.apiKey,
    apiKeyIsSet: state.hipChatConfig.apiKeyIsSet,
    testStatus: state.hipChatConfig.testStatus,
    updateStatus: state.hipChatConfig.updateStatus,
    errorMessage: state.hipChatConfig.error.message,
    fieldErrors: state.hipChatConfig.error.fieldErrors,
    id: state.hipChatConfig.id
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: (config) => dispatch(updateConfig(config)),
    testConfig: (config) => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatConfiguration);
