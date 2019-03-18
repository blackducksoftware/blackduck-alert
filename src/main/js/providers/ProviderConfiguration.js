import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import FieldsPanel from 'providers/FieldsPanel';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

import { getConfig, testConfig, updateConfig } from 'store/actions/provider';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';

class ProviderConfiguration extends React.Component {
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
        this.props.getConfig();
        this.state = {
            currentConfig: this.props.currentConfig
        };
    }

    handleTest() {
        const fieldModel = this.state.currentConfig;
        this.props.testConfig(fieldModel);
        this.state = {
            currentConfig: this.props.currentConfig
        };
    }

    handleSubmit(evt) {
        evt.preventDefault();
        const fieldModel = this.state.currentConfig;
        this.props.updateConfig(fieldModel);
        this.state = {
            currentConfig: this.props.currentConfig
        };
    }

    render() {
        const {
            fontAwesomeIcon, label, description, fields
        } = this.state.currentDescriptor;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon={fontAwesomeIcon} configurationName={label} description={description} />
                {errorMessage && <div className="alert alert-danger"> d
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div>
                        <FieldsPanel currentConfig={this.state.currentConfig} fieldKeys={this.state.currentKeys} descriptorFields={fields} />
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
    actionMessage: null
};

// Mapping redux state -> react props
const mapStateToProps = state => ({
    currentConfig: state.provider.config,
    actionMessage: state.provider.actionMessage,
    updateStatus: state.provider.updateStatus,
    errorMessage: state.provider.error.message
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    updateConfig: config => dispatch(updateConfig(config)),
    testConfig: config => dispatch(testConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(ProviderConfiguration);
