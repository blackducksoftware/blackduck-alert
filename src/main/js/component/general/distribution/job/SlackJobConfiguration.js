import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import TextInput from '../../../../field/input/TextInput';
import {getDistributionJob} from '../../../../store/actions/distributions';

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
            channelName: props.channelName
        };
    }

    componentDidMount() {
        const {baseUrl, distributionConfigId} = this.props;
        this.props.getDistributionJob(baseUrl, distributionConfigId);
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
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

    handleChange({target}) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const {name} = target;
        this.handleStateValues(name, value);
    }

    handleStateValues(name, value) {
        this.setState({
            [name]: value
        });
    }

    getConfiguration() {
        return Object.assign({}, this.state, {
            distributionType: this.props.distributionType
        });
    }

    render() {
        const content = (
            <div>
                <TextInput id="jobSlackWebhook" label="Webhook" name="webhook" value={this.state.webhook} onChange={this.handleChange} errorName="webhookError" errorValue={this.props.errors.webhookError}/>
                <TextInput id="jobSlackChannelName" label="Channel Name" name="channelName" value={this.state.channelName} onChange={this.handleChange} errorName="channelNameError" errorValue={this.props.errors.channelNameError}/>
                <TextInput id="jobSlackChannelUsername" label="Channel Username" name="channelUsername" value={this.state.channelUsername} onChange={this.handleChange} errorName="channelUsernameError"
                           errorValue={this.props.channelUsernameError}/>
            </div>);
        return (<BaseJobConfiguration
            baseUrl={this.props.baseUrl}
            testUrl={this.props.testUrl}
            alertChannelName={this.props.alertChannelName}
            distributionConfigId={this.props.distributionConfigId}
            handleCancel={this.props.handleCancel}
            handleSaveBtnClick={this.props.handleSaveBtnClick}
            getParentConfiguration={this.getConfiguration}
            childContent={content}/>);
    }
}

SlackJobConfiguration.propTypes = {
    jobs: PropTypes.object,
    distributionConfigId: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    csrfToken: PropTypes.string,
    webhook: PropTypes.string,
    channelUserName: PropTypes.string,
    errors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired
};

SlackJobConfiguration.defaultProps = {
    jobs: {},
    distributionConfigId: null,
    baseUrl: '/alert/api/configuration/channel/distribution/channel_slack',
    testUrl: '/alert/api/configuration/channel/distribution/channel_slack/test',
    distributionType: 'channel_slack',
    webhook: '',
    channelName: '',
    channelUserName: '',
    errors: {}
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url, id) => dispatch(getDistributionJob(url, id))
});

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    jobs: state.distributions.jobs,
    errors: state.distributions.errors
});

export default connect(mapStateToProps, mapDispatchToProps)(SlackJobConfiguration);
