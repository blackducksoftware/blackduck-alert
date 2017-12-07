import React from 'react';

import styles from '../../../css/distributionConfig.css';

import {ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton} from 'react-bootstrap-table';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

export default class ProjectConfiguration extends React.Component {
	constructor(props) {
		super(props);
	}

	render() {
		const projectTableOptions = {
	  		noDataText: 'No projects found',
	  		clearSearch: true
		};

		const projectsSelectRowProp = {
	  		mode: 'checkbox',
	  		clickToSelect: true,
	  		showOnlySelected: true
		};

		return (
			<div>
				<BootstrapTable data={this.props.projects} containerClass={styles.table} striped hover condensed selectRow={projectsSelectRowProp} search={true} options={projectTableOptions} trClassName={styles.tableRow} headerContainerClass={styles.scrollable} bodyContainerClass={styles.projectTableScrollableBody} >
					<TableHeaderColumn dataField='name' isKey dataSort>Project</TableHeaderColumn>
					<TableHeaderColumn dataField='url' hidden>Project Url</TableHeaderColumn>
				</BootstrapTable>
				<p name="projectTableMessage">{this.props.projectTableMessage}</p>
			</div>
		)
	}
}