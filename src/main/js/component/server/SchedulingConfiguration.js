'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import TextInput from '../../field/input/TextInput';
import LabeledField from '../../field/LabeledField';
import ServerConfiguration from './ServerConfiguration';

import { accumulatorFieldLabel, textInput, fieldError, labelField, accumulatorTypeAheadField} from '../../../css/field.css';
import { alignCenter, submitButtons } from '../../../css/main.css';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

class SchedulingConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);
		this.state = {
            dailyDigestOptions: [
				{ label: '12 am', value: '0'},
				{ label: '1 am', value: '1'},
				{ label: '2 am', value: '2'},
				{ label: '3 am', value: '3'},
				{ label: '4 am', value: '4'},
				{ label: '5 am', value: '5'},
				{ label: '6 am', value: '6'},
				{ label: '7 am', value: '7'},
				{ label: '8 am', value: '8'},
				{ label: '9 am', value: '9'},
				{ label: '10 am', value: '10'},
				{ label: '11 am', value: '11'},
				{ label: '12 pm', value: '12'},
				{ label: '1 pm', value: '13'},
				{ label: '2 pm', value: '14'},
				{ label: '3 pm', value: '15'},
				{ label: '4 pm', value: '16'},
				{ label: '5 pm', value: '17'},
				{ label: '6 pm', value: '18'},
				{ label: '7 pm', value: '19'},
				{ label: '8 pm', value: '20'},
				{ label: '9 pm', value: '21'},
				{ label: '10 pm', value: '22'},
				{ label: '11 pm', value: '23'}
			],
			purgeOptions: [
				{ label: 'Every day', value: '1'},
				{ label: 'Every 2 days', value: '2' },
				{ label: 'Every 3 days', value: '3'},
				{ label: 'Every 4 days', value: '4'},
				{ label: 'Every 5 days', value: '5'},
				{ label: 'Every 6 days', value: '6'},
				{ label: 'Every 7 days', value: '7'}
			]
        }

		this.decreaseAccumulatorTime = this.decreaseAccumulatorTime.bind(this);
		this.loadSchedulingTimes = this.loadSchedulingTimes.bind(this);
		this.runAccumulator = this.runAccumulator.bind(this);

		this.handleStateValues = this.handleStateValues.bind(this);
		this.handleDailyDigestChanged = this.handleDailyDigestChanged.bind(this);
		this.handlePurgeChanged = this.handlePurgeChanged.bind(this);
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
		if(accumulatorNextRunString && parseInt(accumulatorNextRunString) > 0) {
			var accumulatorNextRun = parseInt(accumulatorNextRunString);
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
		fetch('/configuration/global/scheduling/accumulator/run',{
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

    handleStateValues(name, value) {
		var values = this.state.values;
		values[name] = value;
		this.setState({
			values
		});
	}

    handleDailyDigestChanged (option) {
        if(option) {
	        this.handleStateValues('dailyDigestHourOfDay', option.value);
        } else {
            this.handleStateValues('dailyDigestHourOfDay', option);
        }
	}

	handlePurgeChanged (option) {
        if(option) {
	        this.handleStateValues('purgeDataFrequencyDays', option.value);
        } else {
            this.handleStateValues('purgeDataFrequencyDays', option);
        }
	}


	render() {
		let accumulatorErrorDiv = null;
		if (this.state.accumulatorError) {
			accumulatorErrorDiv = <p className={fieldError} name="accumulatorError">{this.state.accumulatorError}</p>;
		}
		let content =
				<div>
					<div>
						<label className={accumulatorFieldLabel}>Collecting Hub notifications in </label>
						<label className={labelField}>{this.state.values.accumulatorNextRun} seconds</label>
						<input className={submitButtons} type="button" value="Run Now" onClick={this.runAccumulator}></input>
						{accumulatorErrorDiv}
					</div>
					<div>
						<label className={accumulatorFieldLabel}>Daily Digest Frequency</label>
						<Select className={accumulatorTypeAheadField}
							onChange={this.handleDailyDigestChanged}
                            searchable={true}
						    options={this.state.dailyDigestOptions}
						    placeholder='Choose the hour of day'
						    value={this.state.values.dailyDigestHourOfDay}
						  />
					</div>
					<div>
						<label className={accumulatorFieldLabel}>Daily Digest Cron Next Run</label>
						<label className={labelField}>{this.state.values.dailyDigestNextRun}</label>
					</div>
					<div>
						<label className={accumulatorFieldLabel}>Notification Purge Frequency</label>
						<Select className={accumulatorTypeAheadField}
							onChange={this.handlePurgeChanged}
                            searchable={true}
						    options={this.state.purgeOptions}
						    placeholder='Choose the frequency'
						    value={this.state.values.purgeDataFrequencyDays}
						  />
					</div>
					<div>
						<label className={accumulatorFieldLabel}>Purge Cron Next Run</label>
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
