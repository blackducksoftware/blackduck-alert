import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'common/table/TableDisplay';
import DynamicSelectInput from 'common/input/DynamicSelectInput';
import CheckboxInput from 'common/input/CheckboxInput';
import StatusMessage from 'common/StatusMessage';

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
        this.onCopy = this.onCopy.bind(this);

        this.state = {
            permissionsData: {},
            errorMessage: null,
            saveInProgress: false
        };
    }

    handlePermissionsChange(e) {
        const {
            name, value, type, checked
        } = e.target;
        const { permissionsData } = this.state;
        const updatedValue = type === 'checkbox' ? checked.toString()
            .toLowerCase() === 'true' : value;
        const trimmedValue = (Array.isArray(updatedValue) && updatedValue.length > 0) ? updatedValue[0] : updatedValue;

        let newPermissions = { ...permissionsData, [name]: trimmedValue };
        if (newPermissions && newPermissions.descriptorName !== 'Authentication') {
            newPermissions = {
                ...newPermissions,
                [PERMISSIONS_TABLE.UPLOAD_READ]: undefined,
                [PERMISSIONS_TABLE.UPLOAD_WRITE]: undefined,
                [PERMISSIONS_TABLE.UPLOAD_DELETE]: undefined
            };
        }
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

        return data.map((permission) => {
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
            return {
                id: permission.id,
                [PERMISSIONS_TABLE.DESCRIPTOR_NAME]: descriptorName,
                [PERMISSIONS_TABLE.CONTEXT]: permission[PERMISSIONS_TABLE.CONTEXT],
                permissionsColumn: permissionShorthand.join('-')
            };
        });
    }

    convertPermissionsColumn(permissions) {
        const {
            permissionsColumn, descriptorName, context, id
        } = permissions;
        const splitPermissions = permissionsColumn.split('-');

        return {
            id,
            descriptorName,
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

        descriptors.forEach((descriptor) => {
            const { label } = descriptor;
            if (!nameCache.includes(label)) {
                nameCache.push(label);
                descriptorOptions.push({
                    label,
                    value: label
                });
            }
        });

        return descriptorOptions;
    }

    createContextOptions() {
        const { permissionsData } = this.state;
        const { descriptors } = this.props;

        const availableContexts = [];
        if (permissionsData && permissionsData.descriptorName) {
            descriptors.forEach((descriptor) => {
                if (descriptor.label === permissionsData.descriptorName) {
                    availableContexts.push(descriptor.context);
                }
            });

            if (permissionsData.context && !availableContexts.includes(permissionsData.context)) {
                this.setState({
                    permissionsData: {
                        descriptorName: permissionsData.descriptorName
                    }
                });
            }
        }

        return availableContexts.map((context) => ({
            label: context,
            value: context
        }));
    }

    onPermissionsClose(callback) {
        this.setState({
            permissionsData: {}
        }, callback);
    }

    onEdit(selectedRow, callback) {
        const parsedPermissions = this.convertPermissionsColumn(selectedRow);
        this.setState({
            permissionsData: parsedPermissions
        }, callback);
    }

    onCopy(selectedRow, callback) {
        const selectedRowCopy = {
            ...selectedRow,
            id: null
        };
        const parsedPermissions = this.convertPermissionsColumn(selectedRowCopy);
        this.setState({
            permissionsData: parsedPermissions
        }, callback);
    }

    createPermissionsModal() {
        const { permissionsData, errorMessage } = this.state;

        let uploadInputs;
        // Currently, there does not seem to be a good way to filter this dynamically.
        // For now, restrict upload permissions to 'Authentication'.
        if (permissionsData && permissionsData.descriptorName === 'Authentication') {
            uploadInputs = (
                <div>
                    <CheckboxInput
                        name={PERMISSIONS_TABLE.UPLOAD_READ}
                        id={PERMISSIONS_TABLE.UPLOAD_READ}
                        label="Upload Read"
                        description="This permission shows or hides upload related content for the user."
                        onChange={this.handlePermissionsChange}
                        isChecked={permissionsData[PERMISSIONS_TABLE.UPLOAD_READ]}
                    />
                    <CheckboxInput
                        name={PERMISSIONS_TABLE.UPLOAD_WRITE}
                        id={PERMISSIONS_TABLE.UPLOAD_WRITE}
                        label="Upload Write"
                        description="Allow users to modify uploaded content with this permission."
                        onChange={this.handlePermissionsChange}
                        isChecked={permissionsData[PERMISSIONS_TABLE.UPLOAD_WRITE]}
                    />
                    <CheckboxInput
                        name={PERMISSIONS_TABLE.UPLOAD_DELETE}
                        id={PERMISSIONS_TABLE.UPLOAD_DELETE}
                        label="Upload Delete"
                        description="Allow users to delete uploaded content with this permission."
                        onChange={this.handlePermissionsChange}
                        isChecked={permissionsData[PERMISSIONS_TABLE.UPLOAD_DELETE]}
                    />
                </div>
            );
        }

        return (
            <div>
                <DynamicSelectInput
                    name={PERMISSIONS_TABLE.DESCRIPTOR_NAME}
                    id={PERMISSIONS_TABLE.DESCRIPTOR_NAME}
                    label="Descriptor Name"
                    options={this.createDescriptorOptions()}
                    clearable={false}
                    onChange={this.handlePermissionsChange}
                    value={permissionsData[PERMISSIONS_TABLE.DESCRIPTOR_NAME]}
                />
                <DynamicSelectInput
                    name={PERMISSIONS_TABLE.CONTEXT}
                    id={PERMISSIONS_TABLE.CONTEXT}
                    label="Context"
                    options={this.createContextOptions()}
                    clearable={false}
                    onChange={this.handlePermissionsChange}
                    value={permissionsData[PERMISSIONS_TABLE.CONTEXT]}
                />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.CREATE}
                    id={PERMISSIONS_TABLE.CREATE}
                    label="Create"
                    description="Allow users to create new items with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.CREATE]}
                />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.DELETE_OPERATION}
                    id={PERMISSIONS_TABLE.DELETE_OPERATION}
                    label="Delete"
                    description="Allow users to delete items with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.DELETE_OPERATION]}
                />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.READ}
                    id={PERMISSIONS_TABLE.READ}
                    label="Read"
                    description="This permission shows or hides content for the user."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.READ]}
                />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.WRITE}
                    id={PERMISSIONS_TABLE.WRITE}
                    label="Write"
                    description="Allow users to edit items with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.WRITE]}
                />
                <CheckboxInput
                    name={PERMISSIONS_TABLE.EXECUTE}
                    id={PERMISSIONS_TABLE.EXECUTE}
                    label="Execute"
                    description="Allow users to perform functionality with this permission."
                    onChange={this.handlePermissionsChange}
                    isChecked={permissionsData[PERMISSIONS_TABLE.EXECUTE]}
                />
                {uploadInputs}
                {errorMessage
                && (
                    <StatusMessage id="permission-table-status-message" errorMessage={errorMessage} />
                )}
            </div>
        );
    }

    async onSavePermissions(callback) {
        await this.setState({
            saveInProgress: true
        });
        const { data, saveRole } = this.props;
        const { permissionsData } = this.state;
        if (!permissionsData[PERMISSIONS_TABLE.DESCRIPTOR_NAME] || !permissionsData[PERMISSIONS_TABLE.CONTEXT]) {
            await this.setState({
                errorMessage: 'Please select Descriptor name and context',
                saveInProgress: false
            });
            return false;
        }
        const duplicates = data.filter((permission) => permission[PERMISSIONS_TABLE.DESCRIPTOR_NAME] === permissionsData[PERMISSIONS_TABLE.DESCRIPTOR_NAME]
            && permission[PERMISSIONS_TABLE.CONTEXT] === permissionsData[PERMISSIONS_TABLE.CONTEXT]
            && permission.id !== permissionsData.id);

        if (duplicates && duplicates.length > 0) {
            await this.setState({
                errorMessage: `Can't add a duplicate permission. A permission already exists for Descriptor: ${permissionsData[PERMISSIONS_TABLE.DESCRIPTOR_NAME]} and Context: ${permissionsData[PERMISSIONS_TABLE.CONTEXT]}`,
                saveInProgress: false
            });
            return false;
        }

        const saved = saveRole(permissionsData);
        if (saved) {
            this.setState({
                permissionsData: {}
            });
        }
        await this.setState({
            saveInProgress: false
        });
        callback(saved);
        return saved;
    }

    onDeletePermissions(permissionsToDelete, callback) {
        const { deleteRole } = this.props;
        if (permissionsToDelete) {
            deleteRole(permissionsToDelete);
            callback();
        }
    }

    render() {
        const { saveInProgress } = this.state;
        const {
            canCreate, canDelete, inProgress, fetching, nestedInModal
        } = this.props;
        const savingInProgress = inProgress || saveInProgress;
        return (
            <div>
                <TableDisplay
                    id="permissions"
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
                    onEditState={this.onEdit}
                    onConfigCopy={this.onCopy}
                    newConfigFields={this.createPermissionsModal}
                    columns={this.createPermissionsColumns()}
                    data={this.retrievePermissionsData()}
                    refreshData={() => null}
                    deleteButton={canDelete}
                    newButton={canCreate}
                    sortName={PERMISSIONS_TABLE.DESCRIPTOR_NAME}
                    clearModalFieldState={() => this.setState({ errorMessage: null })}
                    nestedInAnotherModal={nestedInModal}
                />
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
