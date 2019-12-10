import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
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
        this.onUpdatePermissions = this.onUpdatePermissions.bind(this);

        this.state = {
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
        const { data } = this.props;
        if (!data) {
            return [];
        }

        return data.map(permission => {
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
                permissionsColumn: permissionShorthand.join('-')
            };
        });
    }

    convertPermissionsColumn(permissions) {
        const { permissionsColumn, descriptorName, context } = permissions;
        const splitPermissions = permissionsColumn.split('-');

        return {
            descriptorName,
            context,
            [CREATE]: splitPermissions.includes('c'),
            [DELETE_OPERATION]: splitPermissions.includes('d'),
            [READ]: splitPermissions.includes('r'),
            [WRITE]: splitPermissions.includes('w'),
            [EXECUTE]: splitPermissions.includes('x'),
            [UPLOAD_READ]: splitPermissions.includes('ur'),
            [UPLOAD_WRITE]: splitPermissions.includes('uw'),
            [UPLOAD_DELETE]: splitPermissions.includes('ud')
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
        }]
    }

    onPermissionsClose() {
        this.setState({
            permissionsData: {}
        });
    }

    isMatchingPermissions(first, second) {
        return first.descriptorName === second.descriptorName &&
            first.context === second.context &&
            first[CREATE] === second[CREATE] &&
            first[DELETE_OPERATION] === second[DELETE_OPERATION] &&
            first[READ] === second[READ] &&
            first[WRITE] === second[WRITE] &&
            first[EXECUTE] === second[EXECUTE] &&
            first[UPLOAD_READ] === second[UPLOAD_READ] &&
            first[UPLOAD_DELETE] === second[UPLOAD_DELETE] &&
            first[UPLOAD_WRITE] === second[UPLOAD_WRITE];
    }

    createPermissionsModal(selectedRow) {
        const { permissionsData } = this.state;
        let newPermissions = permissionsData;
        if (selectedRow) {
            const parsedPermissions = this.convertPermissionsColumn(selectedRow);
            newPermissions = Object.assign({}, parsedPermissions, permissionsData);
            if (!this.isMatchingPermissions(permissionsData, newPermissions)) {
                this.setState({
                    permissionsData: newPermissions
                });
            }
        }

        return (
            <div>
                <DynamicSelectInput name={DESCRIPTOR_NAME} id={DESCRIPTOR_NAME} label="Descriptor Name" options={this.createDescriptorOptions()} onChange={this.handlePermissionsChange} value={newPermissions[DESCRIPTOR_NAME]} />
                <DynamicSelectInput name={CONTEXT} id={CONTEXT} label="Context" options={this.createContextOptions()} onChange={this.handlePermissionsChange} value={newPermissions[CONTEXT]} />
                <CheckboxInput name={CREATE} label="Create" description="Allow users to create new items with this permission." onChange={this.handlePermissionsChange} isChecked={newPermissions[CREATE]} />
                <CheckboxInput name={DELETE_OPERATION} label="Delete" description="Allow users to delete items with this permission." onChange={this.handlePermissionsChange} isChecked={newPermissions[DELETE_OPERATION]} />
                <CheckboxInput name={READ} label="Read" description="This permission shows or hides content for the user." onChange={this.handlePermissionsChange} isChecked={newPermissions[READ]} />
                <CheckboxInput name={WRITE} label="Write" description="Allow users to edit items with this permission." onChange={this.handlePermissionsChange} isChecked={newPermissions[WRITE]} />
                <CheckboxInput name={EXECUTE} label="Execute" description="Allow users to perform functionality with this permission." onChange={this.handlePermissionsChange} isChecked={newPermissions[EXECUTE]} />
                <CheckboxInput name={UPLOAD_READ} label="Upload Read" description="This permission shows or hides upload related content for the user." onChange={this.handlePermissionsChange}
                               isChecked={newPermissions[UPLOAD_READ]} />
                <CheckboxInput name={UPLOAD_WRITE} label="Upload Write" description="Allow users to modify uploaded content with this permission." onChange={this.handlePermissionsChange}
                               isChecked={newPermissions[UPLOAD_WRITE]} />
                <CheckboxInput name={UPLOAD_DELETE} label="Upload Delete" description="Allow users to delete uploaded content with this permission." onChange={this.handlePermissionsChange}
                               isChecked={newPermissions[UPLOAD_DELETE]} />
            </div>
        );
    }

    onSavePermissions() {
        const { permissionsData } = this.state;
        if (!permissionsData[DESCRIPTOR_NAME] || !permissionsData[CONTEXT]) {
            console.log('ERROR: Did not select Descriptor name and context');
        } else {
            const { permissionsData } = this.state;
            this.props.saveRole(permissionsData);
            this.setState({
                permissionsData: {}
            });
        }
    }

    onUpdatePermissions() {
        const { permissionsData } = this.state;
        this.props.updateRole(permissionsData);
        this.setState({
            permissionsData: {}
        });
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

    render() {
        const { canCreate, canDelete } = this.props;

        return (
            <div>
                <TableDisplay
                    modalTitle="Role Permissions"
                    tableNewButtonLabel="Add"
                    tableDeleteButtonLabel="Remove"
                    tableSearchable={false}
                    autoRefresh={false}
                    tableRefresh={false}
                    onConfigSave={this.onSavePermissions}
                    onConfigUpdate={this.onUpdatePermissions}
                    onConfigDelete={this.onDeletePermissions}
                    onConfigClose={this.onPermissionsClose}
                    newConfigFields={this.createPermissionsModal}
                    columns={this.createPermissionsColumns()}
                    data={this.retrievePermissionsData()}
                    refreshData={() => null}
                    deleteButton={canDelete}
                    newButton={canCreate}
                    sortName={DESCRIPTOR_NAME} />
            </div>
        );
    }
}

PermissionTable.propTypes = {
    data: PropTypes.array.isRequired,
    updateRole: PropTypes.func.isRequired,
    saveRole: PropTypes.func.isRequired,
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    descriptors: PropTypes.array
};

PermissionTable.defaultProps = {
    canCreate: true,
    canDelete: true,
};

export default PermissionTable;
