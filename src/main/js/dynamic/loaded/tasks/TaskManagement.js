import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import TableDisplay from "../../../field/TableDisplay";
import { fetchTasks } from "../../../store/actions/tasks";
import ReadOnlyField from "../../../field/ReadOnlyField";

class TaskManagement extends Component {
    constructor(props) {
        super(props);
        this.retrieveData = this.retrieveData.bind(this);
        this.clearModalFieldState = this.clearModalFieldState.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.state = {
            task: {}
        };
    }

    createColumns() {
        return [
            {
                header: 'name',
                headerLabel: 'Name',
                isKey: true,
                hidden: false
            },
            {
                header: 'nextRunTime',
                headerLabel: 'Next Run Time',
                isKey: false,
                hidden: false
            }

        ];
    }

    clearModalFieldState() {
        if (this.state.task && Object.keys(this.state.task).length > 0) {
            this.setState({
                task: {}
            });
        }
    }

    createModalFields() {
        const { task } = this.state;
        const nameKey = 'name';
        const nextRunTimeKey = 'nextRunTime';
        return (
            <div>
                <ReadOnlyField label="Name" name="name" readOnly="true" value={task[nameKey]} />
                <ReadOnlyField label="Next Run Time" name="nextRunTime" readOnly="true" value={task[nextRunTimeKey]} />
            </div>
        );
    }

    onEdit(selectedRow, callback) {
        this.setState({
            task: selectedRow
        }, callback);
    }

    retrieveData() {
        this.props.getTasks();
    }

    render() {
        const { label, description, tasks, fetching } = this.props;
        return (
            <div>
                <ConfigurationLabel
                    configurationName={label}
                    description={description} />
                <TableDisplay
                    newConfigFields={this.createModalFields}
                    modalTitle="Task"
                    clearModalFieldState={this.clearModalFieldState}
                    refreshData={this.retrieveData}
                    data={tasks}
                    columns={this.createColumns()}
                    onEditState={this.onEdit}
                    newButton={false}
                    deleteButton={false}
                    saveButton={false}
                    hasFieldErrors={false}
                    fetching={fetching}
                    enableEdit={false}
                    enableCopy={false}
                />
            </div>
        );
    }
}

TaskManagement.propTypes = {
    tasks: PropTypes.array.isRequired,
    description: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    fetching: PropTypes.bool
};

TaskManagement.defaultProps = {
    fetching: false
}

const mapStateToProps = state => ({
    tasks: state.tasks.data
});

const mapDispatchToProps = dispatch => ({
    getTasks: () => dispatch(fetchTasks())
});

export default connect(mapStateToProps, mapDispatchToProps)(TaskManagement);
