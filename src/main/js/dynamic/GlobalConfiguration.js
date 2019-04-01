import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import FieldsPanel from 'field/FieldsPanel';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

import { getConfig, testConfig, updateConfig } from 'store/actions/globalConfiguration';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';

class GlobalConfiguration extends React.Component {
    constructor(props) {
        super(props);
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
        if (this.props.currentConfig !== prevProps.currentConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            const fieldModel = FieldModelUtilities.checkModelOrCreateEmpty(this.props.currentConfig, this.state.currentKeys);
            this.setState({
                currentConfig: fieldModel
            });
        }
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
        const {
            fontAwesomeIcon, label, description, fields
        } = this.state.currentDescriptor;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon={fontAwesomeIcon} configurationName={label} description={description} />
                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate="true">
                    <div>
                        <FieldsPanel currentConfig={this.state.currentConfig} fieldKeys={this.state.currentKeys} descriptorFields={fields} updateStatus={this.props.updateStatus} fieldErrors={this.props.fieldErrors} />
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
    testConfig: PropTypes.func.isRequired
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
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(GlobalConfiguration);
