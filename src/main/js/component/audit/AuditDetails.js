import React, { Component } from 'react';

import tableStyles from '../../../css/table.css';

import TextInput from '../../field/input/TextInput';
import TextArea from '../../field/input/TextArea';

class AuditDetails extends Component {
	constructor(props) {
		super(props);
		var currentEntry = props.currentEntry;
		let values = {};
		values.errorMessage = currentEntry.errorMessage;
		values.errorStackTrace = currentEntry.errorStackTrace;
		this.state = {
			message: '',
			values
		};
	}

	render(content) {
		var errorMessage = null;
		if (this.state.values.errorMessage) {
			errorMessage = <TextInput label="Error" readOnly={true} name="errorMessage" value={this.state.values.errorMessage}></TextInput>;
		}
		var errorStackTrace = null;
		if (this.state.values.errorStackTrace) {
			errorStackTrace = <TextArea label="Stack Trace" readOnly={true} name="errorStackTrace" value={this.state.values.errorStackTrace}></TextArea>;
		}
		return(
			<div className={tableStyles.expandableContainer}>
				{errorMessage}
				{errorStackTrace}
			</div>
		)
	}
};
export default AuditDetails;