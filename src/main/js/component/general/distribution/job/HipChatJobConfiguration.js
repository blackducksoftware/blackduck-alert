import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import Select from 'react-select';
import TextInput from '../../../../field/input/TextInput';
import CheckboxInput from '../../../../field/input/CheckboxInput';
import {getDistributionJob} from '../../../../store/actions/distributions';

import BaseJobConfiguration from './BaseJobConfiguration';

class HipChatJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleColorChanged = this.handleColorChanged.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);
        this.state = {
            roomId: props.roomId,
            notify: props.notify,
            colorOptions: props.colorOptions,
            error: {}
        };
        this.loading = false;
    }

    componentDidMount() {
        const {baseUrl, distributionConfigId} = this.props;
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
                        roomId: jobConfig.roomId,
                        notify: jobConfig.notify,
                        color: jobConfig.color,
                        colorOptions: nextProps.colorOptions
                    });
                }
            }
        }
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

    handleChange({target}) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const {name} = target;
        this.handleStateValues(name, value);
    }

    handleColorChanged(option) {
        if (option) {
            this.handleStateValues('color', option.value);
        } else {
            this.handleStateValues('color', option);
        }
    }

    render() {
        const colorOptions = this.state.colorOptions;
        var selectedColorOption = null
        if (colorOptions) {
            selectedColorOption = colorOptions.find(option => option.value === this.state.color)
        }
        const content = (
            <div>
                <TextInput id="jobHipChatRoomId" label="Room Id" name="roomId" value={this.state.roomId} onChange={this.handleChange} errorName="roomIdError" errorValue={this.props.error.roomIdError}/>
                <CheckboxInput id="jobHipChatNotify" label="Notify" name="notify" value={this.state.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.props.error.notifyError}/>
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">Color</label>
                    <div className="d-inline-flex p-2 col-sm-9">
                        <Select
                            id="jobHipChatColor"
                            className="typeAheadField"
                            onChange={this.handleColorChanged}
                            isSearchable={true}
                            options={colorOptions}
                            placeholder="Choose the message color"
                            value={selectedColorOption}
                        />
                    </div>
                </div>
            </div>
        );
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

HipChatJobConfiguration.propTypes = {
    jobs: PropTypes.object,
    distributionConfigId: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    csrfToken: PropTypes.string,
    roomId: PropTypes.string,
    notify: PropTypes.bool,
    color: PropTypes.string,
    colorOptions: PropTypes.arrayOf(PropTypes.object),
    error: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired,
    alertChannelName: PropTypes.string.isRequired,
    fetching: PropTypes.bool,
    inProgress: PropTypes.bool,
    testingConfig: PropTypes.bool
};

HipChatJobConfiguration.defaultProps = {
    jobs: {},
    distributionConfigId: null,
    baseUrl: '/alert/api/configuration/channel/distribution/channel_hipchat',
    testUrl: '/alert/api/configuration/channel/distribution/channel_hipchat/test',
    distributionType: 'channel_hipchat',
    roomId: '',
    notify: false,
    color: '',
    colorOptions: [
        {label: 'Yellow', value: 'yellow'},
        {label: 'Green', value: 'green'},
        {label: 'Red', value: 'red'},
        {label: 'Purple', value: 'purple'},
        {label: 'Gray', value: 'gray'},
        {label: 'Random', value: 'random'}
    ],
    error: {},
    fetching: false,
    inProgress: false,
    testingConfig: false,
};

const mapStateToProps = state => ({
    csrfToken: state.session.csrfToken,
    jobs: state.distributions.jobs,
    error: state.distributions.error,
    fetching: state.distributions.fetching,
    inProgress: state.distributions.inProgress,
    testingConfig: state.distributions.testingConfig,
});

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url, id) => dispatch(getDistributionJob(url, id))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatJobConfiguration);
