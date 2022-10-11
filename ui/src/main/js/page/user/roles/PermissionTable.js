import React, { useCallback, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Table from 'common/component/table/Table';
import PermissionTableActions from 'page/user/roles/PermissionTableActions';
import PermissionCell from 'page/user/roles/PermissionCell';
import PermissionRowAction from 'page/user/roles/PermissionRowAction';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import { useSelector } from 'react-redux';

const PermissionTable = ({ data, role, sendPermissionArray }) => {
    const [selected, setSelected] = useState([]);
    const [permissionData, setPermissionData] = useState(data);

    const descriptors = useSelector(state => state.descriptors.items);

    const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
    const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);

    useEffect(() => {
        sendPermissionArray(permissionData);
    }, [permissionData]);

    const onSelected = selected => {
        setSelected(selected);
    };
    
    function handleValidatePermission(permission) {
        setPermissionData(permissionArray => [...permissionArray, permission])
    }

    const COLUMNS = [{
        key: 'descriptorName',
        label: 'Descriptor',
        sortable: true
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
        settings: { alignment: 'right', parentData: role }
    }];

    return (
        <>
            <Table 
                tableData={permissionData}
                columns={COLUMNS}
                selected={selected}
                onSelected={onSelected}
                tableActions={() => <PermissionTableActions data={data} selected={selected} canDelete={canDelete} handleValidatePermission={handleValidatePermission}/>}
            />
        </>
        
    )
}

export default PermissionTable;