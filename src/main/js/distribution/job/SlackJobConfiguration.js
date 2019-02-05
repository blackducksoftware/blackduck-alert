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
        const { distributionConfigId } = this.props;
        this.props.getDistributionJob(distributionConfigId);
        this.loading = true;
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            if (this.loading) {
                this.loading = false;
                const jobConfig = nextProps.jobs[nextProps.distributionConfigId];
                if (jobConfig) {
                    this.setState({
                        webhook: jobConfig.webhook,
                        channelUsername: jobConfig.channelUsername,
                        channelName: jobConfig.channelName
                    });
                }
            }
        }
    }

    getConfiguration() {
        return Object.assign({}, this.state, {});
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
                    name={KEY_WEBHOOK}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_WEBHOOK)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_WEBHOOK)}
                    errorValue={this.props.fieldErrors[KEY_WEBHOOK]}
                />
                <TextInput
                    id={KEY_CHANNEL_NAME}
                    label="Channel Name"
                    name={KEY_CHANNEL_NAME}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_CHANNEL_NAME)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_CHANNEL_NAME)}
                    errorValue={this.props.fieldErrors[KEY_CHANNEL_NAME]}
                />
                <TextInput
                    id={KEY_CHANNEL_USERNAME}
                    label="Channel Username"
                    name={KEY_CHANNEL_USERNAME}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_CHANNEL_USERNAME)}
                    onChange={this.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_CHANNEL_USERNAME)}
                    errorValue={this.props.fieldErrors[KEY_CHANNEL_USERNAME]}
                />
            </div>);
        return (<BaseJobConfiguration
            alertChannelName={DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_SLACK}
            distributionConfigId={this.props.distributionConfigId}
            handleCancel={this.props.handleCancel}
            handleSaveBtnClick={this.props.handleSaveBtnClick}
            getParentConfiguration={this.getConfiguration}
            childContent={content}
        />);
    }
}

SlackJobConfiguration.propTypes = {
    getDistributionJob: PropTypes.func.isRequired,
    jobs: PropTypes.arrayOf(PropTypes.object),
    distributionConfigId: PropTypes.string,
    fieldErrors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool
};

SlackJobConfiguration.defaultProps = {
    jobs: [],
    distributionConfigId: null,
    fieldErrors: {},
    fetching: false,
    inProgress: false
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: id => dispatch(getDistributionJob(id))
});

const mapStateToProps = state => ({
    jobs: state.distributionConfigs.jobs,
    fieldErrors: state.distributionConfigs.error,
    fetching: state.distributionConfigs.fetching,
    inProgress: state.distributionConfigs.inProgress
});

export default connect(mapStateToProps, mapDispatchToProps)(SlackJobConfiguration);
