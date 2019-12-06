import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import { connect } from 'react-redux';
import { createNewRole, deleteRole, fetchRoles } from 'store/actions/roles';

class RoleTable extends Component {
    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);

        this.state = {
            role: {}
        };
    }

    handleChange(e) {
        const { name, value, type, checked } = e.target;
        const { role } = this.state;
        const updatedValue = type === 'checkbox' ? checked.toString().toLowerCase() === 'true' : value;
        const newRole = Object.assign(role, { [name]: updatedValue });
        this.setState({
            role: newRole
        });
    }

    createPermissionsColumns() {
        return [
            {
                header: 'descriptorName',
                headerLabel: 'Descriptor',
                isKey: true
            }, {
                header: 'context',
                headerLabel: 'Context',
                isKey: false
            }, {
                header: 'createOperation',
                headerLabel: 'Create',
                isKey: false
            }, {
                header: 'deleteOperation',
                headerLabel: 'Delete',
                isKey: false
            }, {
                header: 'readOperation',
                headerLabel: 'Read',
                isKey: false
            }, {
                header: 'writeOperation',
                headerLabel: 'Write',
                isKey: false
            }, {
                header: 'executeOperation',
                headerLabel: 'Execute',
                isKey: false
            }, {
                header: 'uploadFileReadOperation',
                headerLabel: 'Upload File Read',
                isKey: false
            }, {
                header: 'uploadFileWriteOperation',
                headerLabel: 'Upload File Write',
                isKey: false
            }, {
                header: 'uploadFileDeleteOperation',
                headerLabel: 'Upload File Delete',
                isKey: false
            }
        ];
    }

    createColumns() {
        return [{
            header: 'roleName',
            headerLabel: 'Name',
            isKey: true
        }];
    }

    retrievePermissionsData() {
        return [];
    }

    retrieveData() {
        this.props.getRoles();
    }

    onSave() {
        this.props.createRole(this.state.role);
        this.setState({
            role: {}
        });
    }

    onDelete() {
        this.props.deleteRole(this.state.role['roleName']);
    }

    createModalFields(selectedRow) {
        const { role } = this.state;
        let newRole = role;
        if (selectedRow) {
            newRole = Object.assign({}, role, selectedRow);
        }

        const roleNameKey = 'roleName';
        const roleNameValue = newRole[roleNameKey];

        return (
            <div>
                <TextInput name={roleNameKey} label="Role Name" description="The name of the role." onChange={this.handleChange} value={roleNameValue} />
                <TableDisplay newConfigFields={() => null} columns={this.createPermissionsColumns()} data={[]} refreshData={this.retrievePermissionsData} deleteButton={false} newButton={false} />
            </div>
        );
    }

    render() {
        const { canCreate, canDelete } = this.props;

        return (
            <div>
                <div>
                    <TableDisplay
                        newConfigFields={this.createModalFields}
                        modalTitle="Role"
                        onConfigSave={this.onSave}
                        onConfigDelete={this.onDelete}
                        refreshData={this.retrieveData}
                        data={this.props.roles}
                        columns={this.createColumns()}
                        newButton={canCreate}
                        deleteButton={canDelete} />
                </div>
            </div>
        );
    }
}

RoleTable.defaultProps = {
    canCreate: true,
    canDelete: true
};

RoleTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

const mapStateToProps = state => ({
    roles: state.roles.data
});

const mapDispatchToProps = dispatch => ({
    createRole: role => dispatch(createNewRole(role)),
    deleteRole: rolename => dispatch(deleteRole(rolename)),
    getRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
