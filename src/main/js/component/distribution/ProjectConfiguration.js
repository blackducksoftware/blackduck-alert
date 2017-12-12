import React, { Component } from 'react';

import styles from '../../../css/distributionConfig.css';
import { progressIcon } from '../../../css/main.css';

import {ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton} from 'react-bootstrap-table';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

export default class ProjectConfiguration extends Component {
	constructor(props) {
		super(props);
	}

	render() {
        let projectData = this.props.projects;
        if(!projectData || projectData.length == 0) {
            let rawProjects = this.props.selectedProjects;
            for (var index in rawProjects) {
                projectData.push({
                    name: rawProjects[index]
                });
            }
        }

		const projectTableOptions = {
	  		noDataText: 'No projects found',
	  		clearSearch: true
		};

		const projectsSelectRowProp = {
	  		mode: 'checkbox',
	  		clickToSelect: true,
	  		showOnlySelected: true,
            selected: this.props.selectedProjects
		};
		var progressIndicator = null;
		if (this.props.waitingForProjects) {
        	const fontAwesomeIcon = "fa fa-spinner fa-pulse fa-fw";
			progressIndicator = <div className={progressIcon}>
									<i className={fontAwesomeIcon} aria-hidden='true'></i>
								</div>;
		}
		return (
			<div>
				<BootstrapTable data={projectData} containerClass={styles.table} striped hover condensed selectRow={projectsSelectRowProp} search={true} options={projectTableOptions} trClassName={styles.tableRow} headerContainerClass={styles.scrollable} bodyContainerClass={styles.projectTableScrollableBody} >
					<TableHeaderColumn dataField='name' isKey dataSort>Project</TableHeaderColumn>
				</BootstrapTable>
				{progressIndicator}
				<p name="projectTableMessage">{this.props.projectTableMessage}</p>
			</div>
		)
	}
}
