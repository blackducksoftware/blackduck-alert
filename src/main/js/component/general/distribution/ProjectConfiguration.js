import React, { Component } from 'react';

import { ReactBsTable, BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import CheckboxInput from '../../../field/input/CheckboxInput';

import tableStyles from '../../../../css/table.css';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

export default class ProjectConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedProjects: []
        };
        this.onRowSelected = this.onRowSelected.bind(this);
        this.assignDataFormat = this.assignDataFormat.bind(this);
    }

    createProjectList() {
        const { projects, configuredProjects } = this.props;
        let projectData = new Array();
        if(projects && projects.length > 0) {
            let rawProjects = projects;
            let missingProjects = new Array();
            for (var index in rawProjects) {
                let name = rawProjects[index]
                projectData.push({
                    name: rawProjects[index].name,
                    missing: false
                });
            }

            for(var index in configuredProjects) {
                let projectFound = projectData.find((project) => {
                    return project.name === configuredProjects[index];
                });

                if(!projectFound) {
                    projectData.push({
                        name: configuredProjects[index],
                        missing: true
                    });
                }
            }
        } else {
            let rawProjects = configuredProjects;
            for (var index in rawProjects) {
                projectData.push({
                    name: rawProjects[index],
                    missing: true
                });
            }
        }
        return projectData;
    }

    assignClassName(row, rowIdx) {
        return `${tableStyles.tableRow}`;
    }

    assignDataFormat(cell, row) {
        let cellContent;
        if(row.missing) {
            let fontAwesomeClass = `fa fa-exclamation-triangle fa-fw`;
            cellContent = <span className="missingHubData"><i className={fontAwesomeClass} aria-hidden='true'></i>{ row.name }</span>;
        } else {
            cellContent = row.name;
        }

        return <div title={row.name}> {cellContent} </div>;
    }

    onRowSelected(row, isSelected) {
        const { selectedProjects } = this.state;
        let selected = Object.assign([], selectedProjects);
        if(isSelected) {
            let projectFound = selected.find((project) => {
                return project.value === row.name;
            });
            if (!projectFound) {
                selected.push({value: row.name});
            }
        } else {
            let projectFound = selected.find((project) => {
                return project.value === row.name;
            });
            let index = selected.indexOf(projectFound);
            selected = selected.slice(index);
        }

        const { handleProjectChanged } = this.props;
        handleProjectChanged(selected);
    }

    render() {
        let projectData = this.createProjectList();

        const projectTableOptions = {
            noDataText: 'No projects found',
            clearSearch: true,
            defaultSortName: 'name',
            defaultSortOrder: 'asc'
        };

        const projectsSelectRowProp = {
            mode: 'checkbox',
            clickToSelect: true,
            showOnlySelected: true,
            selected: this.props.configuredProjects,
            onSelect: this.onRowSelected
        };
        var progressIndicator = null;
        if (this.props.waitingForProjects) {
            const fontAwesomeIcon = "fa fa-spinner fa-pulse fa-fw";
            progressIndicator = <div className="progressIcon">
                <i className={fontAwesomeIcon} aria-hidden='true'></i>
            </div>;
        }

        var projectTable = null;
        if (!this.props.includeAllProjects) {
            projectTable = <div>
                <BootstrapTable data={projectData} containerClass={tableStyles.table} striped condensed selectRow={projectsSelectRowProp} search={true} options={projectTableOptions} trClassName={this.assignClassName} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.projectTableScrollableBody} >
                    <TableHeaderColumn dataField='name' isKey dataSort columnClassName={tableStyles.tableCell} dataFormat={this.assignDataFormat}>Project</TableHeaderColumn>
                    <TableHeaderColumn dataField='missing' dataFormat={this.assignDataFormat} hidden>Missing Project</TableHeaderColumn>
                </BootstrapTable>
                {progressIndicator}
                <p name="projectTableMessage">{this.props.projectTableMessage}</p>
            </div>;
        }

        return (
            <div>
                <CheckboxInput label="Include all projects" name="includeAllProjects" value={this.props.includeAllProjects} onChange={this.props.handleChange} errorName="includeAllProjectsError" errorValue={this.props.includeAllProjectsError}></CheckboxInput>
                {projectTable}
            </div>
        )
    }
}
