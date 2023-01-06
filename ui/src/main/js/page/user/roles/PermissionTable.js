import React, { useCallback, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import PermissionTableActions from 'page/user/roles/PermissionTableActions';
import PermissionCell from 'page/user/roles/PermissionCell';
import DescriptorNameCell from 'page/user/roles/DescriptorNameCell';
import PermissionRowAction from 'page/user/roles/PermissionRowAction';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import { useSelector } from 'react-redux';

const PermissionTable = ({ data, role, sendPermissionArray }) => {
    const [selected, setSelected] = useState([]);
    const [permissionData, setPermissionData] = useState(role.permissions);

    const descriptors = useSelector((state) => state.descriptors.items);

    const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
    const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);

    useEffect(() => {
        sendPermissionArray(permissionData);
    }, [permissionData]);

    const onSelected = (selectedRow) => {
        setSelected(selectedRow);
    };

    function handleValidatePermission(permission) {
        setPermissionData((permissionArray) => [...permissionArray, permission]);
    }

    const COLUMNS = [{
        key: 'descriptorName',
        label: 'Descriptor',
        sortable: true,
        customCell: DescriptorNameCell
    }, {
        key: 'context',
        label: 'Context',
        sortable: true
    }, {
        key: 'permissions',
        label: 'Permissions',
        sortable: true,
        customCell: PermissionCell
    }, {
        key: 'permissionAction',
        label: '',
        sortable: false,
        customCell: PermissionRowAction,
        settings: { alignment: 'right', permissionData, role }
    }];

    return (
        <>
            <Table
                tableData={permissionData}
                columns={COLUMNS}
                selected={selected}
                onSelected={onSelected}
                tableActions={() => <PermissionTableActions data={data} selected={selected} canDelete={canDelete} handleValidatePermission={handleValidatePermission} />}
            />
        </>
    );
};

PermissionTable.propTypes = {
    data: PropTypes.arrayOf(PropTypes.shape({
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
    sendPermissionArray: PropTypes.func
};

export default PermissionTable;
