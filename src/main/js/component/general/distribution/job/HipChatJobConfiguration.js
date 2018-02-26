import React from 'react';
import PropTypes from 'prop-types';

import {typeAheadField} from '../../../../../css/field.css';

import TextInput from '../../../../field/input/TextInput';
import CheckboxInput from '../../../../field/input/CheckboxInput';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

import BaseJobConfiguration from './BaseJobConfiguration';

export default class HipChatJobConfiguration extends BaseJobConfiguration {
	constructor(props) {
		super(props);
    this.handleColorChanged = this.handleColorChanged.bind(this);
	}

  initializeValues(data) {
    super.initializeValues(data);

    let roomId = data.roomId || this.props.roomId;
    let notify = null;
    if(data.notify && data.notify === "true") {
      notify = data.notify;
    } else if (this.props.notify && this.props.notify === "true") {
      notify = this.props.notify;
    } else {
      notify = "false";
    }
    let color = data.color || this.props.color;

    super.handleStateValues('roomId', roomId);
    super.handleStateValues('notify', notify);
    super.handleStateValues('color', color);

    let colorOptions = [
      { label: 'Yellow', value: 'yellow'},
      { label: 'Green', value: 'green' },
      { label: 'Red', value: 'red'},
      { label: 'Purple', value: 'purple'},
      { label: 'Gray', value: 'gray'},
      { label: 'Random', value: 'random'}
    ];
    this.setState({
      colorOptions
    });
  }

  handleColorChanged (option) {
      if(option) {
        super.handleStateValues('color', option.value);
      } else {
        super.handleStateValues('color', option);
      }
  }

	render() {
		let content = <div>
            <TextInput label="Room Id" name="roomId" value={this.state.values.roomId} onChange={this.handleChange} errorName="roomIdError" errorValue={this.props.roomIdError}></TextInput>
            <CheckboxInput label="Notify" name="notify" value={this.state.values.notify} onChange={this.handleChange} errorName="notifyError" errorValue={this.props.notifyError}></CheckboxInput>
            <div className="form-group">
                <label className="col-sm-3 control-label">Color</label>
                <div className="col-sm-8">
                    <Select className={typeAheadField}
                      onChange={this.handleColorChanged}
                        searchable={true}
                        options={this.state.colorOptions}
                        placeholder='Choose the message color'
                        value={this.state.values.color}
                      />
                </div>
              </div>
            </div>;

		return super.render(content);
	}
}

HipChatJobConfiguration.propTypes = {
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string
};

HipChatJobConfiguration.defaultProps = {
    baseUrl: '/api/configuration/distribution/hipchat',
    testUrl: '/api/configuration/distribution/hipchat/test',
    distributionType: 'hipchat_channel'
};
