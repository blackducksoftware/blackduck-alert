'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import TextInput from '../../field/input/TextInput';
import LabeledField from '../../field/input/LabeledField';
import ServerConfiguration from './ServerConfiguration';

import { fieldLabel, textInput, fieldError, labelField } from '../../../css/field.css';
import { alignCenter, submitButtons } from '../../../css/main.css';

class SchedulingConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);

		this.decreaseAccumulatorTime = this.decreaseAccumulatorTime.bind(this);
		this.loadSchedulingTimes = this.loadSchedulingTimes.bind(this);
		this.runAccumulator = this.runAccumulator.bind(this);
	}

	componentWillUnmount() {
		 this.cancelAutoTick();
	}

	componentDidMount() {
		this.loadSchedulingTimes();
		this.startAutoTick();
	}

	handleSetState(name, value) {
		this.setState({
			[name]: value
		});
	}

	startAutoTick() {
		// run every second
		let tickInterval = setInterval(() => this.decreaseAccumulatorTime(), 1000);
		this.handleSetState('tickInterval', tickInterval);
	}

	cancelAutoTick() {
		clearInterval(this.state.tickInterval);
	}


	decreaseAccumulatorTime() {
		var accumulatorNextRunString = this.state.values.accumulatorNextRun;
		if(accumulatorNextRunString) {
			var accumulatorNextRun = parseInt(accumulatorNextRunString);
			if(accumulatorNextRun > 0) {
				accumulatorNextRun = accumulatorNextRun - 1;
				var values = this.state.values;
				values['accumulatorNextRun'] = accumulatorNextRun;
				this.setState({
					values
				});
			} else {
				var values = this.state.values;
				values['accumulatorNextRun'] = 60;
				this.setState({
					values
				});
				this.loadSchedulingTimes();
			}
}
	}


	loadSchedulingTimes() {
		this.handleSetState('configurationMessage', 'Loading...');
		this.handleSetState('inProgress', true);

		var self = this;
		let getUrl = this.props.getUrl || this.props.baseUrl;
		if (!getUrl) {
			return;
		}
		fetch(getUrl,{
			credentials: "same-origin"
		})
		.then(function(response) {
			self.handleSetState('inProgress', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('configurationMessage', json.message);
				});
			} else {
				return response.json().then(jsonArray => {
					self.handleSetState('configurationMessage', '');
					var json = jsonArray[0];
					if (json != null) {
						var values = {};
						for (var index in json) {
							if (json.hasOwnProperty(index)) {
								let name = index;
								let value = json[index];
								values[name] = value;
							}
						}
						self.setState({
							values
						});
					}
				});
			}
		})
		.catch(function(error) {
 		 	console.log(error); 
 		});
    }

    runAccumulator() {
		this.handleSetState('inProgress', true);

		var self = this;
		fetch('/configuration/global/accumulator/run',{
			credentials: "same-origin",
			method: 'POST',
		})
		.then(function(response) {
			self.handleSetState('inProgress', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('accumulatorError', json.message);
				});
			} else {
				self.handleSetState('accumulatorError', null);
			}
		})
		.catch(function(error) {
 		 	console.log(error); 
 		});
    }


	render() {
		let accumulatorErrorDiv = null;
		if (this.state.accumulatorError) {
			accumulatorErrorDiv = <p className={fieldError} name="accumulatorError">{this.state.accumulatorError}</p>;
		}
		let content =
				<div>
					<div>
						<label className={fieldLabel}>Collecting Hub notifications in </label>
						<label className={labelField}>{this.state.values.accumulatorNextRun} seconds</label>
						<input className={submitButtons} type="button" value="Run Now" onClick={this.runAccumulator}></input>
						{accumulatorErrorDiv}
					</div>
					<TextInput label="Daily Digest Cron" name="dailyDigestCron" value={this.state.values.dailyDigestCron} onChange={this.handleChange} errorName="dailyDigestCronError" errorValue={this.state.errors.dailyDigestCronError}></TextInput>
					<div>
						<label className={fieldLabel}>Daily Digest Next Run</label>
						<label className={labelField}>{this.state.values.dailyDigestNextRun}</label>
					</div>
					<TextInput label="Purge Digest Cron" name="purgeDataCron" value={this.state.values.purgeDataCron} onChange={this.handleChange} errorName="purgeDataCronError" errorValue={this.state.errors.purgeDataCronError}></TextInput>
					<div>
						<label className={fieldLabel}>Purge Digest Next Run</label>
						<label className={labelField}>{this.state.values.purgeDataNextRun}</label>
					</div>
				</div>;

		return super.render(content);
	}
};

SchedulingConfiguration.propTypes = {
    headerText: PropTypes.string,
    configButtonTest: PropTypes.bool,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string
};

SchedulingConfiguration.defaultProps = {
    headerText: 'Scheduling',
    configButtonTest: false,
    baseUrl: '/configuration/global/scheduling'
};

export default SchedulingConfiguration;
