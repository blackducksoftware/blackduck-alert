import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import FieldsPanel from 'field/FieldsPanel';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

import { deleteConfig, getConfig, testConfig, updateConfig } from 'store/actions/globalConfiguration';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import StatusMessage from 'field/StatusMessage';

class GlobalConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
        const { fields, name } = this.props.descriptor;
        const fieldKeys = FieldMapping.retrieveKeys(fields);
        const fieldModel = FieldModelUtilities.createEmptyFieldModelFromFieldObject(fieldKeys, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, name);
        this.state = {
            currentConfig: fieldModel,
            currentDescriptor: this.props.descriptor,
            currentKeys: fieldKeys
        };
    }

    componentDidMount() {
        const fieldModel = this.state.currentConfig;
        this.props.getConfig(fieldModel.descriptorName);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.currentConfig !== prevProps.currentConfig && this.props.updateStatus === 'DELETED') {
            const newState = FieldModelUtilities.createEmptyFieldModel(this.state.currentKeys, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, this.state.currentDescriptor.name);
            this.setState({
                currentConfig: newState
            });
        } else if (this.props.currentConfig !== prevProps.currentConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            const fieldModel = FieldModelUtilities.checkModelOrCreateEmpty(this.props.currentConfig, this.state.currentKeys);
            this.setState({
                currentConfig: fieldModel
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);

        this.setState({
            currentConfig: newState
        });
    }

    handleTest() {
        const fieldModel = this.state.currentConfig;
        this.props.testConfig(fieldModel);
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        const fieldModel = this.state.currentConfig;
        const emptyModel = !FieldModelUtilities.hasAnyValuesExcludingId(fieldModel);
        const id = FieldModelUtilities.getFieldModelId(fieldModel);
        if (emptyModel && id) {
            this.props.deleteConfig(id);
        } else {
            this.props.updateConfig(fieldModel);
        }
    }

    render() {
        const {
            fontAwesomeIcon, label, description, fields, name
        } = this.state.currentDescriptor;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon={fontAwesomeIcon} configurationName={label} description={description} />
                <StatusMessage errorMessage={errorMessage} actionMessage={actionMessage} />

                <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate={true}>
                    <div>
                        <FieldsPanel descriptorFields={fields} currentConfig={this.state.currentConfig} fieldErrors={this.props.fieldErrors} handleChange={this.handleChange} />
                    </div>
                    <ConfigButtons isFixed={false} includeSave includeTest type="submit" onTestClick={this.handleTest} />
                </form>
            </div>
        );
    }
}

// Used for compile/validation of properties
GlobalConfiguration.propTypes = {
    descriptor: PropTypes.object.isRequired,
    currentConfig: PropTypes.object,
    fieldErrors: PropTypes.object,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    getConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired,
    testConfig: PropTypes.func.isRequired,
    deleteConfig: PropTypes.func.isRequired
};

// Default values
GlobalConfiguration.defaultProps = {
    currentConfig: {},
    errorMessage: null,
    actionMessage: null,
    updateStatus: null,
    fieldErrors: {}
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    currentConfig: state.globalConfiguration.config,
    actionMessage: state.globalConfiguration.actionMessage,
    updateStatus: state.globalConfiguration.updateStatus,
    errorMessage: state.globalConfiguration.error.message,
    fieldErrors: state.globalConfiguration.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: descriptorName => dispatch(getConfig(descriptorName)),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config)),
    deleteConfig: id => dispatch(deleteConfig(id))
});

export default connect(mapStateToProps, mapDispatchToProps)(GlobalConfiguration);
