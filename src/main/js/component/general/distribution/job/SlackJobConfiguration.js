import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import TextInput from '../../../../field/input/TextInput';
import { getDistributionJob } from '../../../../store/actions/distributions';

import BaseJobConfiguration from './BaseJobConfiguration';

class SlackJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);
        this.state = {
            webhook: props.webhook,
            channelUsername: props.channelUsername,
            channelName: props.channelName,
            error: {}
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
                        webhook: jobConfig.webhook,
                        channelUsername: jobConfig.channelUsername,
                        channelName: jobConfig.channelName
                    });
                }
            }
        }
    }

    getConfiguration() {
        return Object.assign({}, this.state, {
            distributionType: this.props.distributionType
        });
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const { name } = target;
        this.handleStateValues(name, value);
    }

    handleStateValues(name, value) {
        this.setState({
            [name]: value
        });
    }

    render() {
        const content = (
            <div>
                <TextInput id="jobSlackWebhook" label="Webhook" name="webhook" value={this.state.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.props.error.webhookError} />
                <TextInput id="jobSlackChannelName" label="Channel Name" name="channelName" value={this.state.channelName} onChange={this.handleChange} errorName="channelNameError" errorValue={this.props.error.channelNameError} />
                <TextInput
                    id="jobSlackChannelUsername"
                    label="Channel Username"
                    name="channelUsername"
                    value={this.state.channelUsername}
                    onChange={this.handleChange}
                    errorName="channelUsernameError"
                    errorValue={this.props.error.channelUsernameError}
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

SlackJobConfiguration.propTypes = {
    getDistributionJob: PropTypes.func.isRequired,
    jobs: PropTypes.object,
    distributionConfigId: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    webhook: PropTypes.string,
    channelName: PropTypes.string,
    channelUsername: PropTypes.string,
    error: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool
};

SlackJobConfiguration.defaultProps = {
    jobs: {},
    distributionConfigId: null,
    baseUrl: '/alert/api/configuration/channel/distribution/channel_slack',
    testUrl: '/alert/api/configuration/channel/distribution/channel_slack/test',
    distributionType: 'channel_slack',
    webhook: '',
    channelName: '',
    channelUsername: '',
    error: {},
    fetching: false,
    inProgress: false
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url, id) => dispatch(getDistributionJob(url, id))
});

const mapStateToProps = state => ({
    jobs: state.distributions.jobs,
    error: state.distributions.error,
    fetching: state.distributions.fetching,
    inProgress: state.distributions.inProgress
});

export default connect(mapStateToProps, mapDispatchToProps)(SlackJobConfiguration);
