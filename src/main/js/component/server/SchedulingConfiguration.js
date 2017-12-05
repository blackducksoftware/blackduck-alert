'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import TextInput from '../../field/input/TextInput';
import ServerConfiguration from './ServerConfiguration';
import { alignCenter } from '../../../css/main.css';

class SchedulingConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
		let content =
				<div>
					<TextInput label="Accumulator Cron" name="accumulatorCron" value={this.state.values.accumulatorCron} onChange={this.handleChange} errorName="accumulatorCronError" errorValue={this.state.errors.accumulatorCronError}></TextInput>
					<TextInput label="Daily Digest Cron" name="dailyDigestCron" value={this.state.values.dailyDigestCron} onChange={this.handleChange} errorName="dailyDigestCronError" errorValue={this.state.errors.dailyDigestCronError}></TextInput>
					<TextInput label="Purge Digest Cron" name="purgeDataCron" value={this.state.values.purgeDataCron} onChange={this.handleChange} errorName="purgeDataCronError" errorValue={this.state.errors.purgeDataCronError}></TextInput>
				</div>;

		return super.render(content);
	}
};

SchedulingConfiguration.propTypes = {
    headerText: PropTypes.string,
    externaconfigButtonTest: PropTypes.string
};

SchedulingConfiguration.defaultProps = {
    headerText: 'Scheduling Configuration',
    configButtonTest: 'true'
};

export default SchedulingConfiguration;
