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

    async onSave() {
        const { role } = this.state;
        await this.props.saveRole(role);
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

    async savePermissions(permission) {
        const { role, incrementalId } = this.state;
        const { permissions } = role;
        if (!permission.id) {
            permission.id = incrementalId;
            this.setState({
                incrementalId: incrementalId + 1
            });
            permissions.push(permission);
        } else {
            const matchingPermissionIndex = permissions.findIndex(listPermission => listPermission.id === permission.id);
            if (matchingPermissionIndex > -1) {
                permissions[matchingPermissionIndex] = permission;
            }
        }
        role.permissions = permissions;
        this.setState({
            role: role
        });
        return true;
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

        const { canCreate, canDelete, fieldErrors, inProgress, fetching } = this.props;

        return (
            <div>
                <TextInput name={roleNameKey} label="Role Name" description="The name of the role." required={true}
                           onChange={this.handleChange} value={roleNameValue} errorName={roleNameKey}
                           errorValue={fieldErrors[roleNameKey]} />
                <PermissionTable
                    inProgress={inProgress}
                    fetching={fetching}
                    data={permissions}
                    saveRole={this.savePermissions}
                    deleteRole={this.deletePermission}
                    descriptors={this.props.descriptors}
                    canCreate={canCreate}
                    canDelete={canDelete}
                    nestedInModal={true} />
            </div>
        );
    }

    render() {
        const { canCreate, canDelete, fieldErrors, roleError, inProgress, fetching } = this.props;
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
                    fetching={fetching}
                />
            </div>
        );
    }
}

RoleTable.defaultProps = {
    canCreate: true,
    canDelete: true,
    roleError: null,
    fieldErrors: {},
    inProgress: false,
    fetching: false
};

RoleTable.propTypes = {
    saveRole: PropTypes.func.isRequired,
    deleteRole: PropTypes.func.isRequired,
    getRoles: PropTypes.func.isRequired,
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    descriptors: PropTypes.array,
    roleError: PropTypes.string,
    fieldErrors: PropTypes.object,
    inProgress: PropTypes.bool,
    fetching: PropTypes.bool
};

const mapStateToProps = state => ({
    roles: state.roles.data,
    descriptors: state.descriptors.items,
    roleError: state.roles.roleError,
    fieldErrors: state.roles.fieldErrors,
    inProgress: state.roles.inProgress,
    fetching: state.roles.fetching
});

const mapDispatchToProps = dispatch => ({
    saveRole: role => dispatch(saveRole(role)),
    deleteRole: roleId => dispatch(deleteRole(roleId)),
    getRoles: () => dispatch(fetchRoles()),
    clearFieldErrors: () => dispatch(clearRoleFieldErrors())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
