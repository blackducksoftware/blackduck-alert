import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { ReactBsTable, BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';

import CheckboxInput from '../../../field/input/CheckboxInput';
import { getProjects } from '../../../store/actions/projects';

function assignClassName(row, rowIdx) {
    return 'tableRow';
}

function assignDataFormat(cell, row) {
    const cellContent = (row.missing) ?
        <span className="missingHubData"><span className="fa fa-exclamation-triangle fa-fw" aria-hidden="true" />{ cell }</span> :
        cell;
    return <div title={cell}> {cellContent} </div>;
}

class ProjectConfiguration extends Component {
    constructor(props) {
        super(props);
        this.onRowSelected = this.onRowSelected.bind(this);
        this.onRowSelectedAll = this.onRowSelectedAll.bind(this);
    }

    componentWillMount() {
        this.props.getProjects();
    }

    createSelectedArray(selectedArray, row, isSelected) {
        if (isSelected) {
            const projectFound = selectedArray.find(project => project === row.name);
            if (!projectFound) {
                selectedArray.push(row.name);
            }
        } else {
            const index = selectedArray.indexOf(row.name);
            if (index >= 0) {
                selectedArray.splice(index, 1); // if found, remove that element from selected array
            }
        }
    }

    onRowSelectedAll(isSelected, rows) {
        if(rows) {
            const selected = Object.assign([], this.props.configuredProjects);
            rows.forEach(row => {
                this.createSelectedArray(selected, row, isSelected);
            });
            this.props.handleProjectChanged(selected);
        } else {
            this.props.handleProjectChanged([]);
        }
    }

    onRowSelected(row, isSelected) {
        const selected = Object.assign([], this.props.configuredProjects);
        this.createSelectedArray(selected, row, isSelected);
        this.props.handleProjectChanged(selected);
    }

    createProjectList() {
        const { projects, configuredProjects } = this.props;
        const projectData = projects.map(({ name, description }) => ({ name, description: description || '', missing: false }));

        configuredProjects.forEach((project) => {
            const projectFound = projectData.find(p => project === p.name);
            if (!projectFound) {
                projectData.unshift({
                    name: project,
                    missing: true
                });
            }
        });

        return projectData;
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
            onSelect: this.onRowSelected,
            onSelectAll: this.onRowSelectedAll
        };

        let projectTable = null;
        if (!this.props.includeAllProjects) {
            projectTable = (<div>
                <BootstrapTable data={projectData} containerClass="table" hover condensed selectRow={projectsSelectRowProp} search options={projectTableOptions} trClassName={assignClassName} headerContainerClass="scrollable" bodyContainerClass="projectTableScrollableBody">
                    <TableHeaderColumn dataField="name" isKey dataSort columnClassName="tableCell" dataFormat={assignDataFormat}>Project</TableHeaderColumn>
                    <TableHeaderColumn dataField="description" dataSort columnClassName="tableCell" dataFormat={assignDataFormat}>Description</TableHeaderColumn>
                    <TableHeaderColumn dataField="missing" dataFormat={assignDataFormat} hidden>Missing Project</TableHeaderColumn>
                </BootstrapTable>

                {this.props.fetching && <div className="progressIcon"><span className="fa fa-spinner fa-pulse fa-fw" aria-hidden="true" /></div>}

                {this.props.errorMsg && <p name="projectTableMessage">{this.props.errorMsg}</p> }
            </div>);
        }

        return (
            <div>
                <CheckboxInput
                    id="project-include"
                    label="Include all projects"
                    name="includeAllProjects"
                    value={this.props.includeAllProjects}
                    onChange={this.props.handleChange}
                    errorName="includeAllProjectsError"
                    errorValue={this.props.includeAllProjectsError}
                />
                {projectTable}
            </div>
        );
    }
}

ProjectConfiguration.defaultProps = {
    projects: [],
    configuredProjects: [],
    errorMsg: null,
    includeAllProjects: false
};

ProjectConfiguration.propTypes = {
    includeAllProjects: PropTypes.bool,
    configuredProjects: PropTypes.arrayOf(PropTypes.string),
    projects: PropTypes.arrayOf(PropTypes.any),
    fetching: PropTypes.bool.isRequired,
    errorMsg: PropTypes.string,
    getProjects: PropTypes.func.isRequired,
    handleChange: PropTypes.func.isRequired,
    handleProjectChanged: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    fetching: state.projects.fetching,
    projects: state.projects.items,
    errorMsg: state.projects.error.message
});

const mapDispatchToProps = dispatch => ({
    getProjects: () => dispatch(getProjects())
});

export default connect(mapStateToProps, mapDispatchToProps)(ProjectConfiguration);
