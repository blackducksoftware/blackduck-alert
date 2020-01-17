import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import { connect } from 'react-redux';
import PermissionTable from 'dynamic/loaded/users/PermissionTable';
import { clearRoleFieldErrors, deleteRole, fetchRoles, saveRole } from 'store/actions/roles';

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
        this.savePermissions = this.savePermissions.bind(this);
        this.deletePermission = this.deletePermission.bind(this);
        this.onEdit = this.onEdit.bind(this);

        this.state = {
            role: {
                permissions: []
            },
            incrementalId: 1
        };
    }

    handleChange(e) {
        const { name, value, type, checked } = e.target;
        const { role } = this.state;
        const updatedValue = type === 'checkbox' ? checked.toString()
            .toLowerCase() === 'true' : value;
        const newRole = Object.assign(role, { [name]: updatedValue });
        this.setState({
            role: newRole
        });
    }

    createColumns() {
        return [
            {
                header: 'id',
                headerLabel: 'Id',
                isKey: true,
                hidden: true
            },
            {
                header: 'roleName',
                headerLabel: 'Name',
                isKey: false,
                hidden: false
            }
        ];
    }

    retrieveData() {
        this.props.getRoles();
    }

    onEdit(selectedRow) {
        this.setState({
            role: selectedRow
        });
    }

    onSave() {
        const { role } = this.state;
        console.log('Saving the role : ' + role);
        this.props.saveRole(role);
        this.setState({
            role: {
                permissions: []
            }
        });
        this.retrieveData();
        return true;
    }

    onDelete(rolesToDelete) {
        if (rolesToDelete) {
            rolesToDelete.forEach(roleId => {
                this.props.deleteRole(roleId);
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
        this.props.clearFieldErrors();
    }

    savePermissions(permission) {
        const { role, incrementalId } = this.state;
        const { permissions } = role;
        console.log('Saving the permission : ' + permission);
        // FIXME
        if (!permission.id) {
            console.log(`The permission has no id: ${permission.id}`);
            permission.id = incrementalId;
            this.setState({
                incrementalId: incrementalId + 1
            });
            permissions.push(permission);
        } else {
            console.log(`The permission has an id: ${permission.id}`);
            const matchingPermissionIndex = permissions.findIndex(listPermission => listPermission.id === permission.id);
            console.log(`Matching permission index: ${matchingPermissionIndex}`);
            if (matchingPermissionIndex > -1) {
                permissions[matchingPermissionIndex] = permission;
            }
        }
        role.permissions = permissions;
        this.setState({
            role: role
        });
    }

    deletePermission(permissionIds) {
        const { role } = this.state;
        const { permissions } = role;
        const filteredPermissions = permissions.filter(listPermission => !permissionIds.includes(listPermission.id));
        let newRole = { ...role };
        newRole.permissions = filteredPermissions;
        this.setState({
            role: newRole
        });
    }

    createModalFields() {
        const { role } = this.state;

        const { permissions } = role;
        let incrementedId = this.state.incrementalId;
        permissions.forEach(permission => {
            if (!permission.id) {
                permission.id = incrementedId;
                incrementedId++;
            }
        });

        if (incrementedId !== this.state.incrementalId) {
            role.permissions = permissions;
            this.setState({
                role: role,
                incrementedId: incrementedId
            });
        }

        const roleNameKey = 'roleName';
        const roleNameValue = role[roleNameKey];

        const { canCreate, canDelete, fieldErrors } = this.props;

        return (
            <div>
                <TextInput name={roleNameKey} label="Role Name" description="The name of the role." required={true}
                           onChange={this.handleChange} value={roleNameValue} errorName={roleNameKey}
                           errorValue={fieldErrors[roleNameKey]} />
                <PermissionTable
                    data={permissions}
                    saveRole={this.savePermissions}
                    deleteRole={this.deletePermission}
                    descriptors={this.props.descriptors}
                    canCreate={canCreate}
                    canDelete={canDelete} />
            </div>
        );
    }

    render() {
        const { canCreate, canDelete, fieldErrors, roleError, inProgress } = this.props;
        const fieldErrorKeys = Object.keys(fieldErrors);
        const hasErrors = fieldErrorKeys && fieldErrorKeys.length > 0;
        return (
            <div>
                <TableDisplay
                    newConfigFields={this.createModalFields}
                    modalTitle="Role"
                    editState={this.onEdit}
                    onConfigSave={this.onSave}
                    onConfigDelete={this.onDelete}
                    onConfigClose={this.onRoleClose}
                    refreshData={this.retrieveData}
                    data={this.props.roles}
                    columns={this.createColumns()}
                    newButton={canCreate}
                    deleteButton={canDelete}
                    hasFieldErrors={hasErrors}
                    errorDialogMessage={roleError}
                    inProgress={inProgress}
                />
            </div>
        );
    }
}

RoleTable.defaultProps = {
    canCreate: true,
    canDelete: true,
    roleError: null,
    fieldErrors: {}
};

RoleTable.propTypes = {
    saveRole: PropTypes.func.isRequired,
    deleteRole: PropTypes.func.isRequired,
    getRoles: PropTypes.func.isRequired,
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    descriptors: PropTypes.array,
    roleError: PropTypes.string,
    fieldErrors: PropTypes.object
};

const mapStateToProps = state => ({
    roles: state.roles.data,
    descriptors: state.descriptors.items,
    roleError: state.roles.roleError,
    fieldErrors: state.roles.fieldErrors,
    inProgress: state.roles.inProgress
});

const mapDispatchToProps = dispatch => ({
    saveRole: role => dispatch(saveRole(role)),
    deleteRole: roleId => dispatch(deleteRole(roleId)),
    getRoles: () => dispatch(fetchRoles()),
    clearFieldErrors: () => dispatch(clearRoleFieldErrors())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
