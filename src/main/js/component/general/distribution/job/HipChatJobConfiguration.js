import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select-2';
import TextInput from '../../../../field/input/TextInput';
import CheckboxInput from '../../../../field/input/CheckboxInput';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class HipChatJobConfiguration extends BaseJobConfiguration {
    constructor(props) {
        super(props);
        this.handleColorChanged = this.handleColorChanged.bind(this);
    }

    initializeValues(data) {
        super.initializeValues(data);

        const roomId = data.roomId || this.props.roomId;
        let notify = null;
        if (data.notify) {
            notify = data.notify;
        } else if (this.props.notify) {
            notify = this.props.notify;
        } else {
            notify = false;
        }
        const color = data.color || this.props.color;

        super.handleStateValues('roomId', roomId);
        super.handleStateValues('notify', notify);
        super.handleStateValues('color', color);

        const colorOptions = [
            { label: 'Yellow', value: 'yellow' },
            { label: 'Green', value: 'green' },
            { label: 'Red', value: 'red' },
            { label: 'Purple', value: 'purple' },
            { label: 'Gray', value: 'gray' },
            { label: 'Random', value: 'random' }
        ];
        this.setState({
            colorOptions
        });
    }

    handleColorChanged(option) {
        if (option) {
            super.handleStateValues('color', option.value);
        } else {
            super.handleStateValues('color', option);
        }
    }

    render() {
        const content = (<div>
            <TextInput id="hipChatJob-roomId" label="Room Id" name="roomId" value={this.state.values.roomId} onChange={this.handleChange} errorName="roomIdError" errorValue={this.state.errors.roomIdError} />
            <CheckboxInput id="hipChatJob-notify" label="Notify" name="notify" value={this.state.values.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.props.notifyError} />
            <div className="form-group">
                <label className="col-sm-3 control-label">Color</label>
                <div className="col-sm-8">
                    <Select
                        id="hipChatJob-color"
                        className="typeAheadField"
                        onChange={this.handleColorChanged}
                        searchable
                        options={this.state.colorOptions}
                        placeholder="Choose the message color"
                        value={this.state.values.color}
                    />
                </div>
            </div>
        </div>);

        return super.render(content);
    }
}

HipChatJobConfiguration.propTypes = {
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    csrfToken: PropTypes.string
};

HipChatJobConfiguration.defaultProps = {
    baseUrl: '/alert/api/configuration/distribution/hipchat',
    testUrl: '/alert/api/configuration/distribution/hipchat/test',
    distributionType: 'hipchat_channel'
};
