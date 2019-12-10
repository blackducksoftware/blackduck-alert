import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import { connect } from 'react-redux';
import { createNewRole, deleteRole, fetchRoles, updateRole } from 'store/actions/roles';
import PermissionTable from "./PermissionTable";

const DESCRIPTOR_NAME = "descriptorName";
const CONTEXT = "context";
const CREATE = "create";
const DELETE_OPERATION = "delete";
const READ = "read";
const WRITE = "write";
const EXECUTE = "execute";
const UPLOAD_READ = "uploadRead";
const UPLOAD_WRITE = "uploadWrite";
const UPLOAD_DELETE = "uploadDelete";

class RoleTable extends Component {
    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.onRoleClose = this.onRoleClose.bind(this);
        this.onUpdate = this.onUpdate.bind(this);
        this.updatePermissions = this.updatePermissions.bind(this);

        this.state = {
            role: {
                permissions: []
            }
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

    createColumns() {
        return [{
            header: 'roleName',
            headerLabel: 'Name',
            isKey: true
        }];
    }

    retrieveData() {
        this.props.getRoles();
    }

    onSave() {
        const { role } = this.state;
        this.props.createRole(role);
        this.setState({
            role: {
                permissions: []
            }
        });
        this.retrieveData();
    }

    onUpdate() {
        const { role } = this.state;
        this.props.updateRole(role);
        this.setState({
            role: {
                permissions: []
            }
        });
        this.retrieveData();
    }

    onDelete(rolesToDelete) {
        if (rolesToDelete) {
            rolesToDelete.forEach(roleName => {
                this.props.deleteRole(roleName);
            });
        }
        this.retrieveData();
    }

    onRoleClose() {
        this.setState({
            role: {
                permissions: []
            }
        });
    }

    updatePermissions(permission) {
        const { role } = this.state;
        const { permissions } = role;
        permissions.push(permission);
        role.permissions = permissions;
        this.setState({
            role
        });
    }

    createModalFields(selectedRow) {
        const { role } = this.state;
        let newRole = role;
        if (selectedRow) {
            newRole = Object.assign({}, role, selectedRow);
        }

        const roleNameKey = 'roleName';
        const roleNameValue = newRole[roleNameKey];

        const { canCreate, canDelete } = this.props;

        return (
            <div>
                <TextInput name={roleNameKey} label="Role Name" description="The name of the role." required={true} onChange={this.handleChange} value={roleNameValue} />
                <PermissionTable data={newRole.permissions} updateRole={this.updatePermissions} descriptors={this.props.descriptors} canCreate={canCreate} canDelete={canDelete} />
            </div>
        );
    }

    render() {
        const { canCreate, canDelete } = this.props;

        return (
            <div>
                <TableDisplay
                    newConfigFields={this.createModalFields}
                    modalTitle="Role"
                    onConfigSave={this.onSave}
                    onConfigUpdate={this.onUpdate}
                    onConfigDelete={this.onDelete}
                    onConfigClose={this.onRoleClose}
                    refreshData={this.retrieveData}
                    data={this.props.roles}
                    columns={this.createColumns()}
                    newButton={canCreate}
                    deleteButton={canDelete} />
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
    canDelete: PropTypes.bool,
    descriptors: PropTypes.array
};

const mapStateToProps = state => ({
    roles: state.roles.data,
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    createRole: role => dispatch(createNewRole(role)),
    updateRole: role => dispatch(updateRole(role)),
    deleteRole: rolename => dispatch(deleteRole(rolename)),
    getRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
