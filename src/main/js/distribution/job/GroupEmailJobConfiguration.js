import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import TextInput from 'field/input/TextInput';
import BaseJobConfiguration from 'distribution/job/BaseJobConfiguration';
import { getDistributionJob } from 'store/actions/distributionConfigs';
import CheckboxInput from 'field/input/CheckboxInput';
import * as FieldModelUtil from 'util/fieldModelUtilities';
import * as DescriptorUtil from 'util/descriptorUtilities';

const KEY_SUBJECT_LINE = 'email.subject.line';
const KEY_PROJECT_OWNER_ONLY = 'project.owner.only';
const KEY_EMAIL_ADDRESSES = 'email.addresses';

const fieldNames = [
    KEY_SUBJECT_LINE,
    KEY_PROJECT_OWNER_ONLY,
    KEY_EMAIL_ADDRESSES
];

class GroupEmailJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);
        this.state = {
            currentConfig: FieldModelUtil.createEmptyFieldModel(fieldNames, DescriptorUtil.CONTEXT_TYPE.DISTRIBUTION, DescriptorUtil.DESCRIPTOR_NAME.CHANNEL_EMAIL)
        };
        this.loading = false;
    }

    componentDidMount() {
        const { baseUrl, distributionConfigId } = this.props;
        this.props.getDistributionJob(baseUrl, distributionConfigId);
        this.loading = true;
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            if (this.loading) {
                this.loading = false;
                const jobConfig = nextProps.jobs[nextProps.distributionConfigId];
                if (jobConfig) {
                    this.setState({
                        emailSubjectLine: jobConfig.emailSubjectLine,
                        projectOwnerOnly: jobConfig.projectOwnerOnly
                    });
                }
            }
        }
    }

    getConfiguration() {
        return Object.assign({}, this.state, {
            distributionType: DescriptorUtil.DESCRIPTOR_NAME.CHANNEL_EMAIL
        });
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const newState = FieldModelUtil.updateFieldModelSingleValue(this.state.currentConfig, target.name, value);
        this.setState({
            currentConfig: newState
        });
    }

    render() {
        const content = (
            <div>
                <TextInput
                    id={KEY_SUBJECT_LINE}
                    label="Subject Line"
                    name={KEY_SUBJECT_LINE}
                    value={FieldModelUtil.getFieldModelSingleValue(fieldModel, KEY_SUBJECT_LINE)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtil.createFieldModelErrorKey(KEY_SUBJECT_LINE)}
                    errorValue={this.props.fieldErrors[KEY_SUBJECT_LINE]}
                />
                <CheckboxInput
                    id={KEY_PROJECT_OWNER_ONLY}
                    label="Project Owner Only"
                    name={KEY_PROJECT_OWNER_ONLY}
                    isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, KEY_PROJECT_OWNER_ONLY)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtil.createFieldModelErrorKey(KEY_PROJECT_OWNER_ONLY)}
                    errorValue={this.props.fieldErrors[KEY_PROJECT_OWNER_ONLY]}
                />
            </div>);
        return (<BaseJobConfiguration
            baseUrl={this.props.baseUrl}
            testUrl={this.props.testUrl}
            alertChannelName={this.props.alertChannelName}
            distributionConfigId={this.props.distributionConfigId}
            handleCancel={this.props.handleCancel}
            handleSaveBtnClick={this.props.handleSaveBtnClick}
            getParentConfiguration={this.getConfiguration}
            childContent={content}
        />);
    }
}

GroupEmailJobConfiguration.propTypes = {
    jobs: PropTypes.object,
    distributionConfigId: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    getDistributionJob: PropTypes.func.isRequired,
    fieldErrors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool
};

GroupEmailJobConfiguration.defaultProps = {
    jobs: {},
    distributionConfigId: null,
    baseUrl: `/alert/api/configuration/channel/distribution/${DescriptorUtil.DESCRIPTOR_NAME.CHANNEL_EMAIL}`,
    testUrl: `/alert/api/configuration/channel/distribution/${DescriptorUtil.DESCRIPTOR_NAME.CHANNEL_EMAIL}/test`,
    fieldErrors: {},
    fetching: false,
    inProgress: false
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url, id) => dispatch(getDistributionJob(url, id))
});

const mapStateToProps = state => ({
    jobs: state.distributionConfigs.jobs,
    fieldErrors: state.distributionConfigs.error,
    fetching: state.distributionConfigs.fetching,
    inProgress: state.distributionConfigs.inProgress
});

export default connect(mapStateToProps, mapDispatchToProps)(GroupEmailJobConfiguration);
