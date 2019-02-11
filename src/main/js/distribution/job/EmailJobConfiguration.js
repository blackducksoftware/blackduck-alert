import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import TextInput from 'field/input/TextInput';
import BaseJobConfiguration from 'distribution/job/BaseJobConfiguration';
import { getDistributionJob } from 'store/actions/distributionConfigs';
import CheckboxInput from 'field/input/CheckboxInput';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

const KEY_SUBJECT_LINE = 'email.subject.line';
const KEY_PROJECT_OWNER_ONLY = 'project.owner.only';
const KEY_EMAIL_ADDRESSES = 'email.addresses';

const fieldNames = [
    KEY_SUBJECT_LINE,
    KEY_PROJECT_OWNER_ONLY,
    KEY_EMAIL_ADDRESSES
];

class EmailJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);
        this.state = {
            currentConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL)
        };
        this.loading = false;
    }

    componentDidMount() {
        const { jobId } = this.props;
        if (jobId) {
            this.props.getDistributionJob(jobId);
            this.loading = true;
        }
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            if (this.loading) {
                this.loading = false;
                const jobConfig = nextProps.job;
                if (jobConfig && jobConfig.fieldModels) {
                    const channelModel = jobConfig.fieldModels.find(model => model.descriptorName.startsWith('channel_'));
                    this.setState({
                        jobConfig,
                        currentConfig: channelModel
                    });
                }
            }
        }
    }

    getConfiguration() {
        return this.state.currentConfig;
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);
        this.setState({
            currentConfig: newState
        });
    }

    render() {
        const fieldModel = this.state.currentConfig;
        const content = (
            <div>
                <TextInput
                    id={KEY_SUBJECT_LINE}
                    label="Subject Line"
                    name={KEY_SUBJECT_LINE}
                    value={FieldModelUtilities.getFieldModelSingleValueOrDefault(fieldModel, KEY_SUBJECT_LINE, '')}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_SUBJECT_LINE)}
                    errorValue={this.props.fieldErrors[KEY_SUBJECT_LINE]}
                />
                <CheckboxInput
                    id={KEY_PROJECT_OWNER_ONLY}
                    label="Project Owner Only"
                    name={KEY_PROJECT_OWNER_ONLY}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, KEY_PROJECT_OWNER_ONLY)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROJECT_OWNER_ONLY)}
                    errorValue={this.props.fieldErrors[KEY_PROJECT_OWNER_ONLY]}
                />
            </div>);
        return (<BaseJobConfiguration
            alertChannelName={this.props.alertChannelName}
            job={this.state.jobConfig}
            handleCancel={this.props.handleCancel}
            handleSaveBtnClick={this.props.handleSaveBtnClick}
            getParentConfiguration={this.getConfiguration}
            childContent={content}
        />);
    }
}

EmailJobConfiguration.propTypes = {
    job: PropTypes.object,
    jobId: PropTypes.string,
    getDistributionJob: PropTypes.func.isRequired,
    fieldErrors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool
};

EmailJobConfiguration.defaultProps = {
    job: null,
    jobId: null,
    fieldErrors: {},
    fetching: false,
    inProgress: false
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: id => dispatch(getDistributionJob(id))
});

const mapStateToProps = state => ({
    job: state.distributionConfigs.job,
    fieldErrors: state.distributionConfigs.error,
    fetching: state.distributionConfigs.fetching,
    inProgress: state.distributionConfigs.inProgress
});

export default connect(mapStateToProps, mapDispatchToProps)(EmailJobConfiguration);
