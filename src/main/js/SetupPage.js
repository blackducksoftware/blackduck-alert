import { connect } from 'react-redux';
import React, { Component } from 'react';
import { getInitialSystemDescriptor, getInitialSystemSetup, saveInitialSystemSetup } from 'store/actions/system';
import PropTypes from 'prop-types';
import StatusMessage from 'field/StatusMessage';
import FieldsPanel from 'field/FieldsPanel';
import ConfigButtons from 'component/common/ConfigButtons';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import * as FieldMapping from 'util/fieldMapping';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

class SetupPage extends Component {
    constructor(props) {
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);

        this.state = {
            settingsData: {},
            fields: []
        };
    }

    componentWillMount() {
        this.props.getSettings();
        this.props.getDescriptor();
    }

    componentWillReceiveProps(nextProps) {
        const currentStatus = this.props.updateStatus;
        const { settingsDescriptor, updateStatus, settingsData } = nextProps;

        if (updateStatus !== currentStatus && updateStatus === 'DESCRIPTOR_FETCHED') {
            const { fields } = settingsDescriptor;
            this.setState({
                fields
            });
        } else if (updateStatus !== currentStatus && updateStatus === 'FETCHED') {
            this.setState({
                settingsData
            });
        }
    }

    handleSubmit(evt) {
        evt.preventDefault();
        this.props.saveSettings(this.state.settingsData);
    }

    render() {
        const { errorMessage, actionMessage } = this.props;
        const { fields, settingsData } = this.state;
        const saving = this.props.updateStatus === 'UPDATING' || this.props.updateStatus === 'FETCHING';

        return (
            <div className="settingsWrapper">
                <div className="settingsContainer">
                    <div className="settingsBox">
                        <StatusMessage errorMessage={errorMessage} actionMessage={actionMessage} />
                        <form
                            method="POST"
                            className="form-horizontal loginForm"
                            onSubmit={this.handleSubmit}
                            noValidate
                        >
                            <FieldsPanel
                                descriptorFields={fields}
                                currentConfig={settingsData}
                                fieldErrors={this.props.fieldErrors}
                                handleChange={this.handleChange}
                                self={this}
                                stateName="settingsData"
                            />
                            <ConfigButtons includeSave type="submit" performingAction={saving} isFixed={false} />
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

SetupPage.propTypes = {
    settingsDescriptor: PropTypes.object.isRequired,
    settingsData: PropTypes.object.isRequired,
    saveSettings: PropTypes.func.isRequired,
    getDescriptor: PropTypes.func.isRequired,
    getSettings: PropTypes.func.isRequired,
    updateStatus: PropTypes.string,
    fieldErrors: PropTypes.object,
    errorMessage: PropTypes.string,
    actionMessage: PropTypes.string
};

SetupPage.defaultProps = {
    fieldErrors: {},
    updateStatus: '',
    errorMessage: null,
    actionMessage: null
};

const mapStateToProps = state => ({
    settingsDescriptor: state.system.settingsDescriptor,
    settingsData: state.system.settingsData,
    errorMessage: state.system.errorMessage,
    actionMessage: state.system.actionMessage,
    updateStatus: state.system.updateStatus,
    currentSettingsData: state.system.settingsData,
    fieldErrors: state.system.error
});

const mapDispatchToProps = dispatch => ({
    saveSettings: setupData => dispatch(saveInitialSystemSetup(setupData)),
    getSettings: () => dispatch(getInitialSystemSetup()),
    getDescriptor: () => dispatch(getInitialSystemDescriptor())
});

export default connect(mapStateToProps, mapDispatchToProps)(SetupPage);
