'use strict';
import React from 'react';
import Field from '../field/Field';
import ConfigButtons from './ConfigButtons';
import Configuration from './Configuration';

import { alignCenter } from '../../css/main.css';

export default class GlobalConfiguration extends Configuration {
	constructor(props) {
		super(props);
	}

	render() {
		return (
				
				<div>
				<h1 className={alignCenter}>Global Configuration</h1>
				
				<h2 className={alignCenter}>Hub Configuration</h2>
				<Field label="Url" type="text" name="hubUrl" value={this.state.values.hubUrl} onChange={this.handleChange} errorName="hubUrlError" errorValue={this.state.errors.hubUrlError}></Field>
				<Field label="Username" type="text" name="hubUsername" value={this.state.values.hubUsername} onChange={this.handleChange} errorName="hubUsernameError" errorValue={this.state.errors.hubUsernameError}></Field>
				<Field label="Password" type="password" name="hubPassword" value={this.state.values.hubPassword} onChange={this.handleChange} errorName="hubPasswordError" errorValue={this.state.errors.hubPasswordError}></Field>
				<Field label="Timeout" type="number" name="hubTimeout" value={this.state.values.hubTimeout} onChange={this.handleChange} errorName="hubTimeoutError" errorValue={this.state.errors.hubTimeoutError}></Field>
				<Field label="Trust Https Certificates" type="checkbox" name="hubAlwaysTrustCertificate" value={this.state.values.hubAlwaysTrustCertificate} onChange={this.handleChange} errorName="hubAlwaysTrustCertificateError" errorValue={this.state.errors.hubAlwaysTrustCertificateError}></Field>
				
				<h2 className={alignCenter}>Proxy Configuration</h2>
				<Field label="Host Name" type="text" name="hubProxyHost" value={this.state.values.hubProxyHost} onChange={this.handleChange} errorName="hubProxyHostError" errorValue={this.state.errors.hubProxyHostError}></Field>
				<Field label="Port" type="number" name="hubProxyPort" value={this.state.values.hubProxyPort} onChange={this.handleChange} errorName="hubProxyPortError" errorValue={this.state.errors.hubProxyPortError}></Field>
				<Field label="Username" type="text" name="hubProxyUsername" value={this.state.values.hubProxyUsername} onChange={this.handleChange} errorName="hubProxyUsernameError" errorValue={this.state.errors.hubProxyUsernameError}></Field>
				<Field label="Password" type="password" name="hubProxyPassword" value={this.state.values.hubProxyPassword} onChange={this.handleChange} errorName="hubProxyPasswordError" errorValue={this.state.errors.hubProxyPasswordError}></Field>

				<h2 className={alignCenter}>Scheduling Configuration</h2>
				<Field label="Accumulator Cron" type="text" name="accumulatorCron" value={this.state.values.accumulatorCron} onChange={this.handleChange} errorName="accumulatorCronError" errorValue={this.state.errors.accumulatorCronError}></Field>
				<Field label="Daily Digest Cron" type="text" name="dailyDigestCron" value={this.state.values.dailyDigestCron} onChange={this.handleChange} errorName="dailyDigestCronError" errorValue={this.state.errors.dailyDigestCronError}></Field>
				<Field label="Purge Data Cron" type="text" name="purgeDataCron" value={this.state.values.purgeDataCron} onChange={this.handleChange} errorName="purgeDataCronError" errorValue={this.state.errors.purgeDataCronError}></Field>
				
				<ConfigButtons includeTest="true" onClick={this.handleSubmit} onTestClick={this.handleTestSubmit} />
				
				<p name="configurationMessage">{this.state.configurationMessage}</p>
			</div>
		)
	}
}
