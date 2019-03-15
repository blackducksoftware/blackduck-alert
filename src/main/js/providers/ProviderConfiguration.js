import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

import { getConfig, testConfig, updateConfig } from 'store/actions/provider';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class ProviderConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTest = this.handleTest.bind(this);
        const fieldModel = FieldModelUtilities.createEmptyFieldModelFromFieldObject(props.descriptor.fields, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, props.descriptor.name);
        this.state = {
            currentConfig: fieldModel,
            currentDescriptor: props.descriptor
        };
    }

    componentDidMount() {
        this.props.getConfig();
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

    handleSubmit(evt) {
        evt.preventDefault();
        const fieldModel = this.state.currentConfig;
        this.props.updateConfig(fieldModel);
    }

    render() {
        const fieldModel = this.state.currentConfig;
        const descriptor = this.state.currentDescriptor;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon={descriptor.fontAwesomeIcon} configurationName={descriptor.label} description={descriptor.description} />
                {errorMessage && <div className="alert alert-danger"> d
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div>
                        {/* <FieldsPanel fieldModel={fieldModel} /> */}
                    </div>
                    <ConfigButtons isFixed={false} includeSave includeTest type="submit" onTestClick={this.handleTest} />
                </form>
            </div>
        );
    }
}

// Used for compile/validation of properties
ProviderConfiguration.propTypes = {
    descriptor: PropTypes.object.isRequired,
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
ProviderConfiguration.defaultProps = {
    currentConfig: {},
    errorMessage: null,
    updateStatus: null,
    fieldErrors: {},
    actionMessage: null
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    currentConfig: state.provider.config,
    actionMessage: state.provider.actionMessage,
    updateStatus: state.provider.updateStatus,
    errorMessage: state.provider.error.message,
    fieldErrors: state.provider.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(ProviderConfiguration);
