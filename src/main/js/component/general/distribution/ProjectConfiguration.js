import React, { Component } from 'react';
import { ReactBsTable, BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import CheckboxInput from '../../../field/input/CheckboxInput';

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
        const projectData = new Array();
        if (projects && projects.length > 0) {
            const rawProjects = projects;
            const missingProjects = new Array();
            for (const index in rawProjects) {
                const name = rawProjects[index];
                projectData.push({
                    name: rawProjects[index].name,
                    missing: false
                });
            }

            for (const index in configuredProjects) {
                const projectFound = projectData.find(project => project.name === configuredProjects[index]);

                if (!projectFound) {
                    projectData.push({
                        name: configuredProjects[index],
                        missing: true
                    });
                }
            }
        } else {
            const rawProjects = configuredProjects;
            for (const index in rawProjects) {
                projectData.push({
                    name: rawProjects[index],
                    missing: true
                });
            }
        }
        return projectData;
    }

    assignClassName(row, rowIdx) {
        return `tableRow`;
    }

    assignDataFormat(cell, row) {
        let cellContent;
        if (row.missing) {
            const fontAwesomeClass = 'fa fa-exclamation-triangle fa-fw';
            cellContent = <span className="missingHubData"><span className={fontAwesomeClass} aria-hidden="true" />{ row.name }</span>;
        } else {
            cellContent = row.name;
        }

        return <div title={row.name}> {cellContent} </div>;
    }

    onRowSelected(row, isSelected) {
        const { selectedProjects } = this.state;
        let selected = Object.assign([], selectedProjects);
        if (isSelected) {
            const projectFound = selected.find(project => project.value === row.name);
            if (!projectFound) {
                selected.push({ value: row.name });
            }
        } else {
            const projectFound = selected.find(project => project.value === row.name);
            const index = selected.indexOf(projectFound);
            selected = selected.slice(index);
        }

        const { handleProjectChanged } = this.props;
        handleProjectChanged(selected);
    }

    render() {
        const projectData = this.createProjectList();

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
        let progressIndicator = null;
        if (this.props.waitingForProjects) {
            const fontAwesomeIcon = 'fa fa-spinner fa-pulse fa-fw';
            progressIndicator = (<div className="progressIcon">
                <span className={fontAwesomeIcon} aria-hidden="true" />
            </div>);
        }

        let projectTable = null;
        if (!this.props.includeAllProjects) {
            projectTable = (<div>
                <BootstrapTable data={projectData} containerClass="table" hover condensed selectRow={projectsSelectRowProp} search options={projectTableOptions} trClassName={this.assignClassName} headerContainerClass="scrollable" bodyContainerClass="projectTableScrollableBody">
                    <TableHeaderColumn dataField="name" isKey dataSort columnClassName="tableCell" dataFormat={this.assignDataFormat}>Project</TableHeaderColumn>
                    <TableHeaderColumn dataField="missing" dataFormat={this.assignDataFormat} hidden>Missing Project</TableHeaderColumn>
                </BootstrapTable>
                {progressIndicator}
                <p name="projectTableMessage">{this.props.projectTableMessage}</p>
            </div>);
        }

        return (
            <div>
                <CheckboxInput label="Include all projects" name="includeAllProjects" value={this.props.includeAllProjects} onChange={this.props.handleChange} errorName="includeAllProjectsError" errorValue={this.props.includeAllProjectsError} />
                {projectTable}
            </div>
        );
    }
}
