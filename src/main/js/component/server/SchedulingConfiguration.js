import React from 'react';
import PropTypes from 'prop-types';
import ConfigButtons from '../ConfigButtons';
import { dailyDigestOptions, purgeOptions } from "../../util/scheduling-data";
import { fieldError, labelField, accumulatorTypeAheadField} from '../../../css/field.css';

import Select from 'react-select-2';
import 'react-select-2/dist/css/react-select-2.css';

class SchedulingConfiguration extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
            inProgress: false
        };

		this.decreaseAccumulatorTime = this.decreaseAccumulatorTime.bind(this);
		this.loadSchedulingTimes = this.loadSchedulingTimes.bind(this);
		this.runAccumulator = this.runAccumulator.bind(this);

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
		const tickInterval = setInterval(() => this.decreaseAccumulatorTime(), 1000);
        this.setState({
            tickInterval
        });
	}

	cancelAutoTick() {
		clearInterval(this.state.tickInterval);
	}


	decreaseAccumulatorTime() {
		var accumulatorNextRunString = this.state.accumulatorNextRun;
		if(accumulatorNextRunString && parseInt(accumulatorNextRunString) > 0) {
			var accumulatorNextRun = parseInt(accumulatorNextRunString);
			accumulatorNextRun = accumulatorNextRun - 1;
			this.setState({
                accumulatorNextRun
			});
		} else {
			this.setState({
                accumulatorNextRun: 60
			});
			this.loadSchedulingTimes();
		}

	}


	loadSchedulingTimes() {
		this.handleSetState('configurationMessage', 'Loading...');
		this.handleSetState('inProgress', true);

		let getUrl = this.props.getUrl || this.props.baseUrl;
		if (!getUrl) {
			return;
		}
		fetch(getUrl,{
			credentials: "same-origin"
		})
		.then((response) => {
			this.setState({'inProgress': false});
			if (!response.ok) {
				return response.json().then(json => {
					this.handleSetState('configurationMessage', json.message);
				});
			} else {
				return response.json().then(jsonArray => {
					this.handleSetState('configurationMessage', '');
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
						this.setState({
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
		this.setState({'inProgress': true});

		var self = this;
		fetch('/api/configuration/global/scheduling/accumulator/run',{
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

    handleDailyDigestChanged (option) {
        if(option) {
        	this.setState({dailyDigestHourOfDay: option.value});
        } else {
            this.setState({dailyDigestHourOfDay: option});
        }
	}

	handlePurgeChanged (option) {
        if(option) {
            this.setState({purgeDataFrequencyDays: option.value});
        } else {
            this.setState({purgeDataFrequencyDays: option});
        }
	}


	render() {
		const { errorMessage } = this.props;

		return (
			<div>
				{ errorMessage && <div className="alert alert-danger">
					{ errorMessage }
				</div> }
				<form className="form-horizontal" onSubmit={this.handleSubmit}>
					<h1>Server Configuration / Scheduling</h1>

					<div className="form-group">
						<label className="col-sm-4 control-label">Collecting Hub notifications in</label>
						<div className="col-sm-6">
							<label className="control-label">{this.state.accumulatorNextRun} seconds</label>
						</div>
						<div className="col-sm-2">
							<button className="btn btn-primary" type="button" onClick={this.runAccumulator}>Run now</button>
						</div>
					</div>

                    <div className="form-group">
                        <label className="col-sm-4 control-label">Daily Digest Frequency</label>
                        <div className="col-sm-8">
                            <Select
								className={accumulatorTypeAheadField}
								onChange={this.handleDailyDigestChanged}
								searchable={true}
								options={dailyDigestOptions}
								placeholder='Choose the hour of day'
								value={this.state.dailyDigestHourOfDay} />
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-4 control-label">Daily Digest Cron Next Run</label>
                        <div className="col-sm-8">
                            <label className={labelField}>{this.state.dailyDigestNextRun}</label>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-4 control-label">Notification Purge Frequency</label>
                        <div className="col-sm-8">
                            <Select
								className={accumulatorTypeAheadField}
								onChange={this.handlePurgeChanged}
								searchable={true}
								options={purgeOptions}
								placeholder='Choose the frequency'
								value={this.state.purgeDataFrequencyDays} />
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-4 control-label">Purge Cron Next Run</label>
                        <div className="col-sm-8">
                            <label className={labelField}>{this.state.purgeDataNextRun}</label>
                        </div>
                    </div>

					<ConfigButtons isFixed={false} includeSave={true} includeTest={false} />
				</form>
			</div>
		);
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
    baseUrl: '/api/configuration/global/scheduling'
};

export default SchedulingConfiguration;
