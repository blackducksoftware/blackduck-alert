import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { BootstrapTable, ReactBsTable, TableHeaderColumn } from 'react-bootstrap-table';

import CheckboxInput from 'field/input/CheckboxInput';
import { getProjects } from 'store/actions/projects';
import TextInput from 'field/input/TextInput';

import * as FieldModelUtilities from 'util/fieldModelUtilities';

function assignClassName(row, rowIdx) {
    return 'tableRow';
}

function assignDataFormat(cell, row) {
    const cellContent = (row.missing) ? <span className="missingBlackDuckData"><span className="fa fa-exclamation-triangle fa-fw" aria-hidden="true" />{cell}</span> : cell;

    if (cell) {
        return <div title={cell}> {cellContent} </div>;
    }
    return <div> {cellContent} </div>;
}

const KEY_FILTER_BY_PROJECT = 'channel.common.filter.by.project';
const KEY_PROJECT_NAME_PATTERN = 'channel.common.project.name.pattern';
const KEY_CONFIGURED_PROJECT = 'channel.common.configured.project';

const fieldDescriptions = {
    [KEY_PROJECT_NAME_PATTERN]: 'The regular expression to use to determine what Projects to include. These are in addition to the Projects selected in the table.',
    [KEY_FILTER_BY_PROJECT]: 'If true, all projects will be included. Any notifications matching the configured notification types will be processed.'
};

const fieldNames = [
    KEY_FILTER_BY_PROJECT,
    KEY_PROJECT_NAME_PATTERN,
    KEY_CONFIGURED_PROJECT
];


class ProjectConfiguration extends Component {
    constructor(props) {
        super(props);
        this.onRowSelected = this.onRowSelected.bind(this);
        this.onRowSelectedAll = this.onRowSelectedAll.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.state = {
            projectData: this.createProjectList(),
            includeAllProjects: this.props.includeAllProjects,
            configuredProjects: this.props.configuredProjects
        };
    }

    componentWillMount() {
        this.props.getProjects(this.props.providerName);
    }

    componentDidUpdate(prevProps) {
        if (this.props.providerName !== prevProps.providerName) {
            this.props.getProjects(this.props.providerName);
        }

        if (!this.props.fetching && prevProps.fetching) {
            this.setState({
                projectData: this.createProjectList()
            });
        }

        if (this.props.includeAllProjects !== prevProps.includeAllProjects) {
            this.setState({
                includeAllProjects: this.props.includeAllProjects
            });
        }

        if (this.props.configuredProjects !== prevProps.configuredProjects) {
            this.setState({
                configuredProjects: this.props.configuredProjects
            });
        }
    }

    onRowSelectedAll(isSelected, rows) {
        if (rows) {
            const selected = Object.assign([], this.state.configuredProjects);
            rows.forEach((row) => {
                this.createSelectedArray(selected, row, isSelected);
            });
            this.setState({
                configuredProjects: selected
            });
            this.props.handleProjectChanged(selected.map(project => Object.assign({}, {
                label: project,
                value: project
            })));
        } else {
            this.setState({
                configuredProjects: []
            });
            this.props.handleProjectChanged([]);
        }
    }

    onRowSelected(row, isSelected) {
        const selected = Object.assign([], this.state.configuredProjects);
        this.createSelectedArray(selected, row, isSelected);
        this.setState({
            configuredProjects: selected
        });
        this.props.handleProjectChanged(selected.map(project => Object.assign({}, {
            label: project,
            value: project
        })));
    }

    handleChange(event) {
        const { target } = event;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({ includeAllProjects: value });
        this.props.handleChange(event);
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

    createProjectList() {
        const { projects, configuredProjects } = this.props;
        const projectData = projects.map(({ name, description }) => ({
            name,
            description: description || '',
            missing: false
        }));

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
            selected: this.state.configuredProjects,
            onSelect: this.onRowSelected,
            onSelectAll: this.onRowSelectedAll
        };

        let projectSelectionDiv = null;
        if (!this.state.includeAllProjects) {
            projectSelectionDiv = (<div>
                <TextInput
                    id={KEY_PROJECT_NAME_PATTERN}
                    label="Project Name Pattern"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PROJECT_NAME_PATTERN)}
                    name={KEY_PROJECT_NAME_PATTERN}
                    value={this.props.projectNamePattern}
                    onChange={this.props.handleChange}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_PROJECT_NAME_PATTERN)}
                    errorValue={this.props.fieldErrors[KEY_PROJECT_NAME_PATTERN]}
                />
                <BootstrapTable
                    version="4"
                    data={this.state.projectData}
                    containerClass="table"
                    hover
                    condensed
                    selectRow={projectsSelectRowProp}
                    search
                    options={projectTableOptions}
                    trClassName={assignClassName}
                    headerContainerClass="scrollable"
                    bodyContainerClass="projectTableScrollableBody"
                >
                    <TableHeaderColumn dataField="name" isKey dataSort columnClassName="tableCell" dataFormat={assignDataFormat}>Project</TableHeaderColumn>
                    <TableHeaderColumn dataField="description" dataSort columnClassName="tableCell" dataFormat={assignDataFormat}>Description</TableHeaderColumn>
                    <TableHeaderColumn dataField="missing" dataFormat={assignDataFormat} hidden>Missing Project</TableHeaderColumn>
                </BootstrapTable>

                {this.props.fetching &&
                <div className="progressIcon"><span className="fa fa-spinner fa-pulse fa-fw" aria-hidden="true" />
                </div>}

                {this.props.errorMsg && <p name="projectTableMessage">{this.props.errorMsg}</p>}
            </div>);
        }
        return (
            <div>
                <CheckboxInput
                    id={KEY_FILTER_BY_PROJECT}
                    label="Include all projects"
                    description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_FILTER_BY_PROJECT)}
                    name={KEY_FILTER_BY_PROJECT}
                    isChecked={this.state.includeAllProjects}
                    onChange={this.handleChange}
                    negateValue
                    errorName={FieldModelUtilities.createFieldModelErrorKey(KEY_FILTER_BY_PROJECT)}
                    errorValue={this.props.fieldErrors[KEY_FILTER_BY_PROJECT]}
                />
                {this.props.fieldErrors[KEY_CONFIGURED_PROJECT] && <label
                    className="fieldError"
                    name="projectTableErrors"
                >{this.props.fieldErrors[KEY_CONFIGURED_PROJECT]}
                </label>}
                {projectSelectionDiv}
            </div>
        );
    }
}

ProjectConfiguration.defaultProps = {
    projects: [],
    configuredProjects: [],
    projectNamePattern: '',
    errorMsg: null,
    fieldErrors: {},
    includeAllProjects: true,
    includeAllProjectsError: ''
};

ProjectConfiguration.propTypes = {
    providerName: PropTypes.string.isRequired,
    includeAllProjects: PropTypes.bool.isRequired,
    configuredProjects: PropTypes.arrayOf(PropTypes.string),
    projectNamePattern: PropTypes.string,
    projects: PropTypes.arrayOf(PropTypes.any),
    fetching: PropTypes.bool.isRequired,
    errorMsg: PropTypes.string,
    fieldErrors: PropTypes.object,
    getProjects: PropTypes.func.isRequired,
    handleChange: PropTypes.func.isRequired,
    handleProjectChanged: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    fetching: state.projects.fetching,
    projects: state.projects.items,
    errorMsg: state.projects.error.message,
    error: state.distributions.error
});

const mapDispatchToProps = dispatch => ({
    getProjects: providerName => dispatch(getProjects(providerName))
});

export default connect(mapStateToProps, mapDispatchToProps)(ProjectConfiguration);
