'use strict';
import React from 'react';
import TextInput from '../../field/input/TextInput';
import ConfigButtons from '../ConfigButtons';
import Configuration from '../Configuration';

import { alignCenter } from '../../../css/main.css';

export default class SchedulingConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}

	render() {
		return (
				<div>
                    <h1 className={alignCenter}>Scheduling Configuration</h1>
					<TextInput label="Accumulator Cron" name="accumulatorCron" value={this.state.values.accumulatorCron} onChange={this.handleChange} errorName="accumulatorCronError" errorValue={this.state.errors.accumulatorCronError}></TextInput>
					<TextInput label="Daily Digest Cron" name="dailyDigestCron" value={this.state.values.dailyDigestCron} onChange={this.handleChange} errorName="dailyDigestCronError" errorValue={this.state.errors.dailyDigestCronError}></TextInput>
					<TextInput label="Purge Digest Cron" name="purgeDataCron" value={this.state.values.purgeDataCron} onChange={this.handleChange} errorName="purgeDataCronError" errorValue={this.state.errors.purgeDataCronError}></TextInput>

					<ConfigButtons includeTest="true" onClick={this.handleSubmit} onTestClick={this.handleTestSubmit} />

					<p name="configurationMessage">{this.state.configurationMessage}</p>
				</div>
		)
	}
}
