import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import { connect } from 'react-redux';
import PermissionTable, { PERMISSIONS_TABLE } from 'dynamic/loaded/users/PermissionTable';
import { clearRoleFieldErrors, deleteRole, fetchRoles, saveRole } from 'store/actions/roles';

class RoleTable extends Component {
    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.onRoleClose = this.onRoleClose.bind(this);
        this.savePermissions = this.savePermissions.bind(this);
        this.deletePermission = this.deletePermission.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.onCopy = this.onCopy.bind(this);

        this.state = {
            role: {
                permissions: []
            },
            incrementalId: 1,
            saveCallback: () => null
        };
    }

    componentDidUpdate(prevProps) {
        const { saveStatus } = this.props;
        const { saveCallback } = this.state;
        if (prevProps.saveStatus === 'SAVING' && (saveStatus === 'SAVED' || saveStatus === 'ERROR')) {
            this.setState({
                role: {
                    permissions: []
                }
            }, () => saveCallback(true));
        }
    }

    onEdit(selectedRow, callback) {
        this.setState({
            role: selectedRow
        }, callback);
    }

    onCopy(selectedRow, callback) {
        const copy = JSON.parse(JSON.stringify(selectedRow));
        copy.id = null;
        this.setState({
            role: copy
        }, callback);
    }

    onSave(callback) {
        const { descriptors, saveRoleAction } = this.props;
        const { role } = this.state;
        const { permissions } = role;
        const correctedPermissions = [];
        permissions.forEach((permission) => {
            const descriptorName = permission[PERMISSIONS_TABLE.DESCRIPTOR_NAME];
            const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === descriptorName);
            if (descriptor) {
                const descriptorKey = descriptor.name;
                const permissionCopy = JSON.parse(JSON.stringify(permission));
                permissionCopy[PERMISSIONS_TABLE.DESCRIPTOR_NAME] = descriptorKey;
                correctedPermissions.push(permissionCopy);
            }
        });
        role.permissions = correctedPermissions;

        this.setState({
            saveCallback: callback
        }, () => saveRoleAction(role));

        return true;
    }

    onDelete(rolesToDelete, callback) {
        const { deleteRoleAction } = this.props;
        if (rolesToDelete) {
            rolesToDelete.forEach((roleId) => {
                deleteRoleAction(roleId);
            });
        }
        callback();
        this.retrieveData();
    }

    onRoleClose(callback) {
        const { clearFieldErrors } = this.props;
        this.setState({
            role: {
                permissions: []
            }
        }, callback);
        clearFieldErrors();
    }

    handleChange(e) {
        const {
            name, value, type, checked
        } = e.target;
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
        const { getRoles } = this.props;
        getRoles();
    }

    async savePermissions(permission) {
        const { role, incrementalId } = this.state;
        const { permissions } = role;
        const permissionCopy = JSON.parse(JSON.stringify(permission));
        if (!permissionCopy.id) {
            permissionCopy.id = incrementalId;
            this.setState({
                incrementalId: incrementalId + 1
            });
            permissions.push(permissionCopy);
        } else {
            const matchingPermissionIndex = permissions.findIndex((listPermission) => listPermission.id === permission.id);
            if (matchingPermissionIndex > -1) {
                permissions[matchingPermissionIndex] = permission;
            }
        }
        role.permissions = permissions;
        this.setState({
            role
        });
        return true;
    }

    deletePermission(permissionIds) {
        const { role } = this.state;
        const { permissions } = role;
        const filteredPermissions = permissions.filter((listPermission) => !permissionIds.includes(listPermission.id));
        const newRole = { ...role };
        newRole.permissions = filteredPermissions;
        this.setState({
            role: newRole
        });
    }

    createModalFields() {
        const { role, incrementalId } = this.state;

        const { permissions } = role;
        let incrementedId = incrementalId;
        const updatedPermissions = [];
        permissions.forEach((permission) => {
            const permissionCopy = JSON.parse(JSON.stringify(permission));
            if (!permissionCopy.id) {
                permissionCopy.id = incrementedId;
                incrementedId += 1;
            }
            updatedPermissions.push(permissionCopy);
        });

        if (incrementedId !== incrementalId) {
            role.permissions = permissions;
            this.setState({
                role,
                incrementalId: incrementedId
            });
        }

        const roleNameKey = 'roleName';
        const roleNameValue = role[roleNameKey];

        const {
            canCreate, canDelete, fieldErrors, inProgress, fetching, descriptors
        } = this.props;

        return (
            <div>
                <TextInput
                    name={roleNameKey}
                    label="Role Name"
                    description="The name of the role."
                    required
                    onChange={this.handleChange}
                    value={roleNameValue}
                    errorName={roleNameKey}
                    errorValue={fieldErrors[roleNameKey]}
                />
                <PermissionTable
                    inProgress={inProgress}
                    fetching={fetching}
                    data={permissions}
                    saveRole={this.savePermissions}
                    deleteRole={this.deletePermission}
                    descriptors={descriptors}
                    canCreate={canCreate}
                    canDelete={canDelete}
                    nestedInModal
                />
            </div>
        );
    }

    render() {
        const {
            canCreate, canDelete, fieldErrors, roleError, inProgress, fetching, roles
        } = this.props;
        const fieldErrorKeys = Object.keys(fieldErrors);
        const hasErrors = fieldErrorKeys && fieldErrorKeys.length > 0;
        return (
            <div>
                <TableDisplay
                    newConfigFields={this.createModalFields}
                    modalTitle="Role"
                    onEditState={this.onEdit}
                    onConfigSave={this.onSave}
                    onConfigDelete={this.onDelete}
                    onConfigClose={this.onRoleClose}
                    onConfigCopy={this.onCopy}
                    refreshData={this.retrieveData}
                    data={roles}
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
    fetching: false,
    roles: [],
    descriptors: [],
    saveStatus: null
};

RoleTable.propTypes = {
    saveRoleAction: PropTypes.func.isRequired,
    deleteRoleAction: PropTypes.func.isRequired,
    clearFieldErrors: PropTypes.func.isRequired,
    getRoles: PropTypes.func.isRequired,
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    descriptors: PropTypes.array,
    roleError: PropTypes.string,
    fieldErrors: PropTypes.object,
    inProgress: PropTypes.bool,
    fetching: PropTypes.bool,
    saveStatus: PropTypes.string,
    roles: PropTypes.array
};

const mapStateToProps = (state) => ({
    roles: state.roles.data,
    descriptors: state.descriptors.items,
    roleError: state.roles.roleError,
    fieldErrors: state.roles.fieldErrors,
    inProgress: state.roles.inProgress,
    fetching: state.roles.fetching,
    saveStatus: state.roles.saveStatus
});

const mapDispatchToProps = (dispatch) => ({
    saveRoleAction: (role) => dispatch(saveRole(role)),
    deleteRoleAction: (roleId) => dispatch(deleteRole(roleId)),
    getRoles: () => dispatch(fetchRoles()),
    clearFieldErrors: () => dispatch(clearRoleFieldErrors())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
