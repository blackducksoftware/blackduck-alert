import React from 'react';
import { useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import PermissionTableActions from 'page/usermgmt/roles/PermissionTableActions';
import PermissionCell from 'page/usermgmt/roles/PermissionCell';
import DescriptorNameCell from 'page/usermgmt/roles/DescriptorNameCell';
import PermissionRowAction from 'page/usermgmt/roles/PermissionRowAction';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';

const emptyTableConfig = {
    message: 'There are no records to display for this table.  Please create a Permission above to use this table.'
};

const PermissionTable = ({ role, sendPermissionArray, handleFilterPermission }) => {
    const permissionData = role.permissions;

    const descriptors = useSelector((state) => state.descriptors.items);
    const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
    const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);

    function handleValidatePermission(permission) {
        const updatedPermissions = [...permissionData, permission];
        sendPermissionArray(updatedPermissions);
    }

    function handleRemovePermission(data) {
        handleFilterPermission(data);
    }

    const COLUMNS = [{
        key: 'descriptorName',
        label: 'Descriptor',
        sortable: false,
        customCell: DescriptorNameCell
    }, {
        key: 'context',
        label: 'Context',
        sortable: false
    }, {
        key: 'permissions',
        label: 'Permissions',
        sortable: false,
        customCell: PermissionCell
    }, {
        key: 'permissionAction',
        label: '',
        sortable: false,
        customCell: PermissionRowAction,
        settings: { alignment: 'right', permissionData, role },
        customCallback: handleRemovePermission
    }];

    return (
        <Table
            tableData={role.permissions}
            columns={COLUMNS}
            emptyTableConfig={emptyTableConfig}
            tableActions={() => <PermissionTableActions data={role} canDelete={canDelete} handleValidatePermission={handleValidatePermission} />}
        />
    );
};

PermissionTable.propTypes = {
    role: PropTypes.shape({
        alignment: PropTypes.string,
        permissions: PropTypes.arrayOf(PropTypes.shape({
            context: PropTypes.string,
            create: PropTypes.bool,
            delete: PropTypes.bool,
            descriptorName: PropTypes.string,
            execute: PropTypes.bool,
            read: PropTypes.bool,
            uploadDelete: PropTypes.bool,
            uploadRead: PropTypes.bool,
            uploadWrite: PropTypes.bool,
            write: PropTypes.bool
        })),
        role: PropTypes.shape({
            id: PropTypes.string,
            roleName: PropTypes.string,
            permissionData: PropTypes.arrayOf(PropTypes.shape({
                context: PropTypes.string,
                create: PropTypes.bool,
                delete: PropTypes.bool,
                descriptorName: PropTypes.string,
                execute: PropTypes.bool,
                read: PropTypes.bool,
                uploadDelete: PropTypes.bool,
                uploadRead: PropTypes.bool,
                uploadWrite: PropTypes.bool,
                write: PropTypes.bool
            }))
        })
    }),
    sendPermissionArray: PropTypes.func,
    handleFilterPermission: PropTypes.func
};

export default PermissionTable;
