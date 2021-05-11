import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigurationLabel from 'common/ConfigurationLabel';
import TableDisplay from 'common/table/TableDisplay';
import ReadOnlyField from 'common/input/field/ReadOnlyField';
import { fetchTasks } from 'store/actions/tasks';
import { TASK_MANAGEMENT_INFO } from 'page/task/TaskManagementModel';

class TaskManagement extends Component {
    constructor(props) {
        super(props);
        this.retrieveData = this.retrieveData.bind(this);
        this.clearModalFieldState = this.clearModalFieldState.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.createPropertyFields = this.createPropertyFields.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.state = {
            task: {}
        };
    }

    onEdit(selectedRow, callback) {
        this.setState({
            task: selectedRow
        }, callback);
    }

    createColumns() {
        return [
            {
                header: 'taskName',
                headerLabel: 'Task Name',
                isKey: true,
                hidden: true
            },
            {
                header: 'type',
                headerLabel: 'Type',
                isKey: false,
                hidden: false
            },
            {
                header: 'nextRunTime',
                headerLabel: 'Next Run Time',
                isKey: false,
                hidden: false
            },
            {
                header: 'searchableProperties',
                headerLabel: 'Properties',
                isKey: false,
                hidden: true
            }
        ];
    }

    clearModalFieldState() {
        const { task } = this.state;
        if (task && Object.keys(task).length > 0) {
            this.setState({
                task: {}
            });
        }
    }

    createPropertyFields() {
        const { task } = this.state;
        const { properties } = task;
        if (!properties) {
            return null;
        }
        const propertyFields = [];
        properties.forEach((property) => {
            const field = (
                <ReadOnlyField
                    id={property.key}
                    label={property.displayName}
                    name={property.key}
                    readOnly="true"
                    value={property.value}
                />
            );
            propertyFields.push(field);
        });

        return propertyFields;
    }

    createModalFields() {
        const { task } = this.state;
        const nameKey = 'type';
        const fullyQualifiedNameKey = 'fullyQualifiedType';
        const nextRunTimeKey = 'nextRunTime';
        const propertyFields = this.createPropertyFields();
        return (
            <div>
                <ReadOnlyField
                    id={nameKey}
                    label="Type"
                    name={nameKey}
                    readOnly="true"
                    value={task[nameKey]}
                />
                <ReadOnlyField
                    id={fullyQualifiedNameKey}
                    label="Full Type Name"
                    name={fullyQualifiedNameKey}
                    readOnly="true"
                    value={task[fullyQualifiedNameKey]}
                />
                <ReadOnlyField
                    id={nextRunTimeKey}
                    label="Next Run Time"
                    name={nextRunTimeKey}
                    readOnly="true"
                    value={task[nextRunTimeKey]}
                />
                {propertyFields}
            </div>
        );
    }

    retrieveData() {
        const { getTasks } = this.props;
        getTasks();
    }

    createTaskData() {
        const { tasks } = this.props;
        return tasks.map((task) => {
            const searchableProperties = JSON.stringify(task.properties);
            return {
                ...task,
                searchableProperties
            };
        });
    }

    render() {
        const {
            fetching, errorMessage, autoRefresh
        } = this.props;
        return (
            <div>
                <ConfigurationLabel
                    configurationName={TASK_MANAGEMENT_INFO.label}
                    description="This page allows you to view the tasks running internally within Alert."
                />
                <TableDisplay
                    id="task-management"
                    autoRefresh={autoRefresh}
                    newConfigFields={this.createModalFields}
                    modalTitle="Task Details"
                    clearModalFieldState={this.clearModalFieldState}
                    refreshData={this.retrieveData}
                    data={this.createTaskData()}
                    columns={this.createColumns()}
                    onEditState={this.onEdit}
                    newButton={false}
                    deleteButton={false}
                    saveButton={false}
                    errorDialogMessage={errorMessage}
                    hasFieldErrors={false}
                    fetching={fetching}
                    inProgress={fetching}
                    enableEdit
                    editColumnText="View"
                    editColumnIcon="folder-open"
                    enableCopy={false}
                />
            </div>
        );
    }
}

TaskManagement.propTypes = {
    tasks: PropTypes.array.isRequired,
    errorMessage: PropTypes.string,
    fetching: PropTypes.bool,
    getTasks: PropTypes.func.isRequired,
    autoRefresh: PropTypes.bool
};

TaskManagement.defaultProps = {
    fetching: false,
    errorMessage: null
};

const mapStateToProps = (state) => ({
    tasks: state.tasks.data,
    errorMessage: state.tasks.error.message,
    fetching: state.tasks.fetching,
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = (dispatch) => ({
    getTasks: () => dispatch(fetchTasks())
});

export default connect(mapStateToProps, mapDispatchToProps)(TaskManagement);
