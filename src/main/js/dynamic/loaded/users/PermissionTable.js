import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import DynamicSelectInput from 'field/input/DynamicSelect';
import CheckboxInput from 'field/input/CheckboxInput';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';

export const PERMISSIONS_TABLE = {
    DESCRIPTOR_NAME: 'descriptorName',
    CONTEXT: 'context',
    CREATE: 'create',
    DELETE_OPERATION: 'delete',
    READ: 'read',
    WRITE: 'write',
    EXECUTE: 'execute',
    UPLOAD_READ: 'uploadRead',
    UPLOAD_WRITE: 'uploadWrite',
    UPLOAD_DELETE: 'uploadDelete'
};


class PermissionTable extends Component {
    constructor(props) {
        super(props);

        this.handlePermissionsChange = this.handlePermissionsChange.bind(this);
        this.createPermissionsModal = this.createPermissionsModal.bind(this);
        this.retrievePermissionsData = this.retrievePermissionsData.bind(this);
        this.createDescriptorOptions = this.createDescriptorOptions.bind(this);
        this.onSavePermissions = this.onSavePermissions.bind(this);
        this.onDeletePermissions = this.onDeletePermissions.bind(this);
        this.onPermissionsClose = this.onPermissionsClose.bind(this);
        this.onEdit = this.onEdit.bind(this);

        this.state = {
            permissionsData: {},
            errorMessage: null,
            saveInProgress: false
        };
    }

    handlePermissionsChange(e) {
        const { name, value, type, checked } = e.target;
        const { permissionsData } = this.state;
        const updatedValue = type === 'checkbox' ? checked.toString()
        .toLowerCase() === 'true' : value;
        const trimmedValue = (Array.isArray(updatedValue) && updatedValue.length > 0) ? updatedValue[0] : updatedValue;
        const newPermissions = Object.assign(permissionsData, { [name]: trimmedValue });
        this.setState({
            permissionsData: newPermissions
        });
    }

    createPermissionsColumns() {
        return [
            {
                header: 'id',
                headerLabel: 'ID',
                isKey: true,
                hidden: true
            }, {
                header: PERMISSIONS_TABLE.DESCRIPTOR_NAME,
                headerLabel: 'Descriptor',
                isKey: false,
                hidden: false
            }, {
                header: PERMISSIONS_TABLE.CONTEXT,
                headerLabel: 'Context',
                isKey: false,
                hidden: false
            }, {
                header: 'permissionsColumn',
                headerLabel: 'Permissions',
                isKey: false,
                hidden: false
            }
        ];
    }

    retrievePermissionsData() {
        const { data } = this.props;
        if (!data) {
            return [];
        }

        const descriptorOptions = this.createDescriptorOptions();

        return data.map(permission => {
            const permissionShorthand = [];
            permission[PERMISSIONS_TABLE.CREATE] && permissionShorthand.push('c');
            permission[PERMISSIONS_TABLE.DELETE_OPERATION] && permissionShorthand.push('d');
            permission[PERMISSIONS_TABLE.READ] && permissionShorthand.push('r');
            permission[PERMISSIONS_TABLE.WRITE] && permissionShorthand.push('w');
            permission[PERMISSIONS_TABLE.EXECUTE] && permissionShorthand.push('x');
            permission[PERMISSIONS_TABLE.UPLOAD_READ] && permissionShorthand.push('ur');
            permission[PERMISSIONS_TABLE.UPLOAD_WRITE] && permissionShorthand.push('uw');
            permission[PERMISSIONS_TABLE.UPLOAD_DELETE] && permissionShorthand.push('ud');

            const descriptorName = permission[PERMISSIONS_TABLE.DESCRIPTOR_NAME];
            const prettyNameObject = descriptorOptions.find(option => descriptorName === option.value);
            const prettyName = (prettyNameObject) ? prettyNameObject.label : descriptorName;

            return {
                id: permission.id,
                [PERMISSIONS_TABLE.DESCRIPTOR_NAME]: prettyName,
                [PERMISSIONS_TABLE.CONTEXT]: permission[PERMISSIONS_TABLE.CONTEXT],
                permissionsColumn: permissionShorthand.join('-')
            };
        });
    }

    convertPermissionsColumn(permissions) {
        const { permissionsColumn, descriptorName, context, id } = permissions;
        const splitPermissions = permissionsColumn.split('-');

        const prettyNameObject = this.createDescriptorOptions()
        .find(option => descriptorName === option.label);
        const prettyName = (prettyNameObject) ? prettyNameObject.value : descriptorName;

        return {
            id,
            descriptorName: prettyName,
            context,
            [PERMISSIONS_TABLE.CREATE]: splitPermissions.includes('c'),
            [PERMISSIONS_TABLE.DELETE_OPERATION]: splitPermissions.includes('d'),
            [PERMISSIONS_TABLE.READ]: splitPermissions.includes('r'),
            [PERMISSIONS_TABLE.WRITE]: splitPermissions.includes('w'),
            [PERMISSIONS_TABLE.EXECUTE]: splitPermissions.includes('x'),
            [PERMISSIONS_TABLE.UPLOAD_READ]: splitPermissions.includes('ur'),
            [PERMISSIONS_TABLE.UPLOAD_WRITE]: splitPermissions.includes('uw'),
            [PERMISSIONS_TABLE.UPLOAD_DELETE]: splitPermissions.includes('ud')
        };
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
        }];
    }

    onPermissionsClose() {
        this.setState({
            permissionsData: {}
        });
    }

    onEdit(selectedRow) {
        const parsedPermissions = this.convertPermissionsColumn(selectedRow);
        this.setState({
            permissionsData: parsedPermissions
        });
    }

    createPermissionsModal() {
        const { permissionsData } = this.state;

        return (
            <div>

                <DynamicSelectInput
                    name={PERMISSIONS_TABLE.DESCRIPTOR_NAME} id={PERMISSIONS_TABLE.DESCRIPTOR_NAME}
                    label="Descriptor Name" options={this.createDescriptorOptions()} clearable={false}
                    onChange={this.handlePermissionsChange}
                    value={permissionsData[PERMISSIONS_TABLE.DESCRIPTOR_NAME]} />
                <DynamicSelectInput
                    name={PERMISSIONS_TABLE.CONTEXT} id={PERMISSIONS_TABLE.CONTEXT} label="Context"
                    options={this.createContextOptions()} clearable={false}
                    onChange={this.handlePermissionsChange}
                    value={permissionsData[PERMISSIONS_TABLE.CONTEXT]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.CREATE} label="Create"
                    description="Allow users to create new items with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.CREATE]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.DELETE_OPERATION} label="Delete"
                    description="Allow users to delete items with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.DELETE_OPERATION]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.READ} label="Read"
                    description="This permission shows or hides content for the user."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.READ]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.WRITE} label="Write"
                    description="Allow users to edit items with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.WRITE]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.EXECUTE} label="Execute"
                    description="Allow users to perform functionality with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.EXECUTE]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.UPLOAD_READ} label="Upload Read"
                    description="This permission shows or hides upload related content for the user."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.UPLOAD_READ]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.UPLOAD_WRITE} label="Upload Write"
                    description="Allow users to modify uploaded content with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.UPLOAD_WRITE]} />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.UPLOAD_DELETE} label="Upload Delete"
                    description="Allow users to delete uploaded content with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.UPLOAD_DELETE]} />
            </div>
        );
    }

    async onSavePermissions() {
        await this.setState({
            saveInProgress: true
        });
        const { permissionsData } = this.state;
        if (!permissionsData[PERMISSIONS_TABLE.DESCRIPTOR_NAME] || !permissionsData[PERMISSIONS_TABLE.CONTEXT]) {
            this.setState({
                errorMessage: 'Please select Descriptor name and context'
            });
            return false;
        }
        const saved = this.props.saveRole(permissionsData);
        if (saved) {
            this.setState({
                permissionsData: {}
            });
        }
        await this.setState({
            saveInProgress: false
        });
        return saved;
    }

    onDeletePermissions(permissionsToDelete) {
        if (permissionsToDelete) {
            this.props.deleteRole(permissionsToDelete);
        }
    }

    render() {
        const { canCreate, canDelete, inProgress, fetching, nestedInModal } = this.props;
        const savingInProgress = inProgress || this.state.saveInProgress;
        return (
            <div>
                <TableDisplay
                    modalTitle="Role Permissions"
                    inProgress={savingInProgress}
                    fetching={fetching}
                    tableNewButtonLabel="Add"
                    tableDeleteButtonLabel="Remove"
                    tableSearchable={false}
                    autoRefresh={false}
                    tableRefresh={false}
                    onConfigSave={this.onSavePermissions}
                    onConfigDelete={this.onDeletePermissions}
                    onConfigClose={this.onPermissionsClose}
                    newConfigFields={this.createPermissionsModal}
                    editState={this.onEdit}
                    columns={this.createPermissionsColumns()}
                    data={this.retrievePermissionsData()}
                    refreshData={() => null}
                    deleteButton={canDelete}
                    newButton={canCreate}
                    sortName={PERMISSIONS_TABLE.DESCRIPTOR_NAME}
                    errorDialogMessage={this.state.errorMessage}
                    clearModalFieldState={() => this.setState({ errorMessage: null })}
                    nestedInAnotherModal={nestedInModal} />
            </div>
        );
    }
}

PermissionTable.propTypes = {
    data: PropTypes.array.isRequired,
    saveRole: PropTypes.func.isRequired,
    deleteRole: PropTypes.func.isRequired,
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    descriptors: PropTypes.array,
    inProgress: PropTypes.bool,
    fetching: PropTypes.bool,
    nestedInModal: PropTypes.bool
};

PermissionTable.defaultProps = {
    canCreate: true,
    canDelete: true,
    descriptors: [],
    inProgress: false,
    fetching: false,
    nestedInModal: false
};

export default PermissionTable;
