import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import Select from 'react-select-2';
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
            colorOptions: props.colorOptions
        };
    }

    componentDidMount() {
        const {baseUrl,distributionConfigId} = this.props;
        this.props.getDistributionJob(baseUrl,distributionConfigId);
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            if(nextProps.jobs[nextProps.distributionConfigId]) {
                this.setState({
                    roomId: nextProps.jobs[nextProps.distributionConfigId].roomId,
                    notify: nextProps.jobs[nextProps.distributionConfigId].notify,
                    color: nextProps.jobs[nextProps.distributionConfigId].color,
                    colorOptions: nextProps.colorOptions
                });
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

        const content = (
            <div>
                <TextInput id="jobHipChatRoomId" label="Room Id" name="roomId" value={this.state.roomId} onChange={this.handleChange} errorName="roomIdError" errorValue={this.props.errors.roomIdError} />
                <CheckboxInput id="jobHipChatNotify" label="Notify" name="notify" value={this.state.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.props.notifyError} />
                <div className="form-group">
                    <label className="col-sm-3 control-label">Color</label>
                    <div className="col-sm-8">
                        <Select
                        id="jobHipChatColor"
                        className="typeAheadField"
                        onChange={this.handleColorChanged}
                        searchable
                        options={this.state.colorOptions}
                        placeholder="Choose the message color"
                        value={this.state.color}
                        />
                    </div>
                </div>
            </div>
        );
        return (<BaseJobConfiguration
                    baseUrl={this.props.baseUrl}
                    testUrl={this.props.testUrl}
                    distributionConfigId = {this.props.distributionConfigId}
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
    errors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired
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
        { label: 'Yellow', value: 'yellow' },
        { label: 'Green', value: 'green' },
        { label: 'Red', value: 'red' },
        { label: 'Purple', value: 'purple' },
        { label: 'Gray', value: 'gray' },
        { label: 'Random', value: 'random' }
    ],
    errors: {}
};

const mapStateToProps = state => ({
    jobs: state.distributions.jobs,
    errors: state.distributions.errors
});

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url,id) => dispatch(getDistributionJob(url,id))
});

export default connect(mapStateToProps, mapDispatchToProps)(HipChatJobConfiguration);
