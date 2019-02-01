import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import NumberInput from 'field/input/NumberInput';
import PasswordInput from 'field/input/PasswordInput';
import TextInput from 'field/input/TextInput';
import ConfigButtons from 'component/common/ConfigButtons';

import { getConfig, testConfig, updateConfig } from 'store/actions/blackduck';
import * as FieldModelUtil from 'util/fieldModelUtilities';
import * as DescriptorUtil from 'util/descriptorUtilities';

const KEY_BLACKDUCK_URL = 'blackduck.url';
const KEY_BLACKDUCK_API_KEY = 'blackduck.api.key';
const KEY_BLACKDUCK_TIMEOUT = 'blackduck.timeout';


const fieldNames = [
    KEY_BLACKDUCK_URL,
    KEY_BLACKDUCK_TIMEOUT,
    KEY_BLACKDUCK_API_KEY
];

class BlackDuckConfiguration
    extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
        let fieldModel = FieldModelUtil.createEmptyFieldModel(fieldNames, DescriptorUtil.CONTEXT_TYPE.GLOBAL, 'provider_blackduck');
        fieldModel = this.updateDefaults(fieldModel);
        this.state = {
            currentConfig: fieldModel
        };
    }

    componentDidMount() {
        this.props.getConfig();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.currentConfig !== prevProps.currentConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            let fieldModel = FieldModelUtil.checkModelOrCreateEmpty(this.props.currentConfig, fieldNames);
            fieldModel = this.updateDefaults(fieldModel);
            this.setState({
                currentConfig: fieldModel
            });
        }
    }

    updateDefaults(fieldModel) {
        let currentFieldModel = fieldModel;
        const currentTimeout = FieldModelUtil.getFieldModelSingleValue(currentFieldModel, KEY_BLACKDUCK_TIMEOUT);
        if (!currentTimeout) {
            currentFieldModel = FieldModelUtil.updateFieldModelSingleValue(currentFieldModel, KEY_BLACKDUCK_TIMEOUT, 300);
        }
        return currentFieldModel;
    }


    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtil.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);
        this.setState({
            currentConfig: newState
        });
    }

    handleTest() {
        const fieldModel = this.state.currentConfig;
        this.props.testConfig(fieldModel);
    }

    handleSubmit(evt) {
        evt.preventDefault();
        const fieldModel = this.state.currentConfig;
        this.props.updateConfig(fieldModel);
    }

    render() {
        const fieldModel = this.state.currentConfig;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-laptop" />
                    Black Duck
                </h1>

                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div>
                        <TextInput
                            id={KEY_BLACKDUCK_URL}
                            label="Url"
                            name={KEY_BLACKDUCK_URL}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_BLACKDUCK_URL)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_URL)}
                            errorValue={this.props.fieldErrors[KEY_BLACKDUCK_URL]}
                        />
                        <PasswordInput
                            id={KEY_BLACKDUCK_API_KEY}
                            label="API Token"
                            name={KEY_BLACKDUCK_API_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_BLACKDUCK_API_KEY)}
                            isSet={FieldModelUtil.isFieldModelValueSet(fieldModel, KEY_BLACKDUCK_API_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_API_KEY)}
                            errorValue={this.props.fieldErrors[KEY_BLACKDUCK_API_KEY]}
                        />
                        <NumberInput
                            id={KEY_BLACKDUCK_TIMEOUT}
                            label="Timeout"
                            name={KEY_BLACKDUCK_TIMEOUT}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_BLACKDUCK_TIMEOUT)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(KEY_BLACKDUCK_TIMEOUT)}
                            errorValue={this.props.fieldErrors[KEY_BLACKDUCK_TIMEOUT]}
                        />
                    </div>
                    <ConfigButtons isFixed={false} includeSave includeTest type="submit" onTestClick={this.handleTest} />
                </form>
            </div>
        );
    }
}

// Used for compile/validation of properties
BlackDuckConfiguration.propTypes = {
    currentConfig: PropTypes.object,
    fieldErrors: PropTypes.object,
    updateStatus: PropTypes.string,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string,
    getConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired,
    testConfig: PropTypes.func.isRequired
};

// Default values
BlackDuckConfiguration.defaultProps = {
    currentConfig: {},
    errorMessage: null,
    updateStatus: null,
    fieldErrors: {},
    actionMessage: null
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    currentConfig: state.blackduck.config,
    actionMessage: state.blackduck.actionMessage,
    updateStatus: state.blackduck.updateStatus,
    errorMessage: state.blackduck.error.message,
    fieldErrors: state.blackduck.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(BlackDuckConfiguration);
