import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import { connect } from 'react-redux';
import { createNewRole, deleteRole, fetchRoles } from 'store/actions/roles';
import DynamicSelectInput from 'field/input/DynamicSelect';
import CheckboxInput from 'field/input/CheckboxInput';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';

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
        this.handlePermissionsChange = this.handlePermissionsChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.createPermissionsModal = this.createPermissionsModal.bind(this);
        this.retrievePermissionsData = this.retrievePermissionsData.bind(this);
        this.createDescriptorOptions = this.createDescriptorOptions.bind(this);
        this.onSavePermissions = this.onSavePermissions.bind(this);
        this.onDeletePermissions = this.onDeletePermissions.bind(this);

        this.state = {
            role: {
                permissions: []
            },
            permissionsData: {}
        };
    }

    handlePermissionsChange(e) {
        const { name, value, type, checked } = e.target;
        const { permissionsData } = this.state;
        const updatedValue = type === 'checkbox' ? checked.toString().toLowerCase() === 'true' : value;
        const trimmedValue = (Array.isArray(updatedValue) && updatedValue.length > 0) ? updatedValue[0] : updatedValue;
        const newPermissions = Object.assign(permissionsData, { [name]: trimmedValue });
        this.setState({
            permissionsData: newPermissions
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
                header: 'permissionsColumn',
                headerLabel: 'Permissions',
                isKey: false
            }
        ];
    }

    retrievePermissionsData() {
        const { permissions } = this.state.role;

        if (!permissions) {
            return [];
        }

        return permissions.map(permission => {
            const permissionShorthand = [];
            permission[CREATE] && permissionShorthand.push('c');
            permission[DELETE_OPERATION] && permissionShorthand.push('d');
            permission[READ] && permissionShorthand.push('r');
            permission[WRITE] && permissionShorthand.push('w');
            permission[EXECUTE] && permissionShorthand.push('x');
            permission[UPLOAD_READ] && permissionShorthand.push('ur');
            permission[UPLOAD_WRITE] && permissionShorthand.push('uw');
            permission[UPLOAD_DELETE] && permissionShorthand.push('ud');

            return {
                descriptorName: permission.descriptorName,
                context: permission.context,
                permissionsColumn: permissionShorthand.join(', ')
            };
        });
    }

    createDescriptorOptions() {
        const { descriptors } = this.props;
        const descriptorOptions = [];
        const nameCache = [];

        descriptors.forEach(descriptor => {
            const { label, name } = descriptor;
            if (!nameCache.includes(name)) {
                nameCache.push(name);
                descriptorOptions.push({
                    label: label,
                    value: name
                });
            }
        });

        return descriptorOptions;
    }

    createContextOptions() {
        return [{
            label: CONTEXT_TYPE.DISTRIBUTION,
            value: CONTEXT_TYPE.DISTRIBUTION
        }, {
            label: CONTEXT_TYPE.GLOBAL,
            value: CONTEXT_TYPE.GLOBAL
        }]
    }

    createPermissionsModal() {
        return (
            <div>
                <DynamicSelectInput name={DESCRIPTOR_NAME} id={DESCRIPTOR_NAME} label="Descriptor Name" options={this.createDescriptorOptions()} onChange={this.handlePermissionsChange} value={this.state.permissionsData[DESCRIPTOR_NAME]} />
                <DynamicSelectInput name={CONTEXT} id={CONTEXT} label="Context" options={this.createContextOptions()} onChange={this.handlePermissionsChange} value={this.state.permissionsData[CONTEXT]} />
                <CheckboxInput name={CREATE} label="Create" description="Allow users to create new items with this permission." onChange={this.handlePermissionsChange} isChecked={this.state.permissionsData[CREATE]} />
                <CheckboxInput name={DELETE_OPERATION} label="Delete" description="Allow users to delete items with this permission." onChange={this.handlePermissionsChange} isChecked={this.state.permissionsData[DELETE_OPERATION]} />
                <CheckboxInput name={READ} label="Read" description="This permission shows or hides content for the user." onChange={this.handlePermissionsChange} isChecked={this.state.permissionsData[READ]} />
                <CheckboxInput name={WRITE} label="Write" description="Allow users to edit items with this permission." onChange={this.handlePermissionsChange} isChecked={this.state.permissionsData[WRITE]} />
                <CheckboxInput name={EXECUTE} label="Execute" description="Allow users to perform functionality with this permission." onChange={this.handlePermissionsChange} isChecked={this.state.permissionsData[EXECUTE]} />
                <CheckboxInput name={UPLOAD_READ} label="Upload Read" description="This permission shows or hides upload related content for the user." onChange={this.handlePermissionsChange}
                               isChecked={this.state.permissionsData[UPLOAD_READ]} />
                <CheckboxInput name={UPLOAD_WRITE} label="Upload Write" description="Allow users to modify uploaded content with this permission." onChange={this.handlePermissionsChange}
                               isChecked={this.state.permissionsData[UPLOAD_WRITE]} />
                <CheckboxInput name={UPLOAD_DELETE} label="Upload Delete" description="Allow users to delete uploaded content with this permission." onChange={this.handlePermissionsChange}
                               isChecked={this.state.permissionsData[UPLOAD_DELETE]} />
            </div>
        );
    }

    onSavePermissions() {
        const { role, permissionsData } = this.state;
        if (!permissionsData[DESCRIPTOR_NAME] || !permissionsData[CONTEXT]) {
            // Create error message
        } else {
            role.permissions.push(permissionsData);
            this.setState({
                permissionsData: {}
            });
        }
    }

    onDeletePermissions(permissionsToDelete) {
        if (permissionsToDelete) {
            const { permissions } = this.state.role;
            const newPermissions = permissions.filter(permission => !permissionsToDelete.includes(permission.descriptorName));
            this.setState({
                role: {
                    permissions: newPermissions
                }
            });
        }
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

    onDelete(rolesToDelete) {
        if (rolesToDelete) {
            rolesToDelete.forEach(roleName => {
                this.props.deleteRole(roleName);
            });
        }
        this.retrieveData();
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
                <TableDisplay
                    modalTitle="New Role Permissions"
                    tableNewButtonLabel="Add"
                    tableDeleteButtonLabel="Remove"
                    tableSearchable={false}
                    autoRefresh={false}
                    tableRefresh={false}
                    onConfigSave={this.onSavePermissions}
                    onConfigDelete={this.onDeletePermissions}
                    newConfigFields={this.createPermissionsModal}
                    columns={this.createPermissionsColumns()}
                    data={this.retrievePermissionsData()}
                    refreshData={() => null}
                    deleteButton={canDelete}
                    newButton={canCreate} />
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
    canDelete: PropTypes.bool,
    descriptors: PropTypes.array
};

const mapStateToProps = state => ({
    roles: state.roles.data,
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    createRole: role => dispatch(createNewRole(role)),
    deleteRole: rolename => dispatch(deleteRole(rolename)),
    getRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
