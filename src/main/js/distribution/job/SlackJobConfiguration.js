import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import TextInput from 'field/input/TextInput';
import { getDistributionJob } from 'store/actions/distributionConfigs';

import BaseJobConfiguration from 'distribution/job/BaseJobConfiguration';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

const KEY_WEBHOOK = 'channel.slack.webhook';
const KEY_CHANNEL_NAME = 'channel.slack.channel.name';
const KEY_CHANNEL_USERNAME = 'channel.slack.channel.username';

const fieldDescriptions = {
    [KEY_WEBHOOK]: 'The Slack URL to receive alerts.',
    [KEY_CHANNEL_NAME]: 'The name of the Slack channel.',
    [KEY_CHANNEL_USERNAME]: 'The username to show as the message sender in the Slack channel.'
};

const fieldNames = [
    KEY_WEBHOOK,
    KEY_CHANNEL_NAME,
    KEY_CHANNEL_USERNAME
];

class SlackJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);
        this.state = {
            currentConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_SLACK)
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
        const value = target.type === 'checkbox' ? target.checked : target.value;
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
                    id={KEY_WEBHOOK}
                    label="Webhook"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_WEBHOOK)}
                    name={KEY_WEBHOOK}
                    value={FieldModelUtilities.getFieldModelSingleValueOrDefault(fieldModel, KEY_WEBHOOK, '')}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_WEBHOOK)}
                    errorValue={this.props.fieldErrors[KEY_WEBHOOK]}
                />
                <TextInput
                    id={KEY_CHANNEL_NAME}
                    label="Channel Name"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_CHANNEL_NAME)}
                    name={KEY_CHANNEL_NAME}
                    value={FieldModelUtilities.getFieldModelSingleValueOrDefault(fieldModel, KEY_CHANNEL_NAME, '')}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_CHANNEL_NAME)}
                    errorValue={this.props.fieldErrors[KEY_CHANNEL_NAME]}
                />
                <TextInput
                    id={KEY_CHANNEL_USERNAME}
                    label="Channel Username"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_CHANNEL_USERNAME)}
                    name={KEY_CHANNEL_USERNAME}
                    value={FieldModelUtilities.getFieldModelSingleValueOrDefault(fieldModel, KEY_CHANNEL_USERNAME, '')}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_CHANNEL_USERNAME)}
                    errorValue={this.props.fieldErrors[KEY_CHANNEL_USERNAME]}
                />
            </div>);
        return (<BaseJobConfiguration
            alertChannelName={DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_SLACK}
            job={this.state.jobConfig}
            handleCancel={this.props.handleCancel}
            handleSaveBtnClick={this.props.handleSaveBtnClick}
            getParentConfiguration={this.getConfiguration}
            childContent={content}
        />);
    }
}

SlackJobConfiguration.propTypes = {
    getDistributionJob: PropTypes.func.isRequired,
    job: PropTypes.object,
    jobId: PropTypes.string,
    fieldErrors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool
};

SlackJobConfiguration.defaultProps = {
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

export default connect(mapStateToProps, mapDispatchToProps)(SlackJobConfiguration);
