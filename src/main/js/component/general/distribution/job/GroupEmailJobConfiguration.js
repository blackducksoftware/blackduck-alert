import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import TextInput from '../../../../field/input/TextInput';
import BaseJobConfiguration from './BaseJobConfiguration';
import {getDistributionJob} from '../../../../store/actions/distributions';
import CheckboxInput from "../../../../field/input/CheckboxInput";


class GroupEmailJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);

        this.state = {
            emailSubjectLine: props.emailSubjectLine,
            projectOwnerOnly: props.projectOwnerOnly,
            error: {}
        }
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
                    emailSubjectLine: jobConfig.emailSubjectLine,
                    projectOwnerOnly: jobConfig.projectOwnerOnly
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
                <TextInput id="jobEmailSubject" label="Subject Line" name="emailSubjectLine" value={this.state.emailSubjectLine} onChange={this.handleChange} errorName="emailSubjectLineError"
                           errorValue={this.props.emailSubjectLineError}/>
                <CheckboxInput id="projectOwnerOnly" label="Project Owner Only" name="projectOwnerOnly" value={this.state.projectOwnerOnly} onChange={this.handleChange} errorName="projectOwnerOnlyError"
                               errorValue={this.props.error.projectOwnerOnlyError}/>
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

GroupEmailJobConfiguration.propTypes = {
    jobs: PropTypes.object,
    distributionConfigId: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    getDistributionJob: PropTypes.func.isRequired,
    csrfToken: PropTypes.string,
    groups: PropTypes.arrayOf(PropTypes.object),
    error: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired
};

GroupEmailJobConfiguration.defaultProps = {
    jobs: {},
    distributionConfigId: null,
    baseUrl: '/alert/api/configuration/channel/distribution/channel_email',
    testUrl: '/alert/api/configuration/channel/distribution/channel_email/test',
    distributionType: 'channel_email',
    emailSubjectLine: '',
    projectOwnerOnly: false,
    error: {}
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url, id) => dispatch(getDistributionJob(url, id)),
});

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    jobs: state.distributions.jobs,
    error: state.distributions.error
});

export default connect(mapStateToProps, mapDispatchToProps)(GroupEmailJobConfiguration);
